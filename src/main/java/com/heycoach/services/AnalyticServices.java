package com.heycoach.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.heycoach.model.AnalyticsSnapshot;
import com.heycoach.model.User;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.*;


@Service
public class AnalyticServices {
    private static final Logger logger = Logger.getLogger(ChatServices.class);
    @Autowired FirebaseClientServices firebaseClientServices;
    @Autowired EmailServices emailServices;
    @Autowired SMSServices smsServices;
    @Autowired UserServices userServices;
    @Autowired ChatServices chatServices;
    @Autowired EnvironmentServices environmentServices;

    public boolean createDBSnapshot() {
        AnalyticsSnapshot snapshot = gatherSnapshot();
        saveSnapshotToDB(snapshot);
        return true;
    }


    public AnalyticsSnapshot gatherSnapshot() {
        logger.debug("AnalyticsServices gathering snapshot...");
        AnalyticsSnapshot analyticsSnapshot = new AnalyticsSnapshot();

        gatherUserData(analyticsSnapshot);
        gatherChatData(analyticsSnapshot);

        logger.debug("AnalyticsServices gathered snapshot.");
        return analyticsSnapshot;
    }



    private boolean saveSnapshotToDB(AnalyticsSnapshot analyticsSnapshot) {
        String uri = environmentServices.getFirebaseURI() + "/analytics/snapshots.json";

        try {
            if (firebaseClientServices.callFirebaseREST(uri, HttpMethod.POST, analyticsSnapshot.toJson())) {
            } else
                return false;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Object gatherChatData(AnalyticsSnapshot analyticsSnapshot) {
        logger.debug("Obtaining ALL chat messages in JSON...");
        String results = firebaseClientServices.getNodeList("chat");
        JSONObject nodes = new JSONObject(results);
        HashMap listCoaches = (HashMap<String, Object>) nodes.toMap();//converts it to a map of each entry being fbid, hashmap of data
        long i = 0;
        int countChatMessages = 0;
        int maxChatMessages = 0;
        int countChatStandard = 0;
        int countChatProposal = 0;
        int countChatImage = 0;
        int countChatSystem = 0;
        int countCoachClientMessages = 0;
        int countCoachClientSessions = 0;
        String curCoach, curClient;
        String maxCoach="", maxClient="";
        Iterator<Map.Entry<String, Map>> itCoaches = listCoaches.entrySet().iterator();//go through each node
        while (itCoaches.hasNext()) {
            Map.Entry<String, Map> fbNode = itCoaches.next();
            HashMap<String, Object> coach = (HashMap<String, Object>) fbNode.getValue();//each node is a key and map of actual properties
            curCoach = fbNode.getKey();
            Iterator<Map.Entry<String, Object>> itCoachClients = coach.entrySet().iterator();//go through each user node
            while (itCoachClients.hasNext()) {
                countCoachClientSessions++;
                Map.Entry<String, Object> coachClient = itCoachClients.next();
                curClient = coachClient.getKey();
                logger.debug(coachClient);
                HashMap<String, Object> messagesAndSessions = (HashMap<String, Object>) coachClient.getValue();
                HashMap<String, Object> messages = (HashMap<String, Object>) messagesAndSessions.get("messages");
                countCoachClientMessages = 0;
                Iterator<Map.Entry<String, Object>> itMessages = messages.entrySet().iterator();//go through each user node
                while (itMessages.hasNext()) {
                    countChatMessages++;
                    countCoachClientMessages++;
                    if (countCoachClientMessages > maxChatMessages) {
                        maxChatMessages = countCoachClientMessages;
                        maxCoach = curCoach;
                        maxClient = curClient;
                    }
                    Map.Entry<String, Object> message = itMessages.next();
                    HashMap<String, Object> messageFields = (HashMap<String, Object>) message.getValue();
                    Iterator<Map.Entry<String, Object>> itMessageFields = messageFields.entrySet().iterator();//go through each user node
                    while (itMessageFields.hasNext()) {
                        Map.Entry<String, Object> field = itMessageFields.next();
                        String userFieldKey = field.getKey();
                        if (userFieldKey.equalsIgnoreCase("type")) {
                            String type = (String) field.getValue();
                            if (type.equalsIgnoreCase("standard")) {
                                countChatStandard++;
                            } else if (type.equalsIgnoreCase("proposal")) {
                                countChatProposal++;
                            } else if (type.equalsIgnoreCase("image")) {
                                countChatImage++;
                            } else if (type.equalsIgnoreCase("system")) {
                                countChatSystem++;
                            } else {
                                logger.error("Unknown chat message type:" + type);
                            }
                        }
                    }
                }
            }
        }
        append(analyticsSnapshot, "Chat Total conversations", countCoachClientSessions);
        append(analyticsSnapshot, "Chat Total messages", countChatMessages);
        int avg = countChatMessages / countCoachClientSessions;
        append(analyticsSnapshot, "Chat AVG messages", avg);
        append(analyticsSnapshot, "Chat MAX messages", maxChatMessages + " (coach: " + maxCoach + ", client: " + maxClient + ")");
        append(analyticsSnapshot, "Chat Type - standard", countChatStandard);
        append(analyticsSnapshot, "Chat Type - proposal", countChatProposal);
        append(analyticsSnapshot, "Chat Type - image", countChatImage);
        append(analyticsSnapshot, "Chat Type - system", countChatSystem);

        return null;
    }

    public Object gatherUserData(AnalyticsSnapshot analyticsSnapshot) {
        //can't use jackson serialized java objects because they are incomplete
        logger.debug("Obtaining ALL users in JSON...");
        String results = firebaseClientServices.getNodeList("users");

        JSONObject nodes = new JSONObject(results);
        HashMap listUsers = (HashMap<String, Object>) nodes.toMap();//converts it to a map of each entry being fbid, hashmap of data
        long i = 0;
        int countClients = 0;
        int countCoaches = 0;

        int countClientsStatusActive = 0;
        int countClientsStatusPending = 0;
        int countClientsStatusPaid = 0;

        int countInvitations = 0;
        int countInvitationsSend = 0;
        int countInvitationsSent = 0;
        int countInvitationsRemind = 0;
        int countInvitationsAccepted = 0;
        int countInvitationsAcknowledged = 0;

        int countInvitationsEmail = 0;
        int countInvitationsSMS = 0;

        int countPayments = 0;
        int countProposals = 0;
        int countProposalsPaid = 0;
        Double totalCharges = 0.00;
        Double totalFees = 0.00;

        Iterator<Map.Entry<String, Map>> itUsers = listUsers.entrySet().iterator();//go through each node
        String curType = "", curStatus = "";
        while (itUsers.hasNext()) {
            Map.Entry<String, Map> fbNode = itUsers.next();
            HashMap<String, Object> user = (HashMap<String, Object>) fbNode.getValue();//each node is a key and map of actual properties
            Iterator<Map.Entry<String, Object>> itUserFields = user.entrySet().iterator();//go through each user node
            while (itUserFields.hasNext()) {
                Map.Entry<String, Object> userField = itUserFields.next();
                String userFieldKey = userField.getKey();
                if (userFieldKey.equalsIgnoreCase("type")) {
                    curType = (String) userField.getValue();
                } else if (userFieldKey.equalsIgnoreCase("status")) {
                    curStatus = (String) userField.getValue();
                } else if (userField.getKey().equalsIgnoreCase("Invitations")) {
                    HashMap<String, Object> invitations = (HashMap<String, Object>) userField.getValue();//each node is a key and map of actual properties
                    Iterator<Map.Entry<String, Object>> itInvitations = invitations.entrySet().iterator();//go through each user node
                    while (itInvitations.hasNext()) {
                        Map.Entry<String, Object> invitation = itInvitations.next();
                        countInvitations++;
                        HashMap<String, Object> invitationFields = (HashMap<String, Object>) invitation.getValue();
                        Iterator<Map.Entry<String, Object>> itInvitationFields = invitationFields.entrySet().iterator();//go through each user node
                        while (itInvitationFields.hasNext()) {
                            Map.Entry<String, Object> invitationField = itInvitationFields.next();
                            String key = (String) invitationField.getKey();
                            if (key.equalsIgnoreCase("status")) {
                                String value = (String) invitationField.getValue();
                                if (value.equalsIgnoreCase("send"))
                                    countInvitationsSend++;
                                else if (value.equalsIgnoreCase("sent"))
                                    countInvitationsSent++;
                                else if (value.equalsIgnoreCase("remind"))
                                    countInvitationsRemind++;
                                else if (value.equalsIgnoreCase("accepted"))
                                    countInvitationsAccepted++;
                                else if (value.equalsIgnoreCase("acknowledged")) //"got it"
                                    countInvitationsAcknowledged++;
                                else
                                    logger.error("Unknown invitation status:" + value);
                            }
                            if (key.equalsIgnoreCase("receiver")) {
                                String value = (String) invitationField.getValue();
                                if (value.contains("@"))
                                    countInvitationsEmail++;
                                else
                                    countInvitationsSMS++;
                            }
                        }
                    }
                    //invitationsSent++;
                } else if (userField.getKey().equalsIgnoreCase("Payments")) {
                    HashMap<String, Object> payments = (HashMap<String, Object>) userField.getValue();//each node is a key and map of actual properties
                    Iterator<Map.Entry<String, Object>> itPayments = payments.entrySet().iterator();//go through each user node
                    while (itPayments.hasNext()) {
                        Map.Entry<String, Object> invitation = itPayments.next();
                        countPayments++;
/*
                        HashMap<String, Object> invitationFields = (HashMap<String, Object>) invitation.getValue();
                        Iterator<Map.Entry<String, Object>> itInvitationFields = invitationFields.entrySet().iterator();//go through each user node
                        while (itInvitationFields.hasNext()) {
                            Map.Entry<String, Object> invitationField = itInvitationFields.next();
                            String key = (String) invitationField.getKey();
                            if (key.equalsIgnoreCase("status")) {
                                String value = (String) invitationField.getValue();
                                if (value.equalsIgnoreCase("send"))
                                    countInvitationsSend++;
                                else if (value.equalsIgnoreCase("sent"))
                                    countInvitationsSent++;
                                else if (value.equalsIgnoreCase("remind"))
                                    countInvitationsRemind++;
                                else if (value.equalsIgnoreCase("accepted"))
                                    countInvitationsAccepted++;
                                else if (value.equalsIgnoreCase("acknowledged")) //"got it"
                                    countInvitationsAcknowledged++;
                                else
                                    logger.error("Unknown invitation status:" + value);
                            }
                            if (key.equalsIgnoreCase("receiver")) {
                                String value = (String) invitationField.getValue();
                                if (value.contains("@"))
                                    countInvitationsEmail++;
                                else
                                    countInvitationsSMS++;
                            }
                        }
*/
                    }
                    //invitationsSent++;
                } else if (userField.getKey().equalsIgnoreCase("Proposals")) {
                    HashMap<String, Object> proposals = (HashMap<String, Object>) userField.getValue();//each node is a key and map of actual properties
                    Iterator<Map.Entry<String, Object>> itProposals = proposals.entrySet().iterator();//go through each user node
                    while (itProposals.hasNext()) {
                        Map.Entry<String, Object> proposal = itProposals.next();
                        countProposals++;
                        HashMap<String, Object> proposalFields = (HashMap<String, Object>) proposal.getValue();
                        Iterator<Map.Entry<String, Object>> itProposalFields = proposalFields.entrySet().iterator();//go through each user node
                        while (itProposalFields.hasNext()) {
                            Map.Entry<String, Object> proposalField = itProposalFields.next();
                            String key = (String) proposalField.getKey();
                            if (key.equalsIgnoreCase("chargeAmount")) {
                                Double value = (Double) proposalField.getValue();
                                totalCharges += value;
                            }
                            else if (key.equalsIgnoreCase("fee")) {
                                Integer value = (Integer) proposalField.getValue();
                                totalFees += value;
                            }
                            else if (key.equalsIgnoreCase("status")) {
                                String value = (String) proposalField.getValue();
                                if (value.equalsIgnoreCase("paid"))
                                    countProposalsPaid++;
                            }
                        }
                    }
                    //invitationsSent++;
                }
            }
            //user fields obtained, tally up
            if (curType.equalsIgnoreCase("coach")) {
                countCoaches++;
            } else if (curType.equalsIgnoreCase("client")) {
                countClients++;
                if (curStatus.equalsIgnoreCase("active"))
                    countClientsStatusActive++;
                else if (curStatus.equalsIgnoreCase("pending"))
                    countClientsStatusPending++;
                else if (curStatus.equalsIgnoreCase("paid"))
                    countClientsStatusPaid++;
                else
                    logger.error("Unknown client user status:" + curStatus);
            } else
                logger.error("Unknown user type:" + curType);
        }
        append(analyticsSnapshot, "Coaches", countCoaches);
        append(analyticsSnapshot, "Clients", countClients);
        append(analyticsSnapshot, "Clients pending", countClientsStatusPending);
        append(analyticsSnapshot, "Clients paid and activated", countClientsStatusActive);
        append(analyticsSnapshot, "Clients paid awaiting activation", countClientsStatusPaid);

        append(analyticsSnapshot, "Total Invitations", countInvitations);
        append(analyticsSnapshot, "Invitations waiting to be sent", countInvitationsSend);
        append(analyticsSnapshot, "Invitations pending", countInvitationsSent);
        append(analyticsSnapshot, "Invitation reminders pending", countInvitationsRemind);
        append(analyticsSnapshot, "Invitations accepted", countInvitationsAccepted + countInvitationsAcknowledged);

        append(analyticsSnapshot, "Invitations Sent via Email", countInvitationsEmail);
        append(analyticsSnapshot, "Invitations Sent via SMS", countInvitationsSMS);

        append(analyticsSnapshot, "Payments", countPayments);
        append(analyticsSnapshot, "Proposals", countProposals);
        append(analyticsSnapshot, "Proposals Paid", countProposalsPaid);
        append(analyticsSnapshot, "Proposals Fees", totalFees);
        append(analyticsSnapshot, "Proposals total charges", totalCharges);
        return null;
    }

    private void append(AnalyticsSnapshot analyticsSnapshot, String key, int value) {
        analyticsSnapshot.getList().put(key, String.valueOf(value));
    }

    private void append(AnalyticsSnapshot analyticsSnapshot, String key, String value) {
        analyticsSnapshot.getList().put(key, value);
    }

    private void append(AnalyticsSnapshot analyticsSnapshot, String key, Double value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        String formattedValue = "$" + formatter.format(value);
        analyticsSnapshot.getList().put(key, formattedValue);
    }

}
