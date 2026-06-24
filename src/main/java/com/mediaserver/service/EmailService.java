package com.mediaserver.service;

import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String body) {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                // This is the authenticated user
                return new PasswordAuthentication("kmpsdi@gmail.com", "gyaosqflrickbfxd ");
            }
        };

        Session session = Session.getInstance(props, auth);

        try {
            MimeMessage msg = new MimeMessage(session);
            // The "From" address now matches the authenticated user
            msg.setFrom(new InternetAddress("kmpsdi@gmail.com", "KMPS Digital-Signage Server"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            msg.setSubject(subject, "UTF-8");
            msg.setText(body, "UTF-8");
            msg.setSentDate(new java.util.Date());

            Transport.send(msg);

            System.out.println("Email sent successfully!");
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("Error while sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Suspension notification methods
    public void sendSuspensionNotification(String toEmail, String userName, String reason) {
        String subject = "Account Suspension Notice";
        String body = String.format(
            "Dear %s,\n\n" +
            "Your account has been suspended due to the following reason:\n\n" +
            "%s\n\n" +
            "If you believe this suspension is in error or would like to appeal this decision, " +
            "please contact our support team.\n\n" +
            "Best regards,\n" +
            "KMPS Digital-Signage Server Team",
            userName, reason
        );

        sendEmail(toEmail, subject, body);
    }

    public void sendUnsuspensionNotification(String toEmail, String userName) {
        String subject = "Account Reactivation Notice";
        String body = String.format(
            "Dear %s,\n\n" +
            "Your account has been reactivated and you can now access all services.\n\n" +
            "If you have any questions, please contact our support team.\n\n" +
            "Best regards,\n" +
            "KMPS Digital-Signage Server Team",
            userName
        );

        sendEmail(toEmail, subject, body);
    }

    public void sendInactivityNotification(String toEmail, String userName) {
        String subject = "Account Suspended Due to Inactivity";
        String body = String.format(
            "Dear %s,\n\n" +
            "Your account has been suspended due to prolonged inactivity (more than 3 months without login).\n\n" +
            "To reactivate your account, please contact our support team.\n\n" +
            "Best regards,\n" +
            "KMPS Digital-Signage Server Team",
            userName
        );

        sendEmail(toEmail, subject, body);
    }
}
