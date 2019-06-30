/*
 * Copyright (c) 2017. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.services;

import com.heycoach.model.*;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.exceptions.InvalidSSLConfig;
import com.notnoop.exceptions.RuntimeIOException;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class NotificationServices {
    private static final Logger logger = Logger.getLogger(NotificationServices.class);

    //copied from EventDispatcher.ts in client
    //*** these correspond to the db /notifications and global.ts API_EVENT_??? and /users/[uid]/notifications
    public static final String NOTIFICATION_SETTING_ID_CHAT       = "chatmessage";
    public static final String NOTIFICATION_SETTING_ID_PROPOSAL   = "proposal";
    public static final String NOTIFICATION_SETTING_ID_WORKOUT    = "workout";
    public static final String NOTIFICATION_SETTING_ID_NUTRITION  = "nutrition";
    public static final String NOTIFICATION_SETTING_ID_PAYMENTS   = "payment";
    public static final String NOTIFICATION_SETTING_ID_IMAGE      = "image";
    public static final String NOTIFICATION_SETTING_ID_ACTIVATE   = "activate";
    public static final String NOTIFICATION_SETTING_ID_TEST       = "test:push";

    //events copied from EventDispateher.ts that will be sent from EventDispatcher as the event type
    // these correspond to above as far as whether to notify:
    public static final String  EVENT_PAYMENT               = "proposal:payment";
    public static final String  EVENT_PROPOSAL_DELETE       = "proposal:delete";
    public static final String  EVENT_PROPOSAL_ADD          = "proposal:add";
    public static final String  EVENT_PAYMENTCENTER_TAP     = "paymentCenter:tap";
    public static final String  EVENT_NETWORK_DISCONNECTED  = "network:disconnected";
    public static final String  EVENT_NETWORK_CONNECTED     = "network:connected";
    public static final String  EVENT_WORKOUT_CHANGE        = "workout";
    public static final String  EVENT_NUTRITION_CHANGE      = "nutrition";
    public static final String  EVENT_IMAGE_UPLOAD          = "image";
    public static final String  EVENT_ACTIVATE              = "activate";
    public static final String  EVENT_TEST_PUSH             = "test:push";
    public static final String  EVENT_INVITED_CLIENT_REGISTER
                                                            = "register:invited:client";
    public static final String  EVENT_SERVICE_ENDED         = "service:end";

    public static final String  EVENT_CHAT_MESSAGE_SENT     = "chat:message:new";
    public static final String  EVENT_CHAT_MESSAGE_RECEIVED = "chat:message:received";
    public static final String  EVENT_CHAT_SCROLL_TO_BOTTOM = "chat:scrolltobottom";//tell chat to scroll to bottom
    public static final String  EVENT_CHAT_TYPING_STOP      = "chat:typing:stop";
    public static final String  EVENT_CHAT_TYPING_START     = "chat:typing:start";


    @Autowired FirebaseClientServices firebaseClientServices;
    @Autowired EmailServices emailServices;
    @Autowired SMSServices smsServices;
    @Autowired UserServices userServices;
    @Autowired ChatServices chatServices;
    @Autowired EnvironmentServices environmentServices;
    ApnsService apnsService;

    List<NotificationSetting> notificationSettings;

    public boolean processInvitation(String coachId, String invitationId, boolean isReminder) {
        Invitation invitation = firebaseClientServices.getInvitiation(coachId, invitationId);
        User coach = firebaseClientServices.getUserByUID(coachId) ;
        if (coach == null) {
            logger.debug("processInvitation got coach id that doesn't exist! coach: " + coachId );
            return false;
        }
        if (invitation == null) {
            logger.debug("processInvitation got invitation id that doesn't exist! coach: " + coachId + "  invite code:" + invitationId);
            return false;
        }
        return processInvitation(coach, invitation, isReminder);
    }

    private boolean processInvitation(User coach, Invitation invitation, boolean isReminder) {
        if (!isReminder) {
            String accessCode = Utilities.getRandomAlphaNumeric(6);
            if (invitation.getReceiver().toUpperCase().contains("HCTEST"))//for automated testing
                accessCode = "HCTEST";
            invitation.setAccessCode(accessCode);
        }
        String body;
        String subject;
        boolean result;
        if (invitation.getReceiver().contains("@")) { //must be email
            if (isReminder) {
                body = emailServices.getInviteBodyReminder(coach, invitation);
                subject = emailServices.getInviteSubjectReminder(coach, invitation);
            } else {
                body = emailServices.getInviteBody(coach, invitation);
                subject = emailServices.getInviteSubject(coach, invitation);
            }
            if (emailServices.sendEmail(body, subject, invitation.getReceiver())) {
                result = updateInvitation(invitation, coach, isReminder);
            } else {
                logger.debug("Problem sending mail for invitation " + invitation.getId());
                return false;
            }
        } else { //must be SMS
            if (isReminder) {
                body = smsServices.getInviteTextReminderBody(coach, invitation);
            } else {
                body = smsServices.getInviteTextBody(coach, invitation);
            }
            if (smsServices.send(body, invitation.getReceiver())) {
                result = updateInvitation(invitation, coach, isReminder);
            } else {
                logger.debug("Problem sending mail for invitation " + invitation.getId());
                return false;
            }
        }
        if (!isReminder && result) {
            result = firebaseClientServices.createInviteCrossIndex(invitation, coach);
        }
        return result;
    }

    private boolean updateInvitation(Invitation invitation, User coach, boolean isReminder) {
        invitation.setStatus(Utilities.INVITATION_STATUS_SENT);
        invitation.setSent(Utilities.getUTCinISO());
        //invitation.setSent(Utilities.getUTC());
        if (isReminder) {
            invitation.setReminderSent(Utilities.getUTCinISO());
        }
        invitation.setClientId("");

        firebaseClientServices.updateInvitation(coach.getUid(), invitation.getId(), invitation);
        return true;
    }

//*************** APNS and FCM ****************
    //https://github.com/notnoop/java-apns
    //https://github.com/notnoop/java-apns/wiki/Compare-with-Javapns
    //to test cert, token, etc go to: http://pushtry.com/
    private boolean apnsSetup() {
        Resource resource;
        if (environmentServices.isDevelopment()) {
            resource = new ClassPathResource("apns_dev.p12");
        } else {
            resource = new ClassPathResource("apns_prod.p12");
        }
        try {
            InputStream resourceInputStream = resource.getInputStream();
            if (apnsService == null) {
                try {
                    if (environmentServices.isDevelopment()) {
                        apnsService =
                                APNS.newService()
                                        .withCert(resourceInputStream, "Heycoach2017")
                                        .withSandboxDestination()
                                        .build();
                    } else {
                        apnsService =
                                APNS.newService()
                                        .withCert(resourceInputStream, "Heycoach2017")
                                        .withAppleDestination(true)
                                        .build();
                    }
                } catch (RuntimeIOException e) {
                    e.printStackTrace();
                    ErrorService.reportError(e);
                    return false;
                } catch (InvalidSSLConfig invalidSSLConfig) {
                    invalidSSLConfig.printStackTrace();
                    ErrorService.reportError(invalidSSLConfig);
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return false;
        }
        return true;
    }

    private boolean sendNotifications(User recipient, String message, String title) {
        if (recipient == null) {
            ErrorService.reportError("sendNotifications got null user");
            return false;
        }
        if (StringUtils.isEmpty(message)) {
            ErrorService.reportError("sendNotifications got empty message");
            return false;
        }
        if (title == null || title.length() == 0)
            title = "Heycoach Notification";

        boolean result = true;//presume at this point all is ok and the rest is confusing because 2 separate notifications?
        if (!StringUtils.isEmpty(recipient.getApnsToken())) {
            if (!apnsCreateAndSendMessage(recipient.getApnsToken(), message, title)) {
                logger.debug("sendNotifications had a problem sending apns notification for user for userId:" + recipient.getUid());
                result = false;
            }
        }

        if (!StringUtils.isEmpty(recipient.getFcmToken())) {
/*  TODO: need the logic for sending FCS messages
            if (!fcmCreateAndSendMessage(recipient.getApnsToken(), message)) {
                logger.debug("sendNotifications had a problem sending fcm notification for user for userId:" + userId);
                result = false;
            }
*/
        }
        return result;
    }

    public boolean apnsCreateAndSendMessage(String token, String message, String title) {
        if (apnsSetup()) {
            String payload = APNS.newPayload().alertBody(message).alertTitle(title).build();
            apnsService.push(token, payload);
        }
        return true;
    }

    public boolean apnsInactiveDevices() {
        if (apnsSetup()) {
            Map<String, Date> inactiveDevices = apnsService.getInactiveDevices();
            for (String deviceToken : inactiveDevices.keySet()) {
                Date inactiveAsOf = inactiveDevices.get(deviceToken);
                logger.debug("Inactive devices:" + deviceToken + " as of " + inactiveAsOf);
            }
        }
        return true;
    }

    public List<NotificationSetting> getNotificationSettings() {
        logger.debug("Obtaining notification settings...");
        if (notificationSettings == null) {
            logger.debug("Obtaining notification settings from DB (this a one-time call hopefully)");
            notificationSettings = new ArrayList<NotificationSetting>();
            //get the json list which is in a stupid format not parsable by Jackson {"fb id":{blah: value, etc}, ...
            String results = firebaseClientServices.getNodeList("notifications");
            JSONObject notificationNodes = new JSONObject(results);
            HashMap list = (HashMap<String, Object>) notificationNodes.toMap();//converts it to a map of each entry being fbid, hashmap of data
            long i = 0;
            Iterator<Map.Entry<String, Map>> it = list.entrySet().iterator();//got through each notification node
            while (it.hasNext()) {
                Map.Entry<String, Map> fbNode = it.next();
                HashMap<String,Object> data = (HashMap<String, Object>) fbNode.getValue();//each node is a key and map of actual properties
                NotificationSetting setting = new NotificationSetting();
                try {
                    Utilities.setData(setting, data);//use apache commons beanutils to hydrate the object based on the hasmap
                } catch (Exception e) {
                    e.printStackTrace();
                    ErrorService.reportError(e);
                }
                notificationSettings.add(setting);
                //i += pair.getKey() + pair.getValue();
            }
        } else {
            logger.debug("notificationSettings already instantiated.");
        }
        return notificationSettings;
    }

    public NotificationSetting getNotificationSetting(String notificationId) {
        try {
            List<NotificationSetting> notificationSettingSettings = getNotificationSettings();
            for (NotificationSetting notificationSetting : notificationSettingSettings) {
                if (notificationSetting.getId() != null) { //the notes entry caused an issue
                    if (notificationSetting.getId().equals(notificationId)) {
                        return notificationSetting;
                    }
                }
            }
            //RollbarService.reportError("Unknown notification id in NotificationService.getNotificationSetting:" + notificationId);
            throw new Exception("Unknown notification id in NotificationService.getNotificationSetting:" + notificationId);
        } catch (Exception e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return null;
        }
    }

    //*********** BEGIN notification events **********************************************
    public boolean processEvent(String senderUid, String recipientUid, String eventType, String data) {
        logger.debug("processEvent for event type:" + eventType + " from " + senderUid + " to:" + recipientUid + "...");
        User sender;
        User recipient;
        ChatMessage chatMessage = null;
        if (eventType.equals(EVENT_CHAT_MESSAGE_SENT)) {
            chatMessage = chatServices.getChatMessage(senderUid, recipientUid, data);//senderUid is actually always the coach, recipient is always client
            if (chatMessage != null) {
                if (!chatMessage.getType().equals(ChatMessage.CHAT_TYPE_STANDARD)) {
                    //we ignore chats other than standard for now
                    return true;
                }
            } else {           //special case where sender is actually coach and recipient is actually client
                ErrorService.reportError("processEvent could not retrieve chat messsage, it expects coach, client NOT sender recipient. coach:" + senderUid + " client:" + recipientUid + " msg:" + data);
                return false;
            }
            sender = userServices.getUserByUID(chatMessage.getSenderId());
            recipient = userServices.getUserByUID(chatMessage.getRecipientId());
        } else {
            sender = userServices.getUserByUID(senderUid);
            recipient = userServices.getUserByUID(recipientUid);
        }
        boolean result = true;
        if (recipient.getNotifications() != null) {
//            if (Arrays.asList(recipient.getNotifications()).contains(eventType) || eventType.equals(NOTIFICATION_SETTING_ID_TEST)) {
                switch (eventType) {
                    case EVENT_PROPOSAL_ADD:
                        result = notifyClientProposal(sender, recipient, data);
                        break;
                    case EVENT_CHAT_MESSAGE_SENT:
                        result = notifyChat(sender, recipient, chatMessage);//data is a message json
                        break;
                    case EVENT_IMAGE_UPLOAD:
                        result = notifyImage(sender, recipient, data);
                        break;
                    case EVENT_NUTRITION_CHANGE:
                        result = notifyNutirtion(sender, recipient, data);
                        break;
                    case EVENT_WORKOUT_CHANGE:
                        result = notifyWorkout(sender, recipient, data);
                        break;
                    case EVENT_PAYMENT:
                        result = notifyPayment(sender, recipient, data);
                        break;
                    case EVENT_ACTIVATE:
                        result = notifyActivate(sender, recipient, data);
                        break;
                    case EVENT_TEST_PUSH:
                        result = notifyTest(sender, recipient, data);
                        break;
                    default:
                        ErrorService.reportError("Unknown eventType in NotificationServices.processEvent:" + eventType);
                        result = false;
                        break;
                }
                return result;
/*
            } else {
                //this means they have not subscribed to the notification, that's ok and not an error
                return true;
            }
*/
        } else {
            ErrorService.reportError("NotificationService.processEvent found no notifications for user recipient " + recipientUid);
            return true;
        }
    }

    public boolean notifyClientProposal(User sender, User recipient, String proposalId) {
        if (Arrays.asList(recipient.getNotifications()).contains(NOTIFICATION_SETTING_ID_PROPOSAL)) {
            logger.debug("Notifying client about proposal " + proposalId + " from coach " + recipient.getUid());
            //find the proposal in coach's list of proposals
            Proposal proposal = firebaseClientServices.getProposal(sender.getUid(), proposalId);//test
            //notify the user
            if (proposal == null) {
                ErrorService.reportError("Proposal not found in notifyClientProposal: " + proposalId);
            }

            NotificationSetting notificationSetting = getNotificationSetting(NOTIFICATION_SETTING_ID_PROPOSAL);
            String msg = notificationSetting.getMessage() + "$" + proposal.getFee();
            msg = transformText(msg, sender, recipient);

            return sendNotifications(recipient, msg, "");
        } else {
            logger.debug("NOT Notifying recipient about proposal because their notification is disabled.");
            return true;
        }
    }

    public boolean notifyChat(User sender, User recipient, ChatMessage chatMessage) {
        if (Arrays.asList(recipient.getNotifications()).contains(NOTIFICATION_SETTING_ID_CHAT)) {
            logger.debug("Notifying recipient about chat message " + chatMessage.getId());

            NotificationSetting notificationSetting = getNotificationSetting(NOTIFICATION_SETTING_ID_CHAT);
            String msg = sender.getFullName() + notificationSetting.getMessage() + chatMessage.getText();
            msg = transformText(msg, sender, recipient);
            return sendNotifications(recipient, msg, notificationSetting.getTitle());
        } else {
            logger.debug("NOT Notifying recipient about chat message because their notification is disabled." + chatMessage.getId());
            return true;
        }
    }

    public boolean notifyImage(User sender, User recipient, String data) {
        if (Arrays.asList(recipient.getNotifications()).contains(NOTIFICATION_SETTING_ID_IMAGE)) {
            logger.debug("Notifying recipient about image ");
            NotificationSetting notificationSetting = getNotificationSetting(NOTIFICATION_SETTING_ID_IMAGE);
            String msg = notificationSetting.getMessage();
            msg = transformText(msg, sender, recipient);
            return sendNotifications(recipient, msg, notificationSetting.getTitle());
        } else {
            logger.debug("NOT Notifying recipient about image because their notification is disabled.");
            return true;
        }
    }

    public boolean notifyNutirtion(User sender, User recipient, String data) {
        if (Arrays.asList(recipient.getNotifications()).contains(NOTIFICATION_SETTING_ID_IMAGE)) {
            if (recipient.getStatus().equals(Utilities.CLIENT_STATUS_ACTIVE)) {
                logger.debug("Notifying recipient about nutrition change ");
                NotificationSetting notificationSetting = getNotificationSetting(NOTIFICATION_SETTING_ID_NUTRITION);
                String msg = notificationSetting.getMessage();
                msg = transformText(msg, sender, recipient);
                return sendNotifications(recipient, msg, notificationSetting.getTitle());
            } else {
                logger.debug("NOT Notifying recipient about nutrition change because they are not active.");
                return true;
            }
        } else {
            logger.debug("NOT Notifying recipient about nutrition because their notification is disabled.");
            return true;
        }
    }

    public boolean notifyWorkout(User sender, User recipient, String data) {
        if (Arrays.asList(recipient.getNotifications()).contains(NOTIFICATION_SETTING_ID_WORKOUT)) {
            if (recipient.getStatus().equals(Utilities.CLIENT_STATUS_ACTIVE)) {
                logger.debug("Notifying recipient about workout change");
                NotificationSetting notificationSetting = getNotificationSetting(NOTIFICATION_SETTING_ID_WORKOUT);
                String msg = notificationSetting.getMessage();
                msg = transformText(msg, sender, recipient);
                return sendNotifications(recipient, msg, notificationSetting.getTitle());
            } else {
                logger.debug("NOT Notifying recipient about workout change because they are not active.");
                return true;
            }
        } else {
            logger.debug("NOT Notifying recipient about workout change because their notification is disabled.");
            return true;
        }
    }

    public boolean notifyPayment(User sender, User recipient, String data) {
        if (Arrays.asList(recipient.getNotifications()).contains(NOTIFICATION_SETTING_ID_PAYMENTS)) {
            logger.debug("Notifying recipient about chat message ");
            NotificationSetting notificationSetting = getNotificationSetting(NOTIFICATION_SETTING_ID_PAYMENTS);
            String msg = notificationSetting.getMessage();
            msg = transformText(msg, sender, recipient);
            return sendNotifications(recipient, msg, notificationSetting.getTitle());
        } else {
            logger.debug("NOT Notifying recipient about payment because their notification is disabled.");
            return true;
        }
    }

    public boolean notifyActivate(User sender, User recipient, String data) {
        if (Arrays.asList(recipient.getNotifications()).contains(NOTIFICATION_SETTING_ID_ACTIVATE)) {
            logger.debug("Notifying recipient about activate ");
            NotificationSetting notificationSetting = getNotificationSetting(NOTIFICATION_SETTING_ID_ACTIVATE);
            String msg = notificationSetting.getMessage();
            msg = transformText(msg, sender, recipient);
            return sendNotifications(recipient, msg, notificationSetting.getTitle());
        } else {
            logger.debug("NOT Notifying recipient about activate because their notification is disabled.");
            return true;
        }
    }

    public boolean notifyTest(User coach, User client, String data) {
        logger.debug("Notifying recipient TEST ");
        NotificationSetting notificationSetting = getNotificationSetting(NOTIFICATION_SETTING_ID_TEST);
        if (notificationSetting != null) {
            String msg = transformText(notificationSetting.getMessage(), coach, client);
            sendNotifications(coach, msg + " - from coach to client sent " + new Date(), notificationSetting.getTitle());
            return sendNotifications(client, msg + " - from client to coach sent " + new Date(), notificationSetting.getTitle());
        } else {
            return false;
        }
    }

    //*********** END notification events **********************************************

    private String transformText(String text, User sender, User recipient) {
        String newText = text;
        if (sender.isCoach()) {
            newText = newText.replaceAll(Pattern.quote("[coachFullName]"), sender.getFullName());
            newText = newText.replaceAll(Pattern.quote("[clientFullName]"), recipient.getFullName());
        } else { //recipient is coach
            newText = newText.replaceAll(Pattern.quote("[coachFullName]"), recipient.getFullName());
            newText = newText.replaceAll(Pattern.quote("[clientFullName]"), sender.getFullName());
        }
        return newText;
    }
}
