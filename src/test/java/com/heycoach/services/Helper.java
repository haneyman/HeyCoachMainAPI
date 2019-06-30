/*
 * Copyright (c) 2017. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */

package com.heycoach.services;

import com.heycoach.model.ChatMessage;
import com.heycoach.model.Invitation;
import com.heycoach.model.User;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@Service
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:api-servlet.xml")
public class Helper {
    @Autowired UserServices userServices;

    public static User getTestCoach() {
        return new User("US", "usd", "test@markhaney.net", "TestLast", "TestFirst",
                "2017-04-06T15:31:00.413Z", "sometype", "pending", "coach", "coachtest", "", false,
                null, "#979797", "");
    }

    public static User getTestClientViaClone(String suffix,  String status, User user) {
        user.setFirstName(user.getFirstName() + suffix);
        user.setLastName(user.getLastName() + suffix);
        user.setUserId(user.getUserId() + suffix);
        user.setStatus(status);
        return user;
    }

    public static User getTestClientForCoach(String prefix, String status, String coach) {
        User user = getTestClientTemplate();
        user.setFirstName(user.getFirstName() + prefix);
        user.setLastName(user.getLastName() + prefix);
        user.setUserId(user.getUserId() + prefix);
        user.setStatus(status);
        user.setCoachId(coach);
        return user;
    }
    public static User getTestClientTemplate() {
        User user = new User("US", "usd","test@markhaney.net", "TestLast", "TestFirst",
                "2017-04-06T15:31:00.413Z", "sometype", "pending", "client", "coachtest", "", false,
                null, "#979797", "");
        return user;
    }

    public static Invitation getInvitation() {
        return new Invitation("T77OXT", "", "2017-04-06T15:31:00.413Z", "-KhJa4JzondArssOwNBa",
               "Coach Coach has invited you to start your training on HeyCoach",
                "test9@markhaney.net", "", "2017-04-09T21:22:07.270Z", "sent");
    }

    public User getCoachTest1() {
        return userServices.getUserByUsername("coachtest1");
    }

    public User getClientTest1() {
        return userServices.getUserByUsername("clienttest1");
    }

    public ChatMessage getMockChatMessageClient1ToCoach1() {
        User coach1 = userServices.getUserByUsername("coachtest1");
        User client1 = userServices.getUserByUsername("clienttest1");
        return new ChatMessage("-Knu2PaRa7pzK5QZ3pzk", "Chat message from client1 to coach1.", "", client1.getUid(),
                coach1.getUid(), coach1.getUid(), client1.getUid(), "standard", null,
                "readdate", "sentdate");
    }

    public ChatMessage getMockChatMessageCoach1ToClient1() {
        User coach1 = userServices.getUserByUsername("coachtest1");
        User client1 = userServices.getUserByUsername("clienttest1");
        return new ChatMessage("someid", "Chat message to client.", "", client1.getUid(),
                coach1.getUid(), client1.getUid(), coach1.getUid(), "standard", null,
                "readdate", "sentdate");
    }
}
