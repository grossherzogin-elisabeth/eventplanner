package org.eventplanner.events.application.services;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.runAsync;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import org.eventplanner.events.domain.entities.Event;
import org.eventplanner.events.domain.entities.Qualification;
import org.eventplanner.events.domain.entities.Registration;
import org.eventplanner.events.domain.entities.UserDetails;
import org.eventplanner.events.domain.entities.UserQualification;
import org.eventplanner.events.domain.values.GlobalNotification;
import org.eventplanner.events.domain.values.Notification;
import org.eventplanner.events.domain.values.NotificationType;
import org.eventplanner.events.domain.values.PersonalNotification;
import org.eventplanner.events.domain.values.Role;
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

    public void sendAddedToWaitingListNotification(
        @Nullable final UserDetails to,
        @NonNull final Event event
    ) {
        var title = "Deine Anmeldung zu " + event.getName();
        var type = NotificationType.ADDED_TO_WAITING_LIST;
        var props = new HashMap<String, Object>();
        addRecipientDetails(props, to);
        addEventDetails(props, event);
        var link = createEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendRemovedFromWaitingListNotification(
        @Nullable final UserDetails to,
        @NonNull final Event event
    ) {
        var title = "Deine Anmeldung zu " + event.getName();
        var type = NotificationType.REMOVED_FROM_WAITING_LIST;
        var props = new HashMap<String, Object>();
        addRecipientDetails(props, to);
        addEventDetails(props, event);
        var link = createEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendAddedToCrewNotification(
        @Nullable final UserDetails to,
        @NonNull final Event event
    ) {
        var title = "Deine Anmeldung zu " + event.getName();
        var type = NotificationType.ADDED_TO_CREW;
        var props = new HashMap<String, Object>();
        addRecipientDetails(props, to);
        addEventDetails(props, event);
        var link = createEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendRemovedFromCrewNotification(
        @Nullable final UserDetails to,
        @NonNull final Event event
    ) {
        var title = "Deine Anmeldung zu " + event.getName();
        var type = NotificationType.REMOVED_FROM_CREW;
        var props = new HashMap<String, Object>();
        addRecipientDetails(props, to);
        addEventDetails(props, event);
        var link = createEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendCrewRegistrationCanceledNotification(
        @NonNull final Role to,
        @NonNull final Event event,
        @NonNull final String userName,
        @NonNull final String position
    ) {
        var title = "Absage zu " + event.getName();
        var type = NotificationType.CREW_REGISTRATION_CANCELED;
        var props = new HashMap<String, Object>();
        addEventDetails(props, event);
        props.put("userName", userName);
        props.put("position", position);
        var link = createEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendCrewRegistrationAddedNotification(
        @NonNull final Role to,
        @NonNull final Event event,
        @NonNull final String userName,
        @NonNull final String position
    ) {
        var title = "Neue Anmeldung zu " + event.getName();
        var type = NotificationType.CREW_REGISTRATION_ADDED;
        var props = new HashMap<String, Object>();
        addEventDetails(props, event);
        props.put("userName", userName);
        props.put("position", position);
        var link = createEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendUserChangedPersonalDataNotification(
        @NonNull final Role to,
        @NonNull final UserDetails who
    ) {
        var title = who.getFullName() + " hat seine Daten geändert";
        var type = NotificationType.USER_DATA_CHANGED;
        var props = new HashMap<String, Object>();
        props.put("userName", who.getFullName());
        var link = createUserDeepLink(who);

        createNotification(to, type, title, props, link);
    }

    public void sendConfirmationRequestNotification(
        @Nullable final UserDetails to,
        @NonNull final Event event,
        @NonNull final Registration registration
    ) {
        var title = "Bitte um Rückmeldung: " + event.getName();
        var type = NotificationType.CONFIRM_REGISTRATION_REQUEST;
        var props = new HashMap<String, Object>();
        addRecipientDetails(props, to);
        addEventDetails(props, event);
        addRegistrationConfirmationDetails(props, event, registration);
        var link = createEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendConfirmationReminderNotification(
        @Nullable final UserDetails to,
        @NonNull final Event event,
        @NonNull final Registration registration
    ) {
        var title = "Bitte um sofortige Rückmeldung: " + event.getName();
        var type = NotificationType.CONFIRM_REGISTRATION_REMINDER;
        var props = new HashMap<String, Object>();
        addRecipientDetails(props, to);
        addEventDetails(props, event);
        addRegistrationConfirmationDetails(props, event, registration);
        var link = createEventDeepLink(event);

        createNotification(to, type, title, props, link);
    }

    public void sendQualificationAddedNotification(
        @Nullable final UserDetails to,
        @NonNull final Qualification qualification
    ) {
        var title = "Dir wurde die Qualifikation " + qualification.getName() + " zugewiesen";
        var type = NotificationType.QUALIFICATION_ADDED;
        var props = new HashMap<String, Object>();
        addRecipientDetails(props, to);
        addQualificationDetails(props, qualification, to);
        var link = createQualificationsDeepLink();

        createNotification(to, type, title, props, link);
    }

    public void sendQualificationUpdatedNotification(
        @Nullable final UserDetails to,
        @NonNull final Qualification qualification
    ) {
        var title = "Deine Qualifikation " + qualification.getName() + " wurde aktualisiert";
        var type = NotificationType.QUALIFICATION_UPDATED;
        var props = new HashMap<String, Object>();
        addRecipientDetails(props, to);
        addQualificationDetails(props, qualification, to);
        var link = createQualificationsDeepLink();

        createNotification(to, type, title, props, link);
    }

    public void sendQualificationRemovedNotification(
        @Nullable final UserDetails to,
        @NonNull final Qualification qualification
    ) {
        var title = "Deine Qualifikation " + qualification.getName() + " wurde entfernt";
        var type = NotificationType.QUALIFICATION_REMOVED;
        var props = new HashMap<String, Object>();
        addRecipientDetails(props, to);
        addQualificationDetails(props, qualification, to);
        var link = createQualificationsDeepLink();

        createNotification(to, type, title, props, link);
    }

    public void sendQualificationWillExpireSoonNotification(
        @Nullable final UserDetails to,
        @NonNull final Qualification qualification
    ) {
        var title = "Deine Qualifikation " + qualification.getName() + " läuft bald ab";
        var type = NotificationType.QUALIFICATION_CLOSE_TO_EXPIRED;
        var props = new HashMap<String, Object>();
        addRecipientDetails(props, to);
        addQualificationDetails(props, qualification, to);
        var link = createQualificationsDeepLink();

        createNotification(to, type, title, props, link);
    }

    public void sendQualificationExpiredNotification(
        @Nullable final UserDetails to,
        @NonNull final Qualification qualification
    ) {
        var title = "Deine Qualifikation " + qualification.getName() + " ist abgelaufen";
        var type = NotificationType.QUALIFICATION_EXPIRED;
        var props = new HashMap<String, Object>();
        addRecipientDetails(props, to);
        addQualificationDetails(props, qualification, to);
        var link = createQualificationsDeepLink();

        createNotification(to, type, title, props, link);
    }

    private void addRecipientDetails(
        @NonNull HashMap<String, Object> props,
        @Nullable final UserDetails to
    ) {
        if (to != null) {
            props.put("user", to);
        }
    }

    private void addRegistrationConfirmationDetails(
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
            .flatMap(it -> ofNullable(it.etd()))
            .orElse(event.getStart())
            .atZone(timezone);
        props.put("event_start_date", formatDate(start));
        props.put("event_start_datetime", formatDateTime(start));
        props.put("event_end_date", formatDate(event.getEnd().atZone(timezone)));
        props.put("event_end_datetime", formatDateTime(event.getStart().atZone(timezone)));
        props.put("event_crew_on_board_datetime", formatDateTime(event.getStart().atZone(timezone)));
    }

    private void addQualificationDetails(
        @NonNull HashMap<String, Object> props,
        @NonNull final Qualification qualification,
        @Nullable final UserDetails user
    ) {
        props.put("qualification", qualification);
        ofNullable(user)
            .flatMap(u -> u.getQualification(qualification.getKey()))
            .map(UserQualification::getExpiresAt)
            .ifPresent(exp -> props.put("expiration_date", formatDate(exp.atZone(timezone))));
    }

    private void createNotification(
        @NonNull final Role to,
        @NonNull final NotificationType type,
        @NonNull final String title,
        @NonNull final HashMap<String, Object> props,
        @Nullable final String link
    ) {
        try {
            // add some default props
            props.put("title", title);
            props.put("app_link", frontendUrl);

            var content = renderContent(type, props);
            var summary = renderSummary(type, props);
            var notification = new GlobalNotification(to, type, title, summary, content, link);

            log.debug("Dispatching {} global notification for users with role {}", type, to);
            dispatch(notification);
        } catch (Exception e) {
            log.error("Failed role create global '{}' notification for users with role {}", type, to, e);
        }
    }

    private void createNotification(
        @Nullable final UserDetails to,
        @NonNull final NotificationType type,
        @NonNull final String title,
        @NonNull final HashMap<String, Object> props,
        @Nullable final String link
    ) {
        if (to == null) {
            return;
        }
        try {
            // add some default props
            props.put("title", title);
            props.put("user", to);
            props.put("app_link", frontendUrl);

            var content = renderContent(type, props);
            var summary = renderSummary(type, props);
            var notification = new PersonalNotification(to, type, title, summary, content, link);

            log.debug("Dispatching {} notification for user {}", type, to.getKey());
            dispatch(notification);
        } catch (Exception e) {
            log.error("Failed to create '{}' notification for user {}", type, to.getKey(), e);
        }
    }

    private void dispatch(@NonNull final Notification notification) {
        for (var dispatcher : notificationDispatchers) {
            runAsync(() -> dispatcher.dispatch(notification));
        }
    }

    protected @NonNull String renderContent(
        @NonNull final NotificationType type,
        @NonNull final HashMap<String, Object> props
    ) throws TemplateException, IOException {
        return renderTemplate("emails/" + type + ".ftl", props).trim();
    }

    protected @NonNull String renderSummary(
        @NonNull final NotificationType type,
        @NonNull final HashMap<String, Object> props
    )
    throws TemplateException, IOException {
        return renderTemplate("notifications/" + type + ".ftl", props).trim();
    }

    private @NonNull String createUserDeepLink(@NonNull final UserDetails userDetails) {
        return frontendUrl + "/users/edit/" + userDetails.getKey();
    }

    private @NonNull String createEventDeepLink(@NonNull final Event event) {
        return frontendUrl + "/events/" + event.getStart().atZone(timezone).getYear() + "/details/" + event.getKey();
    }

    private @NonNull String createQualificationsDeepLink() {
        return frontendUrl + "/account?tab=app.account.tab.qualifications";
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
