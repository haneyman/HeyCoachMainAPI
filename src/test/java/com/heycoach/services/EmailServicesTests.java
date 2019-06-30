/*
 * Copyright (c) 2016. HeyCoach.me & Mark Haney - All Rights Reserved
 *  You may NOT use, distribute nor modify this code.
 */
package com.heycoach.services;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

//@RunWith(SpringJUnit4ClassRunner.class)
//@WebAppConfiguration
//@ContextConfiguration({"file: src/main/webapp/WEB-INF/web.xml", "file: /src/main/webapp/WEB-INF/api-servlet.xml"})

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = "classpath:api-servlet.xml")
public class EmailServicesTests  extends TestCase {
    static {
        System.setProperty("HC_ENVIRONMENT", "DEVELOPMENT");
    }

    @Autowired EmailServices emailServices;
    @Autowired EnvironmentServices environmentServices;
/*
    @Autowired FirebaseClientServices firebaseClientServices;
    @Autowired Helper helper;
*/


    @Before
    public void setup() {
        ErrorService.isDisabled = true;
    }

//    @Test
//    public void testEmailInvite() throws Exception {
//        emailServices.
//    }

    @Test
    public void testEmail() throws Exception {
//        emailServices.sendMail("mark@markhaney.net", "mark@markhaney.net", "test subject", "test body, did it work?  Don't worry html email will be in the next version.");
        String[] tos = {"mark@markhaney.net"};

        emailServices.sendEmail( "email-smtp.us-west-2.amazonaws.com", "587", "AKIAIKYWEN4633BO5RTQ",
                "AgPIkT+13mcZl1um7AO2wvYki825dVCXwx1nDFkU+Cp+", "support@heycoach.me", tos,"test subject",
                "<h1>test body, did it work</h1> <div style=\"color:orange\">Don't worry html email will be in the next version.</div>");


/*
        emailServices.sendEmail( "smtp.1and1.com", "587", "mark@markhaney.net",
                "", "support@heycoach.me", tos,"test subject",
                "test body, did it work?  Don't worry html email will be in the next version.");
*/

/*
        emailServices.sendEmail( "smtp.gmail.com", "587","support@heycoach.me",
                "TeamHeycoach2017", "support@heycoach.me", tos,"test subject",
                "<h1>test header</h1>  Don't worry html email will be in the next version.");
*/
    }

}
