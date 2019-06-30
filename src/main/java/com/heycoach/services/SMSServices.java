package com.heycoach.services;

import com.heycoach.model.Invitation;
import com.heycoach.model.User;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

//bean defined in xml
@Service
public class SMSServices {
    private static final Logger logger = Logger.getLogger(SMSServices.class);

    private String smtpFrom;
    @Autowired FirebaseClientServices firebaseClientServices;

    public SMSServices() {

    }

    public static final String ACCOUNT_SID_PROD = "ACe1d6670983338f542ac0bba7e87f64f6";//prod
    public static final String ACCOUNT_SID_TEST = "ACbfe755102e474594f786c3edc0454bf0";//test
    public static final String AUTH_TOKEN_PROD = "50b34bb35130b2a1bb4e1b8f561add58";//prod
    public static final String AUTH_TOKEN_TEST = "8cd08409232950132f514dcd0b9e1b2d";//test
    public static final String PHONE_FROM = "+19252926224";

    public boolean send(String body, String toNumber) {
        Twilio.init(ACCOUNT_SID_PROD, AUTH_TOKEN_PROD);
        Message message = Message.creator(new PhoneNumber(toNumber),
                new PhoneNumber(PHONE_FROM),body).create();
        logger.debug(message.getSid());

        return true;
    }

    /*
        public static void v2() {
            // Find your Account Sid and Token at twilio.com/user/account
            String ACCOUNT_SID = "ACe1d6670983338f542ac0bba7e87f64f6";
            String AUTH_TOKEN = "50b34bb35130b2a1bb4e1b8f561add58";
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            Message sms = Message.creator(new PhoneNumber("+16504524852"), new PhoneNumber("+19252926224"),
                    "All in the game, yo").create();

            logger.debug(sms.getSid());
        }
    */


    public String getInviteTextBody(User coach, Invitation invitation) {
        StringBuffer body = new StringBuffer();
        String condition = firebaseClientServices.getCondition();
        String text;
        if (invitation.getMessage() != null && invitation.getMessage().length() > 0 ) {
            text = invitation.getMessage();
        } else {
            text = firebaseClientServices.getParameter(condition, "smsBody");

            text = text.replaceAll(Pattern.quote("[lastName]"), coach.getLastName());
            text = text.replaceAll("\"",  "");
        }
        body.append(text);
        body.append("\n");
        text = firebaseClientServices.getParameter(condition, "smsFooter");
        text = text.replaceAll(Pattern.quote("[accessCode]"), invitation.getAccessCode());
        text = text.replaceAll("\"",  "");
        body.append(text);
        return body.toString();
    }

    public String getInviteTextReminderBody(User coach, Invitation invitation) {
        StringBuffer body = new StringBuffer();
        String condition = firebaseClientServices.getCondition();
        String text = invitation.getMessage();//body is actually coming from invitation
        if (text == null || text.length() == 0 )
            text = firebaseClientServices.getParameter(condition, "smsReminderBody");
        text = text.replaceAll(Pattern.quote("[lastName]"), coach.getLastName());
        text = text.replaceAll("\"",  "");
        body.append(text);
        body.append("\n");
        text = firebaseClientServices.getParameter(condition, "smsFooter");
        text = text.replaceAll(Pattern.quote("[accessCode]"), invitation.getAccessCode());
        text = text.replaceAll("\"",  "");
        body.append(text);
        return body.toString();
    }


}
