package org.eventplanner.application.services;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Optional;

import org.eventplanner.domain.entities.Event;
import org.eventplanner.domain.entities.Registration;
import org.eventplanner.application.ports.QueuedEmailRepository;
import org.eventplanner.domain.entities.QueuedEmail;
import org.eventplanner.domain.values.Notification;
import org.eventplanner.domain.values.NotificationType;
import org.eventplanner.domain.entities.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationService {
    private static final ZoneId timezone = ZoneId.of("Europe/Berlin");

    private final Configuration freemarkerConfig;
    private final String frontendUrl;
    private final QueuedEmailRepository queuedEmailRepository;

    public NotificationService(
        @Autowired QueuedEmailRepository queuedEmailRepository,
        @Autowired Configuration freemarkerConfig,
        @Value("${frontend.url}") String frontendUrl
    ) {
        this.queuedEmailRepository = queuedEmailRepository;
        this.freemarkerConfig = freemarkerConfig;
        this.frontendUrl = frontendUrl;
    }

    public void sendAddedToWaitingListNotification(@NonNull UserDetails to, @NonNull Event event) {
        log.debug("Creating added to waiting list notification for user {}", to.getKey());
        Notification notification = new Notification(NotificationType.ADDED_TO_WAITING_LIST);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        notification.getProps().put("user", to);
        addEventDetails(notification, event);
        try {
            sendNotification(notification, to);
        } catch (Exception e) {
            log.error("Failed to send 'removed from waiting list' notification to user {}", to.getKey(), e);
        }
    }

    public void sendRemovedFromWaitingListNotification(@NonNull UserDetails to, @NonNull Event event) {
        log.debug("Creating removed from waiting list notification for user {}", to.getKey());
        Notification notification = new Notification(NotificationType.REMOVED_FROM_WAITING_LIST);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        notification.getProps().put("user", to);
        addEventDetails(notification, event);
        try {
            sendNotification(notification, to);
        } catch (Exception e) {
            log.error("Failed to send 'removed from waiting list' notification to user {}", to.getKey(), e);
        }
    }

    public void sendAddedToCrewNotification(@NonNull UserDetails user, @NonNull Event event) {
        log.debug("Creating added to crew notification for user {}", user.getKey());
        Notification notification = new Notification(NotificationType.ADDED_TO_CREW);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        addEventDetails(notification, event);
        notification.getProps().put("user", user);
        try {
            sendNotification(notification, user);
        } catch (Exception e) {
            log.error("Failed to send 'added to crew' notification to user {}", user.getKey(), e);
        }
    }

    public void sendRemovedFromCrewNotification(@NonNull UserDetails to, @NonNull Event event) {
        log.debug("Creating removed from crew notification for user {}", to.getKey());
        Notification notification = new Notification(NotificationType.REMOVED_FROM_CREW);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        notification.getProps().put("user", to);
        addEventDetails(notification, event);
        try {
            sendNotification(notification, to);
        } catch (Exception e) {
            log.error("Failed to send 'removed from crew' notification to user {}", to.getKey(), e);
        }
    }

    public void sendCrewRegistrationCanceledNotification(
        @NonNull UserDetails to,
        @NonNull Event event,
        @NonNull String userName,
        @NonNull String position
    ) {
        log.debug("Creating crew registration canceled notification for user {}", to.getKey());
        Notification notification = new Notification(NotificationType.CREW_REGISTRATION_CANCELED);
        notification.setTitle("Absage zu " + event.getName());
        addEventDetails(notification, event);
        notification.getProps().put("userName", userName);
        notification.getProps().put("position", position);
        try {
            sendNotification(notification, to);
        } catch (Exception e) {
            log.error("Failed to send 'crew registration canceled' notification to user {}", to.getKey(), e);
        }
    }

    public void sendFirstParticipationConfirmationRequestNotification(
        @NonNull UserDetails to,
        @NonNull Event event,
        @NonNull Registration registration
    ) {
        log.debug("Creating first participation confirmation request for user {}", to.getKey());
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
        log.debug("Creating second participation confirmation request for user {}", to.getKey());
        Notification notification = new Notification(NotificationType.CONFIRM_PARTICIPATION_REQUEST);
        notification.setTitle("Bitte um sofortige Rückmeldung: " + event.getName());
        addEventDetails(notification, event);

        sendParticipationNotificationBody(to, event, registration, notification);
    }

    public void sendUserChangedPersonalDataNotification(@NonNull UserDetails to, @NonNull UserDetails who) {
        log.debug("Creating user changed personal data notification for user {}", to.getKey());
        Notification notification = new Notification(NotificationType.USER_DATA_CHANGED);
        notification.setTitle(who.getFullName() + " hat seine Daten geändert");
        notification.getProps().put("user", to);
        notification.getProps().put("userName", who.getFullName());
        try {
            sendNotification(notification, to);
        } catch (Exception e) {
            log.error("Failed to send 'user changed personal data' notification to user {}", to.getKey(), e);
        }
    }

    private void sendParticipationNotificationBody(
        @NonNull UserDetails to,
        @NonNull Event event,
        @NonNull Registration registration,
        @NonNull Notification notification
    ) {
        notification.getProps().put("user", to);
        notification.getProps().put("deadline", formatDate(event.getStart().atZone(timezone).minusDays(7)));
        var eventUrl = frontendUrl + "/events/" + event.getStart().atZone(timezone)
            .getYear() + "/details/" + event.getKey() + "/registrations/" + registration.getKey();
        notification.getProps().put("confirm_link", eventUrl + "/confirm?accessKey=" + registration.getAccessKey());
        notification.getProps().put("deny_link", eventUrl + "/deny?accessKey=" + registration.getAccessKey());
        try {
            sendNotification(notification, to);
        } catch (Exception e) {
            log.error("Failed to send participation confirmation request to user {}", to.getKey(), e);
        }
    }

    private void addEventDetails(@NonNull Notification notification, @NonNull Event event) {
        notification.getProps().put("event", event);
        var start = event.getLocations().stream()
            .findFirst()
            .flatMap(it -> Optional.ofNullable(it.etd()))
            .orElse(event.getStart())
            .atZone(timezone);

        notification.getProps().put("event_start_date", formatDate(start));
        notification.getProps().put("event_start_datetime", formatDateTime(start));
        notification.getProps().put("event_end_date", formatDate(event.getEnd().atZone(timezone)));
        notification.getProps().put("event_end_datetime", formatDateTime(event.getStart().atZone(timezone)));
        notification.getProps().put("event_crew_on_board_datetime", formatDateTime(event.getStart().atZone(timezone)));
    }

    public void sendNotification(@NonNull Notification notification, @NonNull UserDetails to)
    throws IOException, TemplateException {
        if (to.getEmail() == null) {
            log.warn("Cannot send email notification to user {} due to missing email address", to.getKey());
            return;
        }
        notification.getProps().put("title", notification.getTitle());
        notification.getProps().put("app_link", frontendUrl);
        notification.getProps().put("user", to);
        queuedEmailRepository.queue(new QueuedEmail(
            notification.getType(),
            to.getEmail().trim(),
            to.getKey(),
            notification.getTitle(),
            renderEmail(notification)
        ));
    }

    private String renderEmail(@NonNull Notification notification) throws IOException, TemplateException {
        var props = notification.getProps();
        String content = renderTemplate("emails/" + notification.getType() + ".ftl", props);

        var baseTemplateParams = new HashMap<>(notification.getProps());
        baseTemplateParams.put("content", content);
        return renderTemplate("partials/base.ftl", baseTemplateParams);
    }

    private String renderTemplate(@NonNull String template, @NonNull Object params)
    throws TemplateException, IOException {
        Template content = freemarkerConfig.getTemplate(template);
        Writer writer = new StringWriter();
        content.process(params, writer);
        return writer.toString();
    }

    private String formatDate(ZonedDateTime zonedDateTime) {
        var formatted = zonedDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        formatted = formatted.replace(".", "&#8203."); // zero length whitespace to prevent phone number detection
        return formatted;
    }

    private String formatDateTime(ZonedDateTime zonedDateTime) {
        var formatted = zonedDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        formatted = formatted.replace(".", "&#8203."); // zero length whitespace to prevent phone number detection
        return formatted;
    }
}
