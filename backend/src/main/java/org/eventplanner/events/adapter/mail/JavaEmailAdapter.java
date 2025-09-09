package org.eventplanner.events.adapter.mail;

import java.util.Properties;

import org.apache.logging.log4j.util.Strings;
import org.eventplanner.events.application.ports.EmailSender;
import org.eventplanner.events.domain.entities.notifications.QueuedEmail;
import org.eventplanner.events.domain.values.config.EmailConfig;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import jakarta.mail.Address;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JavaEmailAdapter implements EmailSender {

    @Override
    public void sendEmail(@NonNull final QueuedEmail notification, @NonNull final EmailConfig emailConfig)
    throws Exception {
        if (Strings.isEmpty(emailConfig.username())) {
            throw new IllegalStateException("Email settings must contain all the required fields");
        }

        var mailSender = getMailSender(emailConfig);
        MimeMessage message = mailSender.createMimeMessage();

        var from = emailConfig.username();
        var fromDisplayName = emailConfig.username();
        var replyTo = emailConfig.username();
        var replyToDisplayName = emailConfig.username();
        if (emailConfig.from() != null) {
            from = emailConfig.from();
            fromDisplayName = emailConfig.from();
            replyTo = emailConfig.from();
            replyToDisplayName = emailConfig.from();
        }
        if (emailConfig.fromDisplayName() != null) {
            fromDisplayName = emailConfig.fromDisplayName();
            replyToDisplayName = emailConfig.fromDisplayName();
        }
        if (emailConfig.replyTo() != null) {
            replyTo = emailConfig.replyTo();
            replyToDisplayName = emailConfig.replyTo();
        }
        if (emailConfig.replyToDisplayName() != null) {
            replyToDisplayName = emailConfig.replyToDisplayName();
        }

        message.setFrom(new InternetAddress(from, fromDisplayName));
        message.setReplyTo(new Address[] { new InternetAddress(replyTo, replyToDisplayName) });
        message.setRecipients(RecipientType.TO, notification.getTo().trim());
        message.setSubject(notification.getSubject());
        message.setContent(notification.getBody(), "text/html; charset=utf-8");

        mailSender.send(message);
    }

    private @NonNull JavaMailSender getMailSender(@NonNull EmailConfig emailConfig) {
        var host = emailConfig.host();
        var port = emailConfig.port();
        var username = emailConfig.username();
        var password = emailConfig.password();

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
        javaMailProperties.put("mail.smtp.starttls.enable", emailConfig.enableStartTls());
        javaMailProperties.put("mail.smtp.ssl.enable", emailConfig.enableSSL());
        mailSender.setJavaMailProperties(javaMailProperties);
        return mailSender;
    }
}
