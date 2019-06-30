
package com.heycoach.services;


import com.heycoach.model.Invitation;
import com.heycoach.model.User;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:api-servlet.xml")
//@ImportResource("classpath:email.properties")
public class SMSServicesTests  extends TestCase {
    private static final Logger logger = Logger.getLogger(SMSServicesTests.class);

    @Autowired
    private SMSServices smsServices;

    User coach;
    Invitation invitation;
    @Autowired EnvironmentServices environmentServices;


    @Before
    public void beforeStuff() {
        coach = Helper.getTestCoach();
        coach.setLastName("CoachLastname");

        invitation = new Invitation();
        invitation.setAccessCode("XYZ987");

        //        environmentServices.setHc_environment("DEVELOPMENT");
        environmentServices.setHc_environment("QA");
    }

//https://www.twilio.com/console/sms/dashboard

    @Test
    public void testSendSMS() throws Exception {
        String KC =  "+14087101170";
        String MPH = "+19254085980";
        assertTrue(smsServices.send("You've been invited by Coach Foo to Heycoach!  Use access code \"ABC123\" " +
                "and go to http://heycoach.me/invite.  You don't even lift bro!", MPH));
    }

    @Test
    public void testGetInviteTextBody() {
        String body = smsServices.getInviteTextBody(coach, invitation);
        logger.debug("body: " + body);
        assertTrue(body.contains(coach.getLastName() + " has invited you"));
        assertTrue(body.contains("and use code XYZ987"));
    }

    @Test
    public void testGetInviteRemoinderTextBody() {
        String body = smsServices.getInviteTextReminderBody(coach, invitation);
        logger.debug("body: " + body);
        assertTrue(body.contains("invited to Heycoach by Coach " + coach.getLastName()));
        assertTrue(body.contains("and use code XYZ987"));
    }


}