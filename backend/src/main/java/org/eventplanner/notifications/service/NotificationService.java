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
import org.eventplanner.settings.service.SettingsService;
import org.eventplanner.settings.values.EmailSettings;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class NotificationService {
    private static final ZoneId timezone = ZoneId.of("Europe/Berlin");
    private final Configuration freemarkerConfig;
    private final SettingsService settingsService;
    private final String frontendDomain;
    private final List<String> recipientsWhitelist;
    private final List<String> admins;
    private final UserService userService;

    public NotificationService(
            Configuration freemarkerConfig,
            SettingsService settingsService,
            @Value("${frontend.domain}") String frontendDomain,
            @Value("${email.recipients-whitelist}") String recipientsWhitelist,
            @Value("${auth.admins}") String admins,
            UserService userService
    ) {
        this.freemarkerConfig = freemarkerConfig;
        this.settingsService = settingsService;
        this.frontendDomain = frontendDomain;
        this.recipientsWhitelist = Arrays.stream(recipientsWhitelist.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
        this.admins = Arrays.stream(admins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
        this.userService = userService;
    }

    private EmailSettings getEmailSettings() {
        return settingsService.getSettings().emailSettings();
    }

    private JavaMailSender getMailSender(EmailSettings emailSettings) {
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

    public void sendAddedToWaitingListNotification(UserDetails user, Event event) {
        Notification notification = new Notification(NotificationType.ADDED_TO_WAITING_LIST);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        notification.getProps().put("user", user);
        notification.getProps().put("event", event);
        notification.getProps().put("event_start_date", event.getStart().atZone(timezone)
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        notification.getProps().put("event_end_date", event.getEnd().atZone(timezone)
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        try {
            sendNotification(notification, user);
        } catch (Exception e) {
            log.error("Failed to send 'removed from waiting list' notification to user {}", user.getEmail(), e);
        }
    }

    public void sendRemovedFromWaitingListNotification(UserDetails user, Event event) {
        Notification notification = new Notification(NotificationType.REMOVED_FROM_WAITING_LIST);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        notification.getProps().put("user", user);
        notification.getProps().put("event", event);
        notification.getProps().put("event_start_date", event.getStart().atZone(timezone)
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        notification.getProps().put("event_end_date", event.getEnd().atZone(timezone)
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        try {
            sendNotification(notification, user);
        } catch (Exception e) {
            log.error("Failed to send 'removed from waiting list' notification to user {}", user.getEmail(), e);
        }
    }

    public void sendAddedToCrewNotification(UserDetails user, Event event) {
        Notification notification = new Notification(NotificationType.ADDED_TO_CREW);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        notification.getProps().put("user", user);
        notification.getProps().put("event", event);
        notification.getProps().put("event_start_date", event.getStart().atZone(timezone)
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        notification.getProps().put("event_end_date", event.getEnd().atZone(timezone)
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        try {
            sendNotification(notification, user);
        } catch (Exception e) {
            log.error("Failed to send 'added to crew' notification to user {}", user.getEmail(), e);
        }
    }

    public void sendRemovedFromCrewNotification(UserDetails user, Event event) {
        Notification notification = new Notification(NotificationType.REMOVED_FROM_CREW);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        notification.getProps().put("user", user);
        notification.getProps().put("event", event);
        notification.getProps().put("event_start_date", event.getStart().atZone(timezone)
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        notification.getProps().put("event_end_date", event.getEnd().atZone(timezone)
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        try {
            sendNotification(notification, user);
        } catch (Exception e) {
            log.error("Failed to send 'removed from crew' notification to user {}", user.getEmail(), e);
        }
    }

    public void sendParticipationConfirmationNotification(UserDetails user, Event event, Registration registration) {
        Notification notification = new Notification(NotificationType.CONFIRM_PARTICIPATION);
        notification.setTitle("Bitte um Rückmeldung: " + event.getName());
        sendParticipationNotificationBody(user, event, registration, notification);
    }

    public void sendParticipationConfirmationNotificationRequest(UserDetails user, Event event, Registration registration) {
        Notification notification = new Notification(NotificationType.CONFIRM_PARTICIPATION_REQUEST);
        notification.setTitle("Bitte sofortige um Rückmeldung: " + event.getName());
        sendParticipationNotificationBody(user, event, registration, notification);
    }

    private void sendParticipationNotificationBody(UserDetails user, Event event, Registration registration, Notification notification) {
        notification.getProps().put("user", user);
        notification.getProps().put("event", event);
        notification.getProps().put("event_start_date", event.getStart().atZone(timezone)
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        notification.getProps().put("event_end_date", event.getEnd().atZone(timezone)
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        notification.getProps().put("deadline", event.getStart().atZone(timezone)
                .minusDays(7).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        var eventUrl = frontendDomain + "/events/" + event.getStart().atZone(timezone)
                .getYear() + "/details/" + event.getKey() + "/registrations/" + registration.getKey();
        notification.getProps().put("confirm_link", eventUrl + "/confirm?accessKey=" + registration.getAccessKey());
        notification.getProps().put("deny_link", eventUrl + "/deny?accessKey=" + registration.getAccessKey());
        try {
            sendNotification(notification, user);
        } catch (Exception e) {
            log.error("Failed to send 'participation confirmation' notification to user {}", user.getEmail(), e);
        }
    }

    public void sendNotification(Notification notification, UserDetails toUser) throws IOException, TemplateException, MessagingException {
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
        message.setRecipients(RecipientType.TO, toUser.getEmail());
        // TODO what emails should also be send to crew speaker?
        // message.setRecipients(RecipientType.BCC, "crew.grossherzogin-elisabeth.de");
        message.setSubject(notification.getTitle());

        notification.getProps().put("title", notification.getTitle());
        notification.getProps().put("app_link", frontendDomain);
        notification.getProps().put("user", toUser);
        notification.getProps().put("footer", emailSettings.getFooter());
        var htmlContent = renderEmail(notification);
        message.setContent(htmlContent, "text/html; charset=utf-8");

        if (!recipientsWhitelist.isEmpty() && !recipientsWhitelist.contains(toUser.getEmail())) {
            log.warn("Skipped sending email to {} because notifications are configured to only be sent to whitelisted users", toUser.getEmail());
        } else if (toUser.getAuthKey() != null) {
            log.debug("Sending {} email to {}", notification.getType(), toUser.getEmail());
            mailSender.send(message);
        } else {
            log.warn("Skipped sending email to {} because notifications are configured to only be sent to beta users", toUser.getEmail());
        }
    }

    private String renderEmail(Notification notification) throws IOException, TemplateException {
        var props = notification.getProps();
        String content = renderTemplate("emails/" + notification.getType().toString() + ".ftl", props);

        var baseTemplateParams = new HashMap<>(notification.getProps());
        baseTemplateParams.put("content", content);
        return renderTemplate("partials/base.ftl", baseTemplateParams);
    }

    private String renderTemplate(String template, Object params) throws TemplateException, IOException {
        Template content = freemarkerConfig.getTemplate(template);
        Writer writer = new StringWriter();
        content.process(params, writer);
        return writer.toString();
    }

    public void sendDeclinedRegistrationNotification(Event event, String userName) {
        Notification notification = new Notification(NotificationType.DECLINED_REGISTRATION_ADMIN);

        notification.setTitle("Absage von " + userName);
        notification.getProps().put("event", event);
        notification.getProps().put("userName", userName);
        try {
            admins.forEach(admin -> {
                try {
                    sendNotification(notification, userService.getUserByEmail(admin).orElseThrow());
                } catch (Exception e) {
                    log.error("Failed to send 'declined registration' notification to admin {}", admin, e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to send 'declined registration' notification to admin", e);
        }
    }
}
