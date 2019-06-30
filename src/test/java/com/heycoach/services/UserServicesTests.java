/*
 * Copyright (c) 2016. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */
package com.heycoach.services;

import com.heycoach.model.ServiceResult;
import com.heycoach.model.User;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static junit.framework.TestCase.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:api-servlet.xml")
public class UserServicesTests {
    static {
        System.setProperty("HC_ENVIRONMENT", "DEVELOPMENT");
    }
    private static final Logger logger = Logger.getLogger(UserServicesTests.class);

    @Autowired
    private UserServices userServices;
    @Autowired EnvironmentServices environmentServices;

    @Before
    public void setup() {
        environmentServices.setHc_environment("PRODUCTION");
//        environmentServices.setHc_environment("DEVELOPMENT");
//        environmentServices.setHc_environment("QA");
    }

    @Test
    public void testGetEmailForUser() throws Exception {
        String email = userServices.getEmailforUsername("coach1@aol.com");
        assertTrue(email != null);
    }

/*
    @Test
    public void testAddUser() throws Exception {
        User user = Helper.getTestCoach();
        ServiceResult result = userServices.addUser(user);
        String uid = result.getData().getString("name");
        assertTrue(!result.getResult().equalsIgnoreCase("error"));
        assertTrue(uid != null);
        assertTrue(uid.length() > 5);
        logger.debug("new user:" + uid);
    }
*/

    @Test
    public void testAddThenDeleteUser() throws Exception {
        //add a user
        User user = Helper.getTestCoach();
        ServiceResult result = userServices.addUser(user);
        String uid = result.getData().getString("name");
        assertTrue(!result.getResult().equalsIgnoreCase("error"));
        assertTrue(uid != null);
        assertTrue(uid.length() > 5);

        //delete them
        result = userServices.deleteUser(uid);
        assertTrue(!result.getResult().equalsIgnoreCase("error"));
    }

    @Test
    public void testGetUsers() throws Exception {
        ServiceResult result = userServices.getUsers();
        logger.debug("results:" + result.getResponseToClient());
        assertTrue("length over 100",result.getResponseToClient().length() > 100 );
        assertTrue("contains lastName",result.getResponseToClient().contains("lastName"));
        assertTrue("contains status", result.getResponseToClient().contains("status"));
    }


    @Test
    public void testAddABunchOfClonedUsers() throws Exception {
        User user;
        User newUser;
        int numberToCreate = 3;
        for (int i = 0; i < numberToCreate; i++) {
            String prefix = "-PND" + i;
            user = userServices.getUserByUID("HVWOKQ4TkmhDZkKYF1C5MdnrjqN2");
            newUser = Helper.getTestClientViaClone(prefix, "pending", user);
            ServiceResult result = userServices.addUser(newUser);
        }
        for (int i = 0; i < numberToCreate; i++) {
            String prefix = "-PD" + i;
            user = userServices.getUserByUID("HVWOKQ4TkmhDZkKYF1C5MdnrjqN2");
            newUser = Helper.getTestClientViaClone(prefix, "paid", user);
            ServiceResult result = userServices.addUser(newUser);
        }
        for (int i = 0; i < numberToCreate; i++) {
            String prefix = "-ACT" + i;
            user = userServices.getUserByUID("HVWOKQ4TkmhDZkKYF1C5MdnrjqN2");
            newUser = Helper.getTestClientViaClone(prefix, "active", user);
            ServiceResult result = userServices.addUser(newUser);
        }
    }

    @Test
    public void testGetEmailByUsername() {
        String email = userServices.getEmailforUsername("coachtest1");
        assertTrue(email != null);
        assertTrue(email.length() > 0);
        assertTrue(email.contains("coachtest1@"));
    }
}
