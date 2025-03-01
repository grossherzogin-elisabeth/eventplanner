package org.eventplanner.events.application.services;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.util.Strings;
import org.eventplanner.events.application.ports.QueuedEmailRepository;
import org.eventplanner.events.domain.entities.QueuedEmail;
import org.eventplanner.events.domain.values.Notification;
import org.eventplanner.events.domain.values.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService implements NotificationDispatcher {
    private final List<String> recipientsWhitelist;
    private final SettingsService settingsService;
    private final QueuedEmailRepository queuedEmailRepository;

    public EmailService(
        @Autowired final SettingsService settingsService,
        @Autowired final QueuedEmailRepository queuedEmailRepository,
        @Value("${email.recipients-whitelist}") final String recipientsWhitelist
    ) {
        this.settingsService = settingsService;
        this.queuedEmailRepository = queuedEmailRepository;
        this.recipientsWhitelist = Arrays.stream(recipientsWhitelist.split(","))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .toList();
    }

    @Override
    public void dispatch(@NonNull final Notification notification) {
        var email = notification.recipient().getEmail();
        if (email == null) {
            log.warn(
                "Cannot send email notification to user {} because no email address is specified",
                notification.recipient().getKey()
            );
            return;
        }
        queuedEmailRepository.queue(new QueuedEmail(
            notification.type(),
            notification.recipient().getEmail().trim(),
            notification.recipient().getKey(),
            notification.title(),
            notification.content()
        ));
    }

    public void sendNextEmail() {
        var maybeNext = queuedEmailRepository.next();
        if (maybeNext.isPresent()) {
            var message = maybeNext.get();
            try {
                sendEmail(message);
            } catch (Exception e) {
                if (message.getRetries() > 10) {
                    log.error(
                        "Failed to send {} notification to user {} 10 times, this was the last retry",
                        message.getType(),
                        message.getUserKey(),
                        e
                    );
                    return;
                }
                log.warn(
                    "Failed to send {} notification to user {}, queuing notification for retry",
                    message.getType(),
                    message.getUserKey()
                );
                message.setRetries(message.getRetries() + 1);
                message.setCreatedAt(Instant.now());
                queuedEmailRepository.queue(message);
            }
        }
    }

    private void sendEmail(@NonNull QueuedEmail notification) throws MessagingException, UnsupportedEncodingException {
        if (!recipientsWhitelist.isEmpty() && !recipientsWhitelist.contains(notification.getTo())) {
            log.warn(
                "Skipped sending email to user {} because notifications are configured to only be sent to whitelisted" +
                " users",
                notification.getTo()
            );
            return;
        }

        var emailSettings = getEmailSettings();

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

        if (!recipientsWhitelist.isEmpty()) {
            log.info("Sending email to whitelisted recipient {}", notification.getUserKey());
        }
        mailSender.send(message);
        log.info("Successfully sent {} notification to user {}", notification.getType(), notification.getUserKey());
    }

    private Settings.EmailSettings getEmailSettings() {
        return settingsService.getSettings().emailSettings();
    }

    private JavaMailSender getMailSender(@NonNull Settings.EmailSettings emailSettings) {
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
