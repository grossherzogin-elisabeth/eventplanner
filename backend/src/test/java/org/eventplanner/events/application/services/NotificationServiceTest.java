package org.eventplanner.events.application.services;

import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.QualificationFactory.createQualification;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;
import static org.eventplanner.testdata.UserFactory.createUser;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.notifications.NotificationType;
import org.eventplanner.testdata.TestDb;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import freemarker.template.TemplateException;

@SpringBootTest
@ActiveProfiles(profiles = { "test" })
class NotificationServiceTest {

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Autowired
    private ConfigurationService configurationService;

    private NotificationService testee;
    private NotificationDispatcher dispatcher;

    @BeforeAll
    static void setUpTestDb() throws IOException {
        TestDb.setup();
    }

    @BeforeEach
    void setUp() {
        dispatcher = mock(NotificationDispatcher.class);
        testee = new NotificationService(
            freeMarkerConfig,
            configurationService,
            List.of(dispatcher)
        );
    }

    @Test
    void shouldThrowExceptionOnMissingProps() throws Exception {
        assertThrows(
            TemplateException.class,
            () -> testee.renderContent(NotificationType.ADDED_TO_WAITING_LIST, new HashMap<>())
        );
        verify(dispatcher, never()).dispatch(any());
    }

    @ParameterizedTest
    @EnumSource(NotificationType.class)
    void shouldCreateAllNotificationsCorrectly(NotificationType type) {
        var toUser = createUser();
        var event = createEvent();
        var qualification = createQualification();
        toUser.addQualification(qualification, Instant.now().plusSeconds(100000));
        var registration = createRegistration();

        switch (type) {
            case ADDED_TO_WAITING_LIST -> testee.sendAddedToWaitingListNotification(toUser, event);
            case REMOVED_FROM_WAITING_LIST -> testee.sendRemovedFromWaitingListNotification(toUser, event);
            case ADDED_TO_CREW -> testee.sendAddedToCrewNotification(toUser, event);
            case REMOVED_FROM_CREW -> testee.sendRemovedFromCrewNotification(toUser, event);
            case CONFIRM_REGISTRATION_REQUEST ->
                testee.sendConfirmationRequestNotification(toUser, event, registration);
            case CONFIRM_REGISTRATION_REMINDER ->
                testee.sendConfirmationReminderNotification(toUser, event, registration);
            case USER_DATA_CHANGED -> testee.sendUserChangedPersonalDataNotification(Role.USER_MANAGER, toUser);
            case CREW_REGISTRATION_CANCELED ->
                testee.sendCrewRegistrationCanceledNotification(Role.TEAM_PLANNER, event, "Test", "Test");
            case CREW_REGISTRATION_ADDED ->
                testee.sendCrewRegistrationAddedNotification(Role.TEAM_PLANNER, event, "Test", "Test");
            case QUALIFICATION_ADDED ->
                testee.sendQualificationAddedNotification(toUser, qualification, Collections.emptyMap());
            case QUALIFICATION_UPDATED ->
                testee.sendQualificationUpdatedNotification(toUser, qualification, Collections.emptyMap());
            case QUALIFICATION_REMOVED ->
                testee.sendQualificationRemovedNotification(toUser, qualification, Collections.emptyMap());
            case QUALIFICATION_CLOSE_TO_EXPIRED ->
                testee.sendQualificationWillExpireSoonNotification(toUser, qualification, Collections.emptyMap());
            case QUALIFICATION_EXPIRED ->
                testee.sendQualificationExpiredNotification(toUser, qualification, Collections.emptyMap());
        }

        verify(dispatcher, timeout(100).times(1))
            .dispatch(argThat(notification -> notification.type().equals(type)));
    }
}
