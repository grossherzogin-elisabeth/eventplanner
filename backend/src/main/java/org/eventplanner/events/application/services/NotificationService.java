package org.eventplanner.events.application.services;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Optional;

import org.eventplanner.events.application.ports.QueuedEmailRepository;
import org.eventplanner.events.domain.entities.Event;
import org.eventplanner.events.domain.entities.QueuedEmail;
import org.eventplanner.events.domain.entities.Registration;
import org.eventplanner.events.domain.entities.UserDetails;
import org.eventplanner.events.domain.values.Notification;
import org.eventplanner.events.domain.values.NotificationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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

    public void sendAddedToWaitingListNotification(@Nullable UserDetails to, @NonNull Event event) {
        if (to == null) {
            return;
        }
        log.debug("Creating added to waiting list notification for user {}", to.getKey());
        Notification notification = new Notification(NotificationType.ADDED_TO_WAITING_LIST);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        notification.getProps().put("user", to);
        addEventDetails(notification, event);
        createNotification(notification, to);
    }

    public void sendRemovedFromWaitingListNotification(@Nullable UserDetails to, @NonNull Event event) {
        if (to == null) {
            return;
        }
        log.debug("Creating removed from waiting list notification for user {}", to.getKey());
        Notification notification = new Notification(NotificationType.REMOVED_FROM_WAITING_LIST);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        notification.getProps().put("user", to);
        addEventDetails(notification, event);
        createNotification(notification, to);
    }

    public void sendAddedToCrewNotification(@Nullable UserDetails to, @NonNull Event event) {
        if (to == null) {
            return;
        }
        log.debug("Creating added to crew notification for user {}", to.getKey());
        Notification notification = new Notification(NotificationType.ADDED_TO_CREW);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        addEventDetails(notification, event);
        notification.getProps().put("user", to);
        createNotification(notification, to);
    }

    public void sendRemovedFromCrewNotification(@Nullable UserDetails to, @NonNull Event event) {
        if (to == null) {
            return;
        }
        log.debug("Creating removed from crew notification for user {}", to.getKey());
        Notification notification = new Notification(NotificationType.REMOVED_FROM_CREW);
        notification.setTitle("Deine Anmeldung zu " + event.getName());
        notification.getProps().put("user", to);
        addEventDetails(notification, event);
        createNotification(notification, to);
    }

    public void sendCrewRegistrationCanceledNotification(
        @Nullable UserDetails to,
        @NonNull Event event,
        @NonNull String userName,
        @NonNull String position
    ) {
        if (to == null) {
            return;
        }
        log.debug("Creating crew registration canceled notification for user {}", to.getKey());
        Notification notification = new Notification(NotificationType.CREW_REGISTRATION_CANCELED);
        notification.setTitle("Absage zu " + event.getName());
        addEventDetails(notification, event);
        notification.getProps().put("userName", userName);
        notification.getProps().put("position", position);
        createNotification(notification, to);
    }

    public void sendFirstParticipationConfirmationRequestNotification(
        @Nullable UserDetails to,
        @NonNull Event event,
        @NonNull Registration registration
    ) {
        if (to == null || !event.isUpForFirstParticipationConfirmationRequest()) {
            return;
        }
        log.debug("Creating first participation confirmation request for user {}", to.getKey());
        Notification notification = new Notification(NotificationType.CONFIRM_PARTICIPATION);
        notification.setTitle("Bitte um Rückmeldung: " + event.getName());
        addEventDetails(notification, event);

        sendParticipationNotificationBody(to, event, registration, notification);
    }

    public void sendSecondParticipationConfirmationRequestNotification(
        @Nullable UserDetails to,
        @NonNull Event event,
        @NonNull Registration registration
    ) {
        if (to == null || !event.isUpForSecondParticipationConfirmationRequest()) {
            return;
        }
        log.debug("Creating second participation confirmation request for user {}", to.getKey());
        Notification notification = new Notification(NotificationType.CONFIRM_PARTICIPATION_REQUEST);
        notification.setTitle("Bitte um sofortige Rückmeldung: " + event.getName());
        addEventDetails(notification, event);

        sendParticipationNotificationBody(to, event, registration, notification);
    }

    public void sendUserChangedPersonalDataNotification(@Nullable UserDetails to, @NonNull UserDetails who) {
        if (to == null) {
            return;
        }
        log.debug("Creating user changed personal data notification for user {}", to.getKey());
        Notification notification = new Notification(NotificationType.USER_DATA_CHANGED);
        notification.setTitle(who.getFullName() + " hat seine Daten geändert");
        notification.getProps().put("user", to);
        notification.getProps().put("userName", who.getFullName());
        createNotification(notification, to);
    }

    private void sendParticipationNotificationBody(
        @Nullable UserDetails to,
        @NonNull Event event,
        @NonNull Registration registration,
        @NonNull Notification notification
    ) {
        if (to == null) {
            return;
        }
        notification.getProps().put("user", to);
        notification.getProps().put("deadline", formatDate(event.getStart().atZone(timezone).minusDays(7)));
        var eventUrl = frontendUrl + "/events/" + event.getStart().atZone(timezone)
            .getYear() + "/details/" + event.getKey() + "/registrations/" + registration.getKey();
        notification.getProps().put("confirm_link", eventUrl + "/confirm?accessKey=" + registration.getAccessKey());
        notification.getProps().put("deny_link", eventUrl + "/deny?accessKey=" + registration.getAccessKey());
        createNotification(notification, to);
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

    public void createNotification(@NonNull Notification notification, @NonNull UserDetails to) {
        try {
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
        } catch (Exception e) {
            log.error(
                "Failed to create '{}' notification for user {}",
                notification.getType(),
                to.getKey(),
                e
            );
        }
    }

    private String renderEmail(@NonNull Notification notification) throws IOException, TemplateException {
        var props = notification.getProps();
        String content = renderTemplate("emails/" + notification.getType() + ".ftl", props);

        var baseTemplateParams = new HashMap<>(notification.getProps());
        baseTemplateParams.put("content", content);
        return renderTemplate("partials/base.ftl", baseTemplateParams);
    }

    private @NonNull String renderTemplate(@NonNull String template, @NonNull Object params)
    throws TemplateException, IOException {
        Template content = freemarkerConfig.getTemplate(template);
        Writer writer = new StringWriter();
        content.process(params, writer);
        return writer.toString();
    }

    private @NonNull String formatDate(@NonNull ZonedDateTime zonedDateTime) {
        var formatted = zonedDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        formatted = formatted.replace(".", "&#8203."); // zero length whitespace to prevent phone number detection
        return formatted;
    }

    private @NonNull String formatDateTime(@NonNull ZonedDateTime zonedDateTime) {
        var formatted = zonedDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        formatted = formatted.replace(".", "&#8203."); // zero length whitespace to prevent phone number detection
        return formatted;
    }
}
