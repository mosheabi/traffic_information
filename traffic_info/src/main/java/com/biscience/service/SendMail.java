package com.biscience.service;

/**
 * Created by Anna Kuranda on 6/20/2017.
 */
import com.biscience.TrafficInfoProperties;
import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendMail {
    private static Logger logger = Logger.getLogger(SendMail.class);

    public void send(String subject, String msg){
        try{
            String to = TrafficInfoProperties.MAIL_TO.getValue();
            String from = TrafficInfoProperties.MAIL_FROM.getValue();
            String host = TrafficInfoProperties.MAIL_SMTP_HOST.getValue().trim();
            String port = TrafficInfoProperties.MAIL_SMTP_PORT.getValue().trim();
            String user = TrafficInfoProperties.MAIL_USER.getValue().trim();
            String pass = TrafficInfoProperties.MAIL_PASS.getValue().trim();

            Properties properties = System.getProperties();

            // Setup mail server
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);


            // Get the default Session object.
            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(user, pass);
                        }
                    });


            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            InternetAddress[] addresses = InternetAddress.parse(to);

            // Set To: header field of the header.
            message.addRecipients(Message.RecipientType.TO, addresses);

            // Set Subject: header field
            message.setSubject(subject);

            // Now set the actual message
            message.setText(msg);

            // Send message
            Transport.send(message);
            logger.debug("Sent message successfully....");
        }catch(Exception e){
            logger.error("Failed send mail "+e);
        }

    }

    public static void main(String [] args) {
        // Recipient's email ID needs to be mentioned.
        String to = "abcd@gmail.com";

        // Sender's email ID needs to be mentioned
        String from = "web@gmail.com";

        // Assuming you are sending email from localhost
        String host = "localhost";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("This is the Subject Line!");

            // Now set the actual message
            message.setText("This is actual message");

            // Send message
            Transport.send(message);
            System.out.println("Sent message successfully....");
        }catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}