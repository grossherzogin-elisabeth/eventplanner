package org.eventplanner.events.adapter.mail;

import java.util.Properties;

import org.apache.logging.log4j.util.Strings;
import org.eventplanner.events.application.ports.EmailSender;
import org.eventplanner.events.domain.entities.QueuedEmail;
import org.eventplanner.events.domain.values.settings.EmailSettings;
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
        if (Strings.isEmpty(emailSettings.username())) {
            throw new IllegalStateException("Email settings must contain all the required fields");
        }

        var mailSender = getMailSender(emailSettings);
        MimeMessage message = mailSender.createMimeMessage();

        var from = emailSettings.username();
        var fromDisplayName = emailSettings.username();
        var replyTo = emailSettings.username();
        var replyToDisplayName = emailSettings.username();
        if (emailSettings.from() != null) {
            from = emailSettings.from();
            fromDisplayName = emailSettings.from();
            replyTo = emailSettings.from();
            replyToDisplayName = emailSettings.from();
        }
        if (emailSettings.fromDisplayName() != null) {
            fromDisplayName = emailSettings.fromDisplayName();
            replyToDisplayName = emailSettings.fromDisplayName();
        }
        if (emailSettings.replyTo() != null) {
            replyTo = emailSettings.replyTo();
            replyToDisplayName = emailSettings.replyTo();
        }
        if (emailSettings.replyToDisplayName() != null) {
            replyToDisplayName = emailSettings.replyToDisplayName();
        }

        message.setFrom(new InternetAddress(from, fromDisplayName));
        message.setReplyTo(new Address[] { new InternetAddress(replyTo, replyToDisplayName) });
        message.setRecipients(MimeMessage.RecipientType.TO, notification.getTo().trim());
        message.setSubject(notification.getSubject());
        message.setContent(notification.getBody(), "text/html; charset=utf-8");

        mailSender.send(message);
    }

    private JavaMailSender getMailSender(@NonNull EmailSettings emailSettings) {
        var host = emailSettings.host();
        var port = emailSettings.port();
        var username = emailSettings.username();
        var password = emailSettings.password();

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
        javaMailProperties.put("mail.smtp.starttls.enable", emailSettings.enableStartTls());
        javaMailProperties.put("mail.smtp.ssl.enable", emailSettings.enableSSL());
        mailSender.setJavaMailProperties(javaMailProperties);
        return mailSender;
    }
}
