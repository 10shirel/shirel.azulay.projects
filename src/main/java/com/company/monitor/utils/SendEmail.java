package com.company.monitor.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Component
public class SendEmail {

  @Autowired
  EmailConfiguration emailConfiguration;

  public static final String MAIL_SMTP_AUTH = " mail.smtp.auth";
  public static final String MAIL_SMTP_STARTTLS_ENABLED = "mail.smtp.starttls.enable";
  public static final String MAIL_SMTP_HOST = "mail.smtp.host";
  public static final String MAIL_SMTP_PORT = "mail.smtp.port";


  public void sendMail(String toAdress, String subject, String body) {
    Properties props = new Properties();
    props.put(MAIL_SMTP_AUTH, emailConfiguration.getMailSmtpAuth());
    props.put(MAIL_SMTP_STARTTLS_ENABLED, emailConfiguration.getMailSmtpStarttlsEnabled());
    props.put(MAIL_SMTP_HOST, emailConfiguration.getMailSmtpHost());
    props.put(MAIL_SMTP_PORT, emailConfiguration.getMailSmtpPort());

    // Get the Session object.
    Session session = Session.getInstance(props,
                                          new javax.mail.Authenticator() {
                                            protected PasswordAuthentication getPasswordAuthentication() {
                                              return new PasswordAuthentication(emailConfiguration.userName, emailConfiguration.getPassword());
                                            }
                                          });

    try {
      // Create a default MimeMessage object.
      Message message = new MimeMessage(session);

      // Set From: header field of the header.
      message.setFrom(new InternetAddress(emailConfiguration.getFromAdress()));

      // Set To: header field of the header.
      message.setRecipients(Message.RecipientType.TO,
                            InternetAddress.parse(toAdress != null ? toAdress : emailConfiguration.getToAdress()));

      // Set Subject: header field
      message.setSubject(subject);

      // Now set the actual message
      message.setText(body);

      // Send message
      Transport.send(message);

      System.out.println("Sent message successfully....");

    } catch (MessagingException e) {
      throw new RuntimeException(e);
    }
  }
}
