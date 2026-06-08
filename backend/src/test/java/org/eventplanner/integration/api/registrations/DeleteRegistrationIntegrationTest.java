package org.eventplanner.integration.api.registrations;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;
import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.eventplanner.events.adapter.jpa.events.EventJpaEntity;
import org.eventplanner.events.adapter.jpa.events.EventJpaRepository;
import org.eventplanner.events.adapter.jpa.events.RegistrationJpaEntity;
import org.eventplanner.events.adapter.jpa.events.RegistrationJpaRepository;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.values.notifications.NotificationType;
import org.eventplanner.testdata.PositionKeys;
import org.eventplanner.testutil.EmailSpy;
import org.eventplanner.testutil.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@AutoConfigureMockMvc
@Transactional // resets db changes after each test
class DeleteRegistrationIntegrationTest extends EmailSpy {

    private MockMvc webMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EventJpaRepository eventJpaRepository;

    @Autowired
    private RegistrationJpaRepository registrationJpaRepository;

    @BeforeEach
    void setup() {
        webMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        var event = createEvent();
        saveTestEvent(event);
        var registration = event.getRegistrations().getFirst();

        webMvc.perform(delete("/api/v1/events/" + event.getKey() + "/registrations/" + registration.getKey())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnNotFoundForUnknownEvent() throws Exception {
        webMvc.perform(delete("/api/v1/events/" + randomUUID() + "/registrations/" + randomUUID())
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForUnknownRegistration() throws Exception {
        var event = createEvent();
        saveTestEvent(event);

        webMvc.perform(delete("/api/v1/events/" + event.getKey() + "/registrations/" + randomUUID())
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldBlockDeletionOfOtherUsersRegistration() throws Exception {
        var event = createEvent();
        saveTestEvent(event);
        // registrations belong to random users, so deletion should be forbidden for TEAM_MEMBER
        var registration = event.getRegistrations().getFirst();

        webMvc.perform(delete("/api/v1/events/" + event.getKey() + "/registrations/" + registration.getKey())
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        verifyEmailsSent(never(), NotificationType.REMOVED_FROM_CREW);
    }

    @Test
    void shouldDeleteWaitinglistRegistration() throws Exception {
        var registrationToDelete = createRegistration()
            .withPosition(PositionKeys.DECKHAND)
            .withUserKey(TestUser.TEAM_MEMBER.getKey());
        var event = createEvent()
            .withRegistrations(List.of(
                registrationToDelete,
                createRegistration(),
                createRegistration()
            ));
        saveTestEvent(event);

        webMvc.perform(delete("/api/v1/events/" + event.getKey() + "/registrations/" + registrationToDelete.getKey())
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value(event.getKey().value()))
            .andExpect(jsonPath("$.registrations", iterableWithSize(2)));

        // registration should no longer exist
        assertThat(registrationJpaRepository.findById(registrationToDelete.getKey().value())).isEmpty();

        // notification should have been sent
        verifyEmailsSent(atLeastOnce(), NotificationType.REMOVED_FROM_WAITING_LIST);
    }

    @Test
    void shouldDeleteAssignedRegistration() throws Exception {
        var registrationToDelete = createRegistration()
            .withPosition(PositionKeys.DECKHAND)
            .withUserKey(TestUser.TEAM_MEMBER.getKey());
        var event = createEvent()
            .withRegistrations(List.of(
                registrationToDelete,
                createRegistration(),
                createRegistration()
            ));
        event.getSlots().getFirst().setAssignedRegistration(registrationToDelete.getKey());
        saveTestEvent(event);

        webMvc.perform(delete("/api/v1/events/" + event.getKey() + "/registrations/" + registrationToDelete.getKey())
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value(event.getKey().value()))
            .andExpect(jsonPath("$.registrations", iterableWithSize(2)));

        // registration should no longer exist
        assertThat(registrationJpaRepository.findById(registrationToDelete.getKey().value())).isEmpty();

        // notification should have been sent
        verifyEmailsSent(atLeastOnce(), NotificationType.REMOVED_FROM_CREW);
    }

    private Event saveTestEvent(Event event) {
        eventJpaRepository.save(EventJpaEntity.fromDomain(event));
        registrationJpaRepository.saveAll(event.getRegistrations().stream()
            .map(r -> RegistrationJpaEntity.fromDomain(r, event.getKey()))
            .toList());
        return event;
    }
}

