package org.eventplanner.events.application.services;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.domain.entities.Event;
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
import static java.util.concurrent.CompletableFuture.runAsync;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationService {
    private static final ZoneId timezone = ZoneId.of("Europe/Berlin");

    private final Configuration freemarkerConfig;
    private final String frontendUrl;
    private final List<NotificationDispatcher> notificationDispatchers;

    public NotificationService(
        @Autowired List<NotificationDispatcher> notificationDispatchers,
        @Autowired Configuration freemarkerConfig,
        @Value("${frontend.url}") String frontendUrl
    ) {
        this.notificationDispatchers = notificationDispatchers;
        this.freemarkerConfig = freemarkerConfig;
        this.frontendUrl = frontendUrl;
    }

    public void sendAddedToWaitingListNotification(@Nullable UserDetails to, @NonNull Event event) {
        var title = "Deine Anmeldung zu " + event.getName();
        var type = NotificationType.ADDED_TO_WAITING_LIST;
        var props = new HashMap<String, Object>();
        addEventDetails(props, event);
        var link = getEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendRemovedFromWaitingListNotification(@Nullable UserDetails to, @NonNull Event event) {
        var title = "Deine Anmeldung zu " + event.getName();
        var type = NotificationType.REMOVED_FROM_WAITING_LIST;
        var props = new HashMap<String, Object>();
        addEventDetails(props, event);
        var link = getEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendAddedToCrewNotification(@Nullable UserDetails to, @NonNull Event event) {
        var title = "Deine Anmeldung zu " + event.getName();
        var type = NotificationType.ADDED_TO_CREW;
        var props = new HashMap<String, Object>();
        addEventDetails(props, event);
        var link = getEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendRemovedFromCrewNotification(@Nullable UserDetails to, @NonNull Event event) {
        var title = "Deine Anmeldung zu " + event.getName();
        var type = NotificationType.REMOVED_FROM_CREW;
        var props = new HashMap<String, Object>();
        addEventDetails(props, event);
        var link = getEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendCrewRegistrationCanceledNotification(
        @Nullable UserDetails to,
        @NonNull Event event,
        @NonNull String userName,
        @NonNull String position
    ) {
        var title = "Absage zu " + event.getName();
        var type = NotificationType.CREW_REGISTRATION_CANCELED;
        var props = new HashMap<String, Object>();
        addEventDetails(props, event);
        props.put("userName", userName);
        props.put("position", position);
        var link = getEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendCrewRegistrationAddedNotification(
        @Nullable UserDetails to,
        @NonNull Event event,
        @NonNull String userName,
        @NonNull String position
    ) {
        var title = "Neue Anmeldung zu " + event.getName();
        var type = NotificationType.CREW_REGISTRATION_ADDED;
        var props = new HashMap<String, Object>();
        addEventDetails(props, event);
        props.put("userName", userName);
        props.put("position", position);
        var link = getEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendUserChangedPersonalDataNotification(@Nullable UserDetails to, @NonNull UserDetails who) {
        var title = who.getFullName() + " hat seine Daten geändert";
        var type = NotificationType.USER_DATA_CHANGED;
        var props = new HashMap<String, Object>();
        props.put("userName", who.getFullName());
        var link = getUserDeepLink(who);

        createNotification(to, type, title, props, link);
    }

    public void sendFirstParticipationConfirmationRequestNotification(
        @Nullable UserDetails to,
        @NonNull Event event,
        @NonNull Registration registration
    ) {
        var title = "Bitte um Rückmeldung: " + event.getName();
        var type = NotificationType.CONFIRM_PARTICIPATION;
        var props = new HashMap<String, Object>();
        addEventDetails(props, event);
        addParticipationNotificationDetails(props, event, registration);
        var link = getEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendSecondParticipationConfirmationRequestNotification(
        @Nullable UserDetails to,
        @NonNull Event event,
        @NonNull Registration registration
    ) {
        var title = "Bitte um sofortige Rückmeldung: " + event.getName();
        var type = NotificationType.CONFIRM_PARTICIPATION_REQUEST;
        var props = new HashMap<String, Object>();
        addEventDetails(props, event);
        addParticipationNotificationDetails(props, event, registration);
        var link = getEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    private void addParticipationNotificationDetails(
        @NonNull HashMap<String, Object> props,
        @NonNull final Event event,
        @NonNull final Registration registration
    ) {
        props.put("deadline", formatDate(event.getStart().atZone(timezone).minusDays(7)));
        var eventUrl = frontendUrl + "/events/" + event.getStart().atZone(timezone)
            .getYear() + "/details/" + event.getKey() + "/registrations/" + registration.getKey();
        props.put("confirm_link", eventUrl + "/confirm?accessKey=" + registration.getAccessKey());
        props.put("deny_link", eventUrl + "/deny?accessKey=" + registration.getAccessKey());
    }

    private void addEventDetails(
        @NonNull HashMap<String, Object> props,
        @NonNull final Event event
    ) {
        props.put("event", event);
        var start = event.getLocations().stream()
            .findFirst()
            .flatMap(it -> Optional.ofNullable(it.etd()))
            .orElse(event.getStart())
            .atZone(timezone);
        props.put("event_start_date", formatDate(start));
        props.put("event_start_datetime", formatDateTime(start));
        props.put("event_end_date", formatDate(event.getEnd().atZone(timezone)));
        props.put("event_end_datetime", formatDateTime(event.getStart().atZone(timezone)));
        props.put("event_crew_on_board_datetime", formatDateTime(event.getStart().atZone(timezone)));
    }

    public void createNotification(
        @Nullable UserDetails to,
        @NonNull NotificationType type,
        @NonNull String title,
        @NonNull HashMap<String, Object> props,
        @Nullable String link
    ) {
        if (to == null) {
            return;
        }
        try {
            // add some default props
            props.put("title", title);
            props.put("user", to);
            props.put("app_link", frontendUrl);

            var content = renderEmailContent(type, props);
            var summary = renderSummary(type, props);

            var notification = new Notification(to, type, title, summary, content, link);
            log.debug(
                "Dispatching {} notification for user {}",
                notification.type(),
                notification.recipient().getKey()
            );

            notificationDispatchers
                .forEach(dispatcher -> runAsync(() -> dispatcher.dispatch(notification)));
        } catch (Exception e) {
            log.error(
                "Failed to create '{}' notification for user {}",
                type,
                to.getKey(),
                e
            );
        }
    }

    protected String renderEmailContent(@NonNull NotificationType type, @NonNull HashMap<String, Object> props)
    throws TemplateException, IOException {
        String content = renderTemplate("emails/" + type + ".ftl", props).trim();
        var baseTemplateParams = new HashMap<>(props);
        baseTemplateParams.put("content", content);
        return renderTemplate("partials/base.ftl", baseTemplateParams);
    }

    protected String renderSummary(@NonNull final NotificationType type, @NonNull final HashMap<String, Object> props)
    throws TemplateException, IOException {
        return renderTemplate("notifications/" + type + ".ftl", props).trim();
    }

    private String getUserDeepLink(@NonNull final UserDetails userDetails) {
        return frontendUrl + "/users/edit/" + userDetails.getKey();
    }

    private String getEventDeepLink(@NonNull final Event event) {
        return frontendUrl + "/events/" + event.getStart().atZone(timezone).getYear() + "/details/" + event.getKey();
    }

    private @NonNull String renderTemplate(@NonNull final String template, @NonNull final Object params)
    throws TemplateException, IOException {
        Template content = freemarkerConfig.getTemplate(template);
        Writer writer = new StringWriter();
        content.process(params, writer);
        return writer.toString();
    }

    private @NonNull String formatDate(@NonNull final ZonedDateTime zonedDateTime) {
        return zonedDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private @NonNull String formatDateTime(@NonNull final ZonedDateTime zonedDateTime) {
        return zonedDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}
