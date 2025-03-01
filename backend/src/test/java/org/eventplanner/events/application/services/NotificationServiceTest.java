package org.eventplanner.events.application.services;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import org.eventplanner.events.domain.values.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import freemarker.template.TemplateException;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;
import static org.eventplanner.testdata.UserFactory.createUser;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles(profiles = { "test" })
class NotificationServiceTest {

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    private NotificationService testee;
    private NotificationDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = mock(NotificationDispatcher.class);
        testee = new NotificationService(
            List.of(dispatcher),
            freeMarkerConfig.getConfiguration(),
            "https://localhost:8080"
        );
    }

    @Test
    void shouldThrowExceptionOnMissingProps() throws Exception {
        assertThrows(
            TemplateException.class,
            () -> testee.renderEmailContent(NotificationType.ADDED_TO_WAITING_LIST, new HashMap<>())
        );
        verify(dispatcher, never()).dispatch(any());
    }

    static Stream<Arguments> provideEventNotifications() {
        return Stream.of(
            Arguments.of(NotificationType.ADDED_TO_WAITING_LIST),
            Arguments.of(NotificationType.REMOVED_FROM_WAITING_LIST),
            Arguments.of(NotificationType.ADDED_TO_CREW),
            Arguments.of(NotificationType.REMOVED_FROM_CREW),
            Arguments.of(NotificationType.CONFIRM_PARTICIPATION),
            Arguments.of(NotificationType.CONFIRM_PARTICIPATION_REQUEST),
            Arguments.of(NotificationType.USER_DATA_CHANGED),
            Arguments.of(NotificationType.CREW_REGISTRATION_CANCELED),
            Arguments.of(NotificationType.CREW_REGISTRATION_ADDED)
        );
    }

    @ParameterizedTest
    @MethodSource("provideEventNotifications")
    void shouldCreateAllNotificationsCorrectly(NotificationType type) {
        var to = createUser();
        var event = createEvent();
        var registration = createRegistration();

        switch (type) {
            case ADDED_TO_WAITING_LIST -> testee.sendAddedToWaitingListNotification(to, event);
            case REMOVED_FROM_WAITING_LIST -> testee.sendRemovedFromWaitingListNotification(to, event);
            case ADDED_TO_CREW -> testee.sendAddedToCrewNotification(to, event);
            case REMOVED_FROM_CREW -> testee.sendRemovedFromCrewNotification(to, event);
            case CONFIRM_PARTICIPATION ->
                testee.sendFirstParticipationConfirmationRequestNotification(to, event, registration);
            case CONFIRM_PARTICIPATION_REQUEST ->
                testee.sendSecondParticipationConfirmationRequestNotification(to, event, registration);
            case USER_DATA_CHANGED -> testee.sendUserChangedPersonalDataNotification(to, to);
            case CREW_REGISTRATION_CANCELED ->
                testee.sendCrewRegistrationCanceledNotification(to, event, "Test", "Test");
            case CREW_REGISTRATION_ADDED -> testee.sendCrewRegistrationAddedNotification(to, event, "Test", "Test");
        }

        verify(dispatcher, timeout(1000).times(1)).dispatch(argThat(notification -> notification.type()
            .equals(type)));
    }
}
