package org.eventplanner.events.application.services;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.HashMap;

import org.eventplanner.events.application.ports.EmailSender;
import org.eventplanner.events.application.ports.QueuedEmailRepository;
import org.eventplanner.events.domain.entities.QueuedEmail;
import org.eventplanner.events.domain.values.GlobalNotification;
import org.eventplanner.events.domain.values.Notification;
import org.eventplanner.events.domain.values.PersonalNotification;
import org.eventplanner.events.domain.values.settings.EmailSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService implements NotificationDispatcher {
    private final ConfigurationService configurationService;
    private final UserService userService;
    private final QueuedEmailRepository queuedEmailRepository;
    private final EmailSender emailSender;
    private final Configuration freemarkerConfig;

    public EmailService(
        @Autowired final ConfigurationService configurationService,
        @Autowired final UserService userService,
        @Autowired final QueuedEmailRepository queuedEmailRepository,
        @Autowired final EmailSender emailSender,
        @Autowired final Configuration freemarkerConfig
    ) {
        this.configurationService = configurationService;
        this.userService = userService;
        this.queuedEmailRepository = queuedEmailRepository;
        this.emailSender = emailSender;
        this.freemarkerConfig = freemarkerConfig;
    }

    @Override
    public void dispatch(@NonNull final Notification notification) {
        if (notification instanceof PersonalNotification personalNotification) {
            queueEmail(personalNotification);
        } else if (notification instanceof GlobalNotification globalNotification) {
            userService.getUsersByRole(globalNotification.recipients())
                .stream()
                .map(globalNotification::toPersonalNotification)
                .forEach(this::queueEmail);
        }
    }

    private void queueEmail(@NonNull final PersonalNotification notification) {
        try {
            var email = notification.recipient().getEmail();
            if (email == null) {
                log.warn(
                    "Cannot send email notification to user {} because no email address is specified",
                    notification.recipient().getKey()
                );
                return;
            }
            var emailBody = renderEmailContent(notification);
            queuedEmailRepository.queue(new QueuedEmail(
                notification.type(),
                notification.recipient().getEmail().trim(),
                notification.recipient().getKey(),
                notification.title(),
                emailBody
            ));
        } catch (Exception e) {
            log.error(
                "Failed to queue '{}' notification for user {}",
                notification.type(),
                notification.recipient().getKey(),
                e
            );
        }
    }

    public void sendNextEmail() {

        var maybeNext = queuedEmailRepository.next();
        if (maybeNext.isPresent()) {
            var config = configurationService.getConfig().email();
            var message = maybeNext.get();
            if (config.titlePrefix() != null && !config.titlePrefix().isEmpty()) {
                message = message.withSubject(config.titlePrefix() + message.getSubject());
            }

            if (!Boolean.TRUE.equals(config.enabled())) {
                log.info(
                    "Skipped sending {} notification email to user {}, because email notifications are disabled",
                    message.getType(),
                    message.getUserKey()
                );
                return;
            }

            try {
                if (config.recipientsWhitelist() == null || config.recipientsWhitelist().isEmpty()) {
                    log.info("Sending {} notification email to user {}", message.getType(), message.getUserKey());
                } else if (config.recipientsWhitelist().contains(message.getTo())) {
                    log.info(
                        "Sending {} notification email to whitelisted recipient {}",
                        message.getType(),
                        message.getTo()
                    );
                } else {
                    log.warn(
                        "Skipped sending {} notification email to user {}, because user is not whitelisted",
                        message.getType(),
                        message.getUserKey()
                    );
                    return;
                }
                emailSender.sendEmail(message, getEmailSettings());
            } catch (Exception e) {
                if (message.getRetries() >= 10) {
                    log.error(
                        "Failed to send {} notification email to user {} 10 times, this was the last retry",
                        message.getType(),
                        message.getUserKey(),
                        e
                    );
                    return;
                }
                log.warn(
                    "Failed to send {} notification email to user {}, queuing email for retry",
                    message.getType(),
                    message.getUserKey()
                );
                message.setRetries(message.getRetries() + 1);
                message.setCreatedAt(Instant.now());
                queuedEmailRepository.queue(message);
            }
        }
    }

    private EmailSettings getEmailSettings() {
        return configurationService.getConfig().email();
    }

    protected String renderEmailContent(@NonNull PersonalNotification notification)
    throws TemplateException, IOException {
        var model = new HashMap<String, Object>();
        model.put("user", notification.recipient());
        model.put("content", notification.content());
        model.put("title", notification.title());

        var content = freemarkerConfig.getTemplate("partials/base.ftl");
        var writer = new StringWriter();
        content.process(model, writer);
        return writer.toString();
    }
}
