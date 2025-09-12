package org.eventplanner.testutil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eventplanner.events.adapter.jpa.notifications.QueuedEmailJpaRepository;
import org.eventplanner.events.adapter.mail.JavaEmailAdapter;
import org.eventplanner.events.application.scheduled.EmailScheduler;
import org.eventplanner.events.domain.entities.notifications.QueuedEmail;
import org.eventplanner.events.domain.values.notifications.NotificationType;
import org.eventplanner.events.domain.values.users.UserKey;
import org.mockito.ArgumentMatcher;
import org.mockito.verification.VerificationMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@AutoConfigureMockMvc
@Transactional // resets db changes after each test
public class EmailSpy {

    @Autowired
    private EmailScheduler emailScheduler;

    @Autowired
    private QueuedEmailJpaRepository queuedEmailJpaRepository;

    @MockitoBean
    private JavaEmailAdapter javaEmailAdapter;

    public void verifyEmailSent(NotificationType type, TestUser to) throws Exception {
        verifyEmailSent(type, new UserKey(to.getOidcId()));
    }

    public void verifyEmailSent(NotificationType type, UserKey to) throws Exception {
        verifyEmailSent(times(1), it -> it.getType().equals(type) && it.getUserKey().equals(to));
    }

    public void verifyEmailsSent(VerificationMode mode, NotificationType type) throws Exception {
        verifyEmailSent(mode, it -> it.getType().equals(type));
    }

    public void verifyEmailSent(VerificationMode mode, ArgumentMatcher<QueuedEmail> email) throws Exception {
        // send all queued notifications
        while (queuedEmailJpaRepository.count() > 0) {
            emailScheduler.sendNotification();
        }

        // wanted email should be sent exactly once
        verify(javaEmailAdapter, mode).sendEmail(argThat(email), any());
        // also allow all other emails
        verify(javaEmailAdapter, atLeast(0)).sendEmail(any(), any());
    }
}
