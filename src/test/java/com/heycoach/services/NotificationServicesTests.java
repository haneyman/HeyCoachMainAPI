/*
 * Copyright (c) 2016. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */
package com.heycoach.services;

import com.heycoach.model.ChatMessage;
import com.heycoach.model.NotificationSetting;
import com.heycoach.model.User;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static com.heycoach.services.NotificationServices.EVENT_CHAT_MESSAGE_SENT;
import static com.heycoach.services.NotificationServices.NOTIFICATION_SETTING_ID_CHAT;
import static com.heycoach.services.NotificationServices.NOTIFICATION_SETTING_ID_IMAGE;

//@RunWith(SpringJUnit4ClassRunner.class)
//@WebAppConfiguration
//@ContextConfiguration({"file: src/main/webapp/WEB-INF/web.xml", "file: /src/main/webapp/WEB-INF/api-servlet.xml"})

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:api-servlet.xml")
public class NotificationServicesTests  extends TestCase {
    static {
        System.setProperty("HC_ENVIRONMENT", "DEVELOPMENT");
    }

    @Autowired
    private NotificationServices notificationServices;
    @Autowired EnvironmentServices environmentServices;
    @Autowired FirebaseClientServices firebaseClientServices;
    @Autowired Helper helper;


    @Before
    public void setup() {
        ErrorService.isDisabled = true;
    }


    @Test
    public void processInvitationTest() throws Exception {
        String invitationId = "-KuvbvoeXGhkb_eT0I5h";
        boolean result = notificationServices.processInvitation("", invitationId, false);
        assertTrue(result);
    }

    @Test
    public void processInvitationEmailTest() throws Exception {
        String invitationId = "-KuvbvoeXGhkb_eT0I5h";
        String coach = "g0bGQkFiNQQO5umIBQuj3vsox1m2";
        boolean result = notificationServices.processInvitation(coach, invitationId, false);
        assertTrue(result);
    }


    @Test
    public void processInvitationEmailReminderTest() throws Exception {
        String invitationId = "-KuvbvoeXGhkb_eT0I5h";
        String coach = "g0bGQkFiNQQO5umIBQuj3vsox1m2";
        boolean result = notificationServices.processInvitation(coach, invitationId, true);
        assertTrue(result);
    }

    //**** APNS *****
    @Test
    public void testAPNSPushTestToCoach1() throws Exception {
        User coachtest1 = helper.getCoachTest1();
        //User clienttest1 = helper.getClientTest1();
        boolean result = notificationServices.apnsCreateAndSendMessage(coachtest1.getApnsToken(), "test from api junit test.", "test title");
        assertTrue(result);
    }

    @Test
    public void testAPNSPushTestViaNotifyMethodToCoach1() throws Exception {
        User coachtest1 = helper.getCoachTest1();
        User clienttest1 = helper.getClientTest1();
        boolean result = notificationServices.notifyTest(coachtest1, clienttest1, "unit test message.");
        assertTrue(result);
    }

/*
    @Test
    public void testAPNSSendMessageForProposalAddToCoach1() throws Exception {
        User coachtest1 = helper.getCoachTest1();
        User clienttest1 = helper.getClientTest1();
//        Proposal proposal = coachtest1.
        String proposalId = "-KnUMVPktG1gm9fI6X0e";
        boolean result = notificationServices.notifyClientProposal(coachtest1, clienttest1, proposalId);
        assertTrue(result);
    }
*/

    @Test
    public void testAPNSInactivePhones() throws Exception {
//        String token = "ad760679813cc37b1fbc53267ee44ab0371e693c1296f1039bcaf51bb5802eef";
        boolean result = notificationServices.apnsInactiveDevices();
        assertTrue(result);
    }

    @Test
    public void testGetSetting() throws Exception {
        NotificationSetting result = notificationServices.getNotificationSetting(NOTIFICATION_SETTING_ID_CHAT);
        assertTrue(result.getMessage().contains("Chat"));
    }

    @Test
    public void testGetSettings() throws Exception {
        List notificationSettings = notificationServices.getNotificationSettings();
        assertTrue(notificationSettings.size() > 5);
    }

/*
    @Test
    public void testNotifyAboutChat() throws Exception {
        User coachtest1 = helper.getCoachTest1();
        User clienttest1 = helper.getClientTest1();
        ChatMessage chatMessage = helper.getMockChatMessageClient1ToCoach1();
        boolean result = notificationServices.notifyChat(coachtest1, clienttest1, chatMessage.toJson());
        assertTrue(result);
    }
*/

    @Test
    public void testNotifyAboutChatToCoach() throws Exception {
        User coachtest1 = helper.getCoachTest1();
        User clienttest1 = helper.getClientTest1();
//        ChatMessage chatMessage = helper.getMockChatMessageClient1ToCoach1();
        boolean result = notificationServices.processEvent(coachtest1.getUid(), clienttest1.getUid(),
                NOTIFICATION_SETTING_ID_CHAT, "FORUNITTESTS");
        assertTrue(result);
    }

    @Test
    public void testNotifyAboutChatToClient() throws Exception {
        User coachtest1 = helper.getCoachTest1();
        User clienttest1 = helper.getClientTest1();
        //gotta be a real chat message nowadays        ChatMessage chatMessage = helper.getMockChatMessageCoach1ToClient1();
        boolean result = notificationServices.processEvent(coachtest1.getUid(), clienttest1.getUid(),
                EVENT_CHAT_MESSAGE_SENT, "FORUNITTESTS");
        assertTrue(result);
    }

    @Test
    public void testNotifyAboutImageUpload() throws Exception {
        User coachtest1 = helper.getCoachTest1();
        User clienttest1 = helper.getClientTest1();
        boolean result = notificationServices.processEvent(clienttest1.getUid(), coachtest1.getUid(),
                NOTIFICATION_SETTING_ID_IMAGE, "");
        assertTrue(result);
    }

    @Test
    public void testNotifyAboutChatToCoachProposalException() throws Exception {
//        User coachtest1 = helper.getCoachTest1();
//        User clienttest1 = helper.getClientTest1();
        String json = "{\"clientId\":\"oofwbwv5pEVCwtc0YUuJPqVVbRF3\",\"coachId\":\"4ctkrKl5IyYmpU1Vs8toVGoTPL12\",\"id\":\"-KnzEc1aKWQse625SJie\",\"link\":\"\",\"object\":{\"chargeAmount\":210.4,\"datestamp\":\"2017-07-01T17:41:45.108Z\",\"discount\":6,\"fee\":200,\"id\":\"-KnzEby0NOZmOMedT_g8\",\"processingFee\":6.4,\"proposalNumber\":\"0183342\",\"qty\":2,\"recipientId\":\"oofwbwv5pEVCwtc0YUuJPqVVbRF3\",\"senderId\":\"4ctkrKl5IyYmpU1Vs8toVGoTPL12\",\"serviceFee\":10,\"services\":[\"24/7 Chat\",\"Workout Program\",\"Nutrition Plan\",\"Scheduled Check-ins\",\"Competition Prep\"],\"status\":\"pending\",\"time\":\"weeks\"},\"read\":\"\",\"recipientId\":\"oofwbwv5pEVCwtc0YUuJPqVVbRF3\",\"senderId\":\"4ctkrKl5IyYmpU1Vs8toVGoTPL12\",\"sent\":\"2017-07-01T17:41:45.394Z\",\"text\":\"\",\"type\":\"proposal\"}";
        ChatMessage chatMessage = ChatMessage.fromJson(json);
        assertTrue(chatMessage != null);
        //ChatMessage chatMessage = helper.getMockChatMessageClient1ToCoach1();
//        boolean result = notificationServices.processEvent(coachtest1.getUid(), clienttest1.getUid(),
//                Utilities.NOTIFICATION_SETTING_ID_CHAT, chatMessage.getId());
//        assertTrue(result);
    }

    @Test
    public void testNotifyALL() throws Exception {
        User coachtest1 = helper.getCoachTest1();
        User clienttest1 = helper.getClientTest1();
        ChatMessage chatMessage = helper.getMockChatMessageClient1ToCoach1();
        String recipient = chatMessage.getRecipientId();//coachtest1
        assertTrue(notificationServices.notifyChat(clienttest1, coachtest1, chatMessage));
        assertTrue(notificationServices.notifyImage(coachtest1, clienttest1, "{data : \"idunno\"}"));
        assertTrue(notificationServices.notifyNutirtion(coachtest1, clienttest1, "{data : \"idunno\"}"));
        assertTrue(notificationServices.notifyWorkout(coachtest1, clienttest1, "{data : \"idunno\"}"));
        assertTrue(notificationServices.notifyPayment(coachtest1, clienttest1, "{data : \"idunno\"}"));
        assertTrue(notificationServices.notifyClientProposal(coachtest1, clienttest1, "KnUMVPktG1gm9fI6X0e"));
    }

}
