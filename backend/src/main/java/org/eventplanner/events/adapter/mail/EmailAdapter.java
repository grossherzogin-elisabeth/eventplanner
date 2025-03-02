package org.eventplanner.events.adapter.mail;

import java.util.Properties;

import org.apache.logging.log4j.util.Strings;
import org.eventplanner.events.application.ports.EmailSender;
import org.eventplanner.events.domain.entities.QueuedEmail;
import org.eventplanner.events.domain.values.Settings.EmailSettings;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import jakarta.mail.Address;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailAdapter implements EmailSender {

    @Override
    public void sendEmail(@NonNull final QueuedEmail notification, @NonNull final EmailSettings emailSettings)
    throws Exception {
        if (Strings.isEmpty(emailSettings.getUsername())) {
            throw new IllegalStateException("Email settings must contain all the required fields");
        }

        var mailSender = getMailSender(emailSettings);
        MimeMessage message = mailSender.createMimeMessage();

        var from = emailSettings.getUsername();
        var fromDisplayName = emailSettings.getUsername();
        var replyTo = emailSettings.getUsername();
        var replyToDisplayName = emailSettings.getUsername();
        if (emailSettings.getFrom() != null) {
            from = emailSettings.getFrom();
            fromDisplayName = emailSettings.getFrom();
            replyTo = emailSettings.getFrom();
            replyToDisplayName = emailSettings.getFrom();
        }
        if (emailSettings.getFromDisplayName() != null) {
            fromDisplayName = emailSettings.getFromDisplayName();
            replyToDisplayName = emailSettings.getFromDisplayName();
        }
        if (emailSettings.getReplyTo() != null) {
            replyTo = emailSettings.getReplyTo();
            replyToDisplayName = emailSettings.getReplyTo();
        }
        if (emailSettings.getReplyToDisplayName() != null) {
            replyToDisplayName = emailSettings.getReplyToDisplayName();
        }

        message.setFrom(new InternetAddress(from, fromDisplayName));
        message.setReplyTo(new Address[] { new InternetAddress(replyTo, replyToDisplayName) });
        message.setRecipients(MimeMessage.RecipientType.TO, notification.getTo().trim());
        message.setSubject(notification.getSubject());
        message.setContent(notification.getBody(), "text/html; charset=utf-8");

        mailSender.send(message);
    }

    private JavaMailSender getMailSender(@NonNull EmailSettings emailSettings) {
        var host = emailSettings.getHost();
        var port = emailSettings.getPort();
        var username = emailSettings.getUsername();
        var password = emailSettings.getPassword();

        if (username == null || username.isBlank()
            || password == null || password.isBlank()
            || host == null || host.isBlank()
            || port == null || port == 0
        ) {
            throw new IllegalStateException("Email settings must contain all the required fields");
        }

        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setProtocol("smtp");
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        final Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.auth", true);
        javaMailProperties.put("mail.smtp.starttls.enable", emailSettings.getEnableStartTls());
        javaMailProperties.put("mail.smtp.ssl.enable", emailSettings.getEnableSSL());
        mailSender.setJavaMailProperties(javaMailProperties);
        return mailSender;
    }
}
