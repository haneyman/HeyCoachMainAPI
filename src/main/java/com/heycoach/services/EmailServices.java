package com.heycoach.services;

import com.google.api.client.util.Value;
import com.heycoach.model.Invitation;
import com.heycoach.model.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.regex.Pattern;

//bean defined in xml
@Service
@PropertySource("classpath:email.properties")
public class EmailServices {
    private static final Logger logger = Logger.getLogger(EmailServices.class);
    @Autowired FirebaseClientServices firebaseClientServices;

    @Value("${smtp.host}")
    private String smtpHost;
    @Value("${smtp.port}")
    private String smtpPort;
    @Value("${smtp.user}")
    private String smtpUser;
    @Value("${smtp.password}")
    private String smtpPassword;
    @Value("${smtp.from}")
    private String smtpFrom;



    //hacked the AWS SES properties in for now, can't get properties to work
    public boolean sendEmail(String body, String subject, String to)  {
        String[] tos = {to};
        try {
            return sendEmail( "email-smtp.us-west-2.amazonaws.com", "587", "AKIAIKYWEN4633BO5RTQ",
                    "AgPIkT+13mcZl1um7AO2wvYki825dVCXwx1nDFkU+Cp+", "support@heycoach.me", tos,subject,
                    body);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return false;
        } catch (MessagingException e) {
            e.printStackTrace();
            ErrorService.reportError(e);
            return false;
        }
    }

    public boolean sendEmail(String host, String port, String user, String password,
                             String from, String[] tos, String subject, String body)
            throws UnsupportedEncodingException, MessagingException {
        final String u = user;
        final String p = password;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        //props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(u, p);
                    }
                });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        for (String to: tos) {
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(to));
        }
        msg.setSubject(subject);
        msg.setContent(body,"text/html");
        Transport.send(msg);
        return true;
    }

    public String getInviteBody(User user, Invitation invitation) {
        StringBuffer body = new StringBuffer();
/*
        body.append("<div style='color:green'><h1>You've been invited to Heycoach.me!!</h1></div>");
        body.append("<br/><br/><div>Coach " + user.getFirstName() + " " + user.getLastName() + " has invited you to be his client.</div>");
        body.append("<br/><br/><br/>Your access code is <b>" + invitation.getAccessCode() + "</b>");
        body.append("<br/><br/>Install the Heycoach.me app and enter this access code to get started with your coach.");
        body.append("<br/><br/><a href='https://itunes.apple.com/us/app/tinder/id547702041?mt=8'>LINK TO APPLE</a> | <a href=''>LINK TO GOOGLE</a>");
*/

        String condition = firebaseClientServices.getCondition();
        String text;
        //get the email body,
        text = firebaseClientServices.getParameter(condition, "emailBody");
        // insert the message from the invitation
        String message = "Coach [lastName] has invited you to start your training on Heycoach";
        if (invitation.getMessage() != null && invitation.getMessage().length() > 0 ) {
            message = invitation.getMessage();
        }
        //removed this since not input in mobile app
        //        text = text.replaceAll(Pattern.quote("[message]"), message);
        text = text.replaceAll(Pattern.quote("[lastName]"), user.getLastName());
        text = text.replaceAll("\"",  "");
        body.append(text);
        body.append("\n");
        text = firebaseClientServices.getParameter(condition, "emailFooter");
        text = text.replaceAll(Pattern.quote("[accessCode]"), invitation.getAccessCode());
        text = text.replaceAll("\"",  "");
        body.append(text);
        return body.toString();
    }

    public String getInviteBodyReminder(User coach, Invitation invitation) {
        StringBuffer body = new StringBuffer();
        String condition = firebaseClientServices.getCondition();
        String text;
        text = firebaseClientServices.getParameter(condition, "emailBodyReminder");
        text = text.replaceAll(Pattern.quote("[lastName]"), coach.getLastName());
        text = text.replaceAll("\"",  "");
        body.append(text);
        body.append("\n");
        text = firebaseClientServices.getParameter(condition, "emailFooter");
        text = text.replaceAll(Pattern.quote("[accessCode]"), invitation.getAccessCode());
        text = text.replaceAll("\"",  "");
        body.append(text);

        return body.toString();
    }

    public String getInviteSubject(User user, Invitation invitation) {
        String subject;
        String condition = firebaseClientServices.getCondition();
        subject = firebaseClientServices.getParameter(condition, "emailSubject");
        subject = subject.replaceAll(Pattern.quote("[lastName]"), user.getLastName());
        subject = subject.replaceAll("\"",  "");
        return subject;
    }

    public String getInviteSubjectReminder(User user, Invitation invitation) {
        String subject;
        String condition = firebaseClientServices.getCondition();
        subject = firebaseClientServices.getParameter(condition, "emailSubjectReminder");
        subject = subject.replaceAll(Pattern.quote("[lastName]"), user.getLastName());
        subject = subject.replaceAll("\"",  "");
        return subject;
    }

}
