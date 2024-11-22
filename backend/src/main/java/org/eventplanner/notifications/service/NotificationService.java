package org.eventplanner.notifications.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMessage.RecipientType;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.notifications.values.Notification;
import org.eventplanner.notifications.values.NotificationType;
import org.eventplanner.positions.entities.Position;
import org.eventplanner.settings.service.SettingsService;
import org.eventplanner.settings.values.EmailSettings;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class NotificationService {
    private static final ZoneId timezone = ZoneId.of("Europe/Berlin");

    private final Configuration freemarkerConfig;
    private final SettingsService settingsService;
    private final String frontendDomain;
    private final List<String> recipientsWhitelist;

    public NotificationService(
            Configuration freemarkerConfig,
            SettingsService settingsService,
            @Value("${frontend.domain}") String frontendDomain,
            @Value("${email.recipients-whitelist}") String recipientsWhitelist) {
        this.freemarkerConfig = freemarkerConfig;
        this.settingsService = settingsService;
        this.frontendDomain = frontendDomain;
        this.recipientsWhitelist = Arrays.stream(recipientsWhitelist.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private EmailSettings getEmailSettings() {
        return settingsService.getSettings().emailSettings();
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

    public void sendAddedToWaitingListNotification(@NonNull UserDetails to, @NonNull Event event) {
        log.info("Sending added to waiting list notification to user {}", to.getEmail());
        Notification notification = new Notification(NotificationType.ADDED_TO_WAITING_LIST);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        notification.getProps().put("user", to);
        addEventDetails(notification, event);
        try {
            sendNotification(notification, to);
        } catch (Exception e) {
            log.error("Failed to send 'removed from waiting list' notification to user {}", to.getEmail(), e);
        }
    }

    public void sendRemovedFromWaitingListNotification(@NonNull UserDetails to, @NonNull Event event) {
        log.info("Sending removed from waiting list notification to user {}", to.getEmail());
        Notification notification = new Notification(NotificationType.REMOVED_FROM_WAITING_LIST);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        notification.getProps().put("user", to);
        addEventDetails(notification, event);
        try {
            sendNotification(notification, to);
        } catch (Exception e) {
            log.error("Failed to send 'removed from waiting list' notification to user {}", to.getEmail(), e);
        }
    }

    public void sendAddedToCrewNotification(@NonNull UserDetails user, @NonNull Event event) {
        log.info("Sending added to crew notification to user {}", user.getEmail());
        Notification notification = new Notification(NotificationType.ADDED_TO_CREW);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        addEventDetails(notification, event);
        notification.getProps().put("user", user);
        try {
            sendNotification(notification, user);
        } catch (Exception e) {
            log.error("Failed to send 'added to crew' notification to user {}", user.getEmail(), e);
        }
    }

    public void sendRemovedFromCrewNotification(@NonNull UserDetails to, @NonNull Event event) {
        log.info("Sending removed from crew notification to user {}", to.getEmail());
        Notification notification = new Notification(NotificationType.REMOVED_FROM_CREW);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        notification.getProps().put("user", to);
        addEventDetails(notification, event);
        try {
            sendNotification(notification, to);
        } catch (Exception e) {
            log.error("Failed to send 'removed from crew' notification to user {}", to.getEmail(), e);
        }
    }

    public void sendCrewRegistrationCanceledNotification(
            @NonNull UserDetails to,
            @NonNull Event event,
            @NonNull String userName,
            @NonNull String position
    ) {
        log.info("Sending crew registration canceled notification to user {}", to.getEmail());
        Notification notification = new Notification(NotificationType.CREW_REGISTRATION_CANCELED);
        notification.setTitle("Absage zu " + event.getName());
        addEventDetails(notification, event);
        notification.getProps().put("userName", userName);
        notification.getProps().put("position", position);
        try {
            sendNotification(notification, to);
        } catch (Exception e) {
            log.error("Failed to send 'crew registration canceled' notification to to {}", to.getEmail(), e);
        }
    }

    public void sendFirstParticipationConfirmationRequestNotification(
            @NonNull UserDetails to,
            @NonNull Event event,
            @NonNull Registration registration
    ) {
        log.info("Sending first participation confirmation request to user {}", to.getEmail());
        Notification notification = new Notification(NotificationType.CONFIRM_PARTICIPATION);
        notification.setTitle("Bitte um Rückmeldung: " + event.getName());
        addEventDetails(notification, event);

        sendParticipationNotificationBody(to, event, registration, notification);
    }

    public void sendSecondParticipationConfirmationRequestNotification(
            @NonNull UserDetails to,
            @NonNull Event event,
            @NonNull Registration registration
    ) {
        log.info("Sending second participation confirmation request to user {}", to.getEmail());
        Notification notification = new Notification(NotificationType.CONFIRM_PARTICIPATION_REQUEST);
        notification.setTitle("Bitte sofortige um Rückmeldung: " + event.getName());
        addEventDetails(notification, event);

        sendParticipationNotificationBody(to, event, registration, notification);
    }

    private void sendParticipationNotificationBody(
            @NonNull UserDetails to,
            @NonNull Event event,
            @NonNull Registration registration,
            @NonNull Notification notification
    ) {
        notification.getProps().put("user", to);
        notification.getProps().put("deadline", formatDate(event.getStart().atZone(timezone).minusDays(7)));
        var eventUrl = frontendDomain + "/events/" + event.getStart().atZone(timezone)
                .getYear() + "/details/" + event.getKey() + "/registrations/" + registration.getKey();
        notification.getProps().put("confirm_link", eventUrl + "/confirm?accessKey=" + registration.getAccessKey());
        notification.getProps().put("deny_link", eventUrl + "/deny?accessKey=" + registration.getAccessKey());
        try {
            sendNotification(notification, to);
        } catch (Exception e) {
            log.error("Failed to send participation confirmation request to to {}", to.getEmail(), e);
        }
    }

    private void addEventDetails(@NonNull Notification notification, @NonNull Event event) {
        notification.getProps().put("event", event);
        notification.getProps().put("event_start_date", formatDate(event.getStart().atZone(timezone)));
        notification.getProps().put("event_start_datetime", formatDateTime(event.getStart().atZone(timezone)));
        notification.getProps().put("event_end_date", formatDate(event.getEnd().atZone(timezone)));
        notification.getProps().put("event_end_datetime", formatDateTime(event.getStart().atZone(timezone)));
        notification.getProps().put("event_crew_on_board_datetime", formatDateTime(event.getStart().atZone(timezone)));
    }

    public void sendNotification(@NonNull Notification notification, @NonNull UserDetails to) throws IOException, TemplateException, MessagingException {
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
        message.setReplyTo(new Address[]{ new InternetAddress(replyTo, replyToDisplayName) });
        message.setRecipients(RecipientType.TO, to.getEmail());
        message.setSubject(notification.getTitle());

        notification.getProps().put("title", notification.getTitle());
        notification.getProps().put("app_link", frontendDomain);
        notification.getProps().put("user", to);
        notification.getProps().put("footer", emailSettings.getFooter());
        var htmlContent = renderEmail(notification);
        message.setContent(htmlContent, "text/html; charset=utf-8");

        if (!recipientsWhitelist.isEmpty()) {
            if (recipientsWhitelist.contains(to.getEmail())) {
                log.info("Sending {} email to whitelisted recipient {}", notification.getType(), to.getEmail());
                try (var exec = Executors.newSingleThreadExecutor()) {
                    exec.submit(() -> mailSender.send(message));
                    exec.shutdown();
                }
            } else {
                log.warn("Skipped sending email to {} because notifications are configured to only be sent to whitelisted users", to.getEmail());
            }
        } else {
            if (to.getAuthKey() != null) {
                log.info("Sending {} email to {}", notification.getType(), to.getEmail());
                try (var exec = Executors.newSingleThreadExecutor()) {
                    exec.submit(() -> mailSender.send(message));
                    exec.shutdown();
                }
            } else {
                log.warn("Skipped sending email to {} because notifications are configured to only be sent to beta users", to.getEmail());
            }
        }
    }

    private String renderEmail(@NonNull Notification notification) throws IOException, TemplateException {
        var props = notification.getProps();
        String content = renderTemplate("emails/" + notification.getType().toString() + ".ftl", props);

        var baseTemplateParams = new HashMap<>(notification.getProps());
        baseTemplateParams.put("content", content);
        return renderTemplate("partials/base.ftl", baseTemplateParams);
    }

    private String renderTemplate(@NonNull String template, @NonNull Object params) throws TemplateException, IOException {
        Template content = freemarkerConfig.getTemplate(template);
        Writer writer = new StringWriter();
        content.process(params, writer);
        return writer.toString();
    }

    private String formatDate(ZonedDateTime zonedDateTime) {
        var formatted = zonedDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        formatted = formatted.replace(".", "&#8203.");
        return formatted;
    }

    private String formatDateTime(ZonedDateTime zonedDateTime) {
        var formatted = zonedDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        formatted = formatted.replace(".", "&#8203.");
        return formatted;
    }
}
