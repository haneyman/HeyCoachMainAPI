/*
 * Copyright (c) 2016. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */
package com.heycoach.services;

        import com.heycoach.model.Invitation;
        import com.heycoach.model.User;
        import org.apache.log4j.Logger;
        import org.junit.Before;
        import org.junit.Test;
        import org.junit.runner.RunWith;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.test.context.ContextConfiguration;
        import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
        import org.springframework.test.context.web.WebAppConfiguration;

        import static junit.framework.TestCase.assertFalse;
        import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:api-servlet.xml")
public class FirebaseClientServicesTests {
    private static final Logger logger = Logger.getLogger(FirebaseClientServicesTests.class);

    @Autowired FirebaseClientServices firebaseClientServices;
    @Autowired EnvironmentServices environmentServices;

    @Before
    public void setup() {
//        environmentServices.setHc_environment("DEVELOPMENT");
//        environmentServices.setHc_environment("QA");
        environmentServices.setHc_environment("PRODUCTION");
    }

    @Test
    public void testUpdateUser() throws Exception {
        firebaseClientServices.updateUserStatus("2G2TCAiEf2bDxSHe69l4OICLBS83", "test");
    }

    @Test
    public void testGetParameter() throws Exception {
        String condition = firebaseClientServices.getCondition();
        assertTrue(condition.equalsIgnoreCase("1"));

        String result = getParam("smsBody");
        assertTrue(result.contains("has invited you to start"));

        result = getParam("smsFooter");
        assertTrue(result.contains("To accept this invite"));
//        assertFalse(result.contains("["));
    }

    private String getParam(String parm) {
        String result = firebaseClientServices.getParameter(firebaseClientServices.getCondition(), parm);
        logger.debug(" parameter " + parm + ": " + result);
        return result;
    }

    @Test
    public  void testCreateInviteCrossIndex() {
        Invitation inv = new Invitation();
        inv.setId("theInvId");
        inv.setAccessCode(Utilities.getRandomAlphaNumeric(6));
        User coach = new User();
        coach.setUserId("theCoachId");
        firebaseClientServices.createInviteCrossIndex(inv, coach);
    }

    @Test
    public void testClearApplicationLog() {
        firebaseClientServices.clearApplicationLog();
    }

    @Test
    public void testUpdateInvitation() {
        Invitation inv = Helper.getInvitation();
        firebaseClientServices.updateInvitation("vgIjAsNO6BSWnp4xsBuGk4977Wy1", "-KhJa4JzondArssOwNBa", inv);
    }


}