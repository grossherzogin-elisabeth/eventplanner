package org.eventplanner.events.application.usecases.users;

import static org.eventplanner.events.domain.values.notifications.NotificationType.QUALIFICATION_CLOSE_TO_EXPIRED;
import static org.eventplanner.events.domain.values.notifications.NotificationType.QUALIFICATION_EXPIRED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.services.ConfigurationService;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.eventplanner.testdata.QualificationFactory;
import org.eventplanner.testdata.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserQualificationExpirationUseCaseTest {
    private static final Qualification NON_EXPIRING_QUALIFICATION = QualificationFactory.createQualification()
        .withKey(new QualificationKey("non-expiring"))
        .withExpires(false);
    private static final Qualification EXPIRING_QUALIFICATION = QualificationFactory.createQualification()
        .withKey(new QualificationKey("expiring"));

    private UserQualificationExpirationUseCase testee;

    private UserService userService;
    private ConfigurationService configurationService;
    private NotificationService notificationService;
    private QualificationRepository qualificationRepository;
    private PositionRepository positionRepository;

    @BeforeEach
    void setup() {
        userService = mock();
        configurationService = mock();
        notificationService = mock();
        qualificationRepository = mock();
        positionRepository = mock();

        when(qualificationRepository.findAllAsMap()).thenReturn(Map.of(
            EXPIRING_QUALIFICATION.getKey(), EXPIRING_QUALIFICATION,
            NON_EXPIRING_QUALIFICATION.getKey(), NON_EXPIRING_QUALIFICATION
        ));
        when(positionRepository.findAllAsMap()).thenReturn(Collections.emptyMap());

        testee = new UserQualificationExpirationUseCase(
            userService,
            configurationService,
            notificationService,
            qualificationRepository,
            positionRepository
        );
    }

    @Test
    void shouldDoNothingWhenNotificationsAreDisabled() {
        when(configurationService.isNotificationEnabled(QUALIFICATION_EXPIRED)).thenReturn(false);
        when(configurationService.isNotificationEnabled(QUALIFICATION_CLOSE_TO_EXPIRED)).thenReturn(false);

        testee.sendExpirationNotifications();

        verifyNoInteractions(userService);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldNotSendAnyNotification() {
        when(configurationService.isNotificationEnabled(QUALIFICATION_EXPIRED)).thenReturn(true);
        when(configurationService.isNotificationEnabled(QUALIFICATION_CLOSE_TO_EXPIRED)).thenReturn(true);

        // user with three qualifications: expired (past), soon (near future), non-expiring
        var user = UserFactory.createUser();
        user.addQualification(NON_EXPIRING_QUALIFICATION);
        user.addQualification(EXPIRING_QUALIFICATION, Instant.now().plus(150, ChronoUnit.DAYS));

        when(userService.getDetailedUsers()).thenReturn(List.of(user));

        testee.sendExpirationNotifications();

        // notifications for expired and soon
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendExpiringSoonNotification() {
        when(configurationService.isNotificationEnabled(QUALIFICATION_EXPIRED)).thenReturn(true);
        when(configurationService.isNotificationEnabled(QUALIFICATION_CLOSE_TO_EXPIRED)).thenReturn(true);

        var user = UserFactory.createUser();
        user.addQualification(NON_EXPIRING_QUALIFICATION);
        user.addQualification(EXPIRING_QUALIFICATION, Instant.now().plus(1, ChronoUnit.DAYS));

        when(userService.getDetailedUsers()).thenReturn(List.of(user));
        when(userService.updateUser(any())).thenAnswer(invocation -> invocation.getArgument(0));

        testee.sendExpirationNotifications();

        verify(notificationService).sendQualificationWillExpireSoonNotification(
            eq(user),
            eq(EXPIRING_QUALIFICATION),
            any()
        );
    }

    @Test
    void shouldSendExpiredNotification() throws InterruptedException {
        when(configurationService.isNotificationEnabled(QUALIFICATION_EXPIRED)).thenReturn(true);
        when(configurationService.isNotificationEnabled(QUALIFICATION_CLOSE_TO_EXPIRED)).thenReturn(true);

        // user with three qualifications: expired (past), soon (near future), non-expiring
        var user = UserFactory.createUser();
        user.addQualification(NON_EXPIRING_QUALIFICATION);
        // add a not yet expired qualification
        user.addQualification(EXPIRING_QUALIFICATION, Instant.now().plusMillis(100));
        // wait until it is expired
        Thread.sleep(150);

        when(userService.getDetailedUsers()).thenReturn(List.of(user));
        when(userService.updateUser(any())).thenAnswer(invocation -> invocation.getArgument(0));

        testee.sendExpirationNotifications();

        // notifications for expired and soon
        verify(notificationService).sendQualificationExpiredNotification(eq(user), eq(EXPIRING_QUALIFICATION), any());
    }

    @Test
    void shouldSkipInactiveUsers() {
        when(configurationService.isNotificationEnabled(QUALIFICATION_EXPIRED)).thenReturn(true);
        when(configurationService.isNotificationEnabled(QUALIFICATION_CLOSE_TO_EXPIRED)).thenReturn(true);

        var user = UserFactory.createUser().withAuthKey(null);
        user.addQualification(EXPIRING_QUALIFICATION, Instant.now().minusSeconds(100));
        when(userService.getDetailedUsers()).thenReturn(List.of(user));

        testee.sendExpirationNotifications();

        verifyNoInteractions(notificationService);
        // inactive user should not be updated
        // userService.updateUser should not be called
        verify(userService, never()).updateUser(any());
    }
}

