/*
 * Copyright (c) 2016. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */
package com.heycoach.services;

import com.heycoach.model.AnalyticsSnapshot;
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

import static com.heycoach.services.NotificationServices.NOTIFICATION_SETTING_ID_CHAT;
import static com.heycoach.services.NotificationServices.NOTIFICATION_SETTING_ID_IMAGE;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:api-servlet.xml")
public class AnalyticsTests  extends TestCase {
    static {
        System.setProperty("HC_ENVIRONMENT", "PRODUCTION");
    }

    @Autowired
    private NotificationServices notificationServices;
    @Autowired EnvironmentServices environmentServices;
    @Autowired FirebaseClientServices firebaseClientServices;
    @Autowired Helper helper;
    @Autowired AnalyticServices analyticServices;


    @Before
    public void setup() {
        ErrorService.isDisabled = true;
    }


    @Test
    public void createSnapshotTest() throws Exception {
        //get a snapshot
        AnalyticsSnapshot result = analyticServices.gatherSnapshot();
        assertTrue(result != null);
//        assertTrue((Integer) result.getList().get("Coaches")  > 0);
//        assertTrue((Integer) result.getList().get("Clients")  > 0);
        System.out.println(result);
    }

    @Test
    public void saveSnapshotToDBTest() throws Exception {
        assertTrue(analyticServices.createDBSnapshot());
    }



}
