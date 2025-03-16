package org.eventplanner.events.application.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.application.ports.EmailSender;
import org.eventplanner.events.application.ports.QueuedEmailRepository;
import org.eventplanner.events.domain.entities.QueuedEmail;
import org.eventplanner.events.domain.values.GlobalNotification;
import org.eventplanner.events.domain.values.NotificationType;
import org.eventplanner.events.domain.values.PersonalNotification;
import org.eventplanner.events.domain.values.Role;
import org.eventplanner.events.domain.values.Settings;
import org.eventplanner.events.domain.values.UserKey;
import org.eventplanner.testdata.TestDb;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.UserFactory.createUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles(profiles = { "test" })
class EmailServiceTest {

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    private QueuedEmailRepository queuedEmailRepository;
    private UserService userService;
    private SettingsService settingsService;
    private EmailSender emailSender;

    private EmailService testee;

    @BeforeAll
    static void setUpTestDb() throws IOException {
        TestDb.setup();
    }

    @BeforeEach
    void setUp() {
        settingsService = mock(SettingsService.class);
        userService = mock(UserService.class);
        emailSender = mock(EmailSender.class);
        queuedEmailRepository = mock(QueuedEmailRepository.class);
        testee = new EmailService(
            settingsService,
            userService,
            queuedEmailRepository,
            emailSender,
            freeMarkerConfig.getConfiguration(),
            ""
        );
    }

    @Test
    void shouldRenderEmail() throws Exception {
        var notification = createPersonalNotification();
        var email = testee.renderEmailContent(notification);
        assertThat(email).isNotNull().isNotEmpty().contains("TestContent");
    }

    @Test
    void shouldQueueOneEmail() {
        var notification = createPersonalNotification();

        testee.dispatch(notification);

        verify(queuedEmailRepository, times(1)).queue(any());
    }

    @Test
    void shouldQueueMultipleEmails() {
        var notification = createGlobalNotification();
        when(userService.getUsersByRole(notification.recipients()))
            .thenReturn(List.of(createUser(), createUser(), createUser()));

        testee.dispatch(notification);

        verify(queuedEmailRepository, times(3)).queue(any());
    }

    @Test
    void shouldNotSendEmail() throws Exception {
        when(queuedEmailRepository.next()).thenReturn(Optional.empty());
        when(settingsService.getSettings()).thenReturn(createSettings());

        testee.sendNextEmail();

        verify(emailSender, never()).sendEmail(any(), any());
    }

    @Test
    void shouldSendEmail() throws Exception {
        when(queuedEmailRepository.next()).thenReturn(Optional.of(createQueuedEmail()));
        when(settingsService.getSettings()).thenReturn(createSettings());

        testee.sendNextEmail();

        verify(emailSender, times(1)).sendEmail(any(), any());
    }

    @Test
    void shouldQueueForRetry() throws Exception {
        when(queuedEmailRepository.next()).thenReturn(Optional.of(createQueuedEmail()));
        when(settingsService.getSettings()).thenReturn(createSettings());
        doThrow(Exception.class).when(emailSender).sendEmail(any(), any());

        testee.sendNextEmail();

        verify(queuedEmailRepository, times(1)).queue(any());
    }

    @Test
    void shouldNotQueueForRetryWhenMaxRetriesExceeded() throws Exception {
        when(queuedEmailRepository.next()).thenReturn(Optional.of(createQueuedEmail().withRetries(10)));
        when(settingsService.getSettings()).thenReturn(createSettings());
        doThrow(Exception.class).when(emailSender).sendEmail(any(), any());

        testee.sendNextEmail();

        verify(queuedEmailRepository, never()).queue(any());
    }

    private QueuedEmail createQueuedEmail() {
        return new QueuedEmail(
            NotificationType.ADDED_TO_CREW,
            "someone@email.de",
            new UserKey(),
            "Test",
            "Test"
        );
    }

    private Settings createSettings() {
        return new Settings(
            new Settings.NotificationSettings(),
            new Settings.EmailSettings(),
            new Settings.UiSettings(
                "Title",
                "Title",
                "test@test.de",
                "test@test.de"
            )
        );
    }

    private PersonalNotification createPersonalNotification() {
        return createGlobalNotification().toPersonalNotification(createUser());
    }

    private GlobalNotification createGlobalNotification() {
        return new GlobalNotification(
            Role.TEAM_MEMBER,
            NotificationType.ADDED_TO_WAITING_LIST,
            "TestTitle",
            "TestSummary",
            "TestContent",
            "https://localhost:8080"
        );
    }
}
