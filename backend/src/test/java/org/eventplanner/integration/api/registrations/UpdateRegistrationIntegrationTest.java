package org.eventplanner.integration.api.registrations;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;
import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.eventplanner.events.adapter.jpa.events.EventJpaEntity;
import org.eventplanner.events.adapter.jpa.events.EventJpaRepository;
import org.eventplanner.events.adapter.jpa.events.RegistrationJpaEntity;
import org.eventplanner.events.adapter.jpa.events.RegistrationJpaRepository;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.testdata.EventFactory;
import org.eventplanner.testdata.PositionKeys;
import org.eventplanner.testutil.EmailSpy;
import org.eventplanner.testutil.TestResources;
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
class UpdateRegistrationIntegrationTest extends EmailSpy {

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
        var event = EventFactory.createEvent();
        var registration = event.getRegistrations().getFirst();
        saveTestEvent(event);

        var requestBody = TestResources.getString("/integration/api/registrations/update-registration-request.json");
        requestBody = requestBody.replace("$registrationKey", registration.getKey().value());
        requestBody = requestBody.replace("$positionKey", PositionKeys.DECKHAND.value());
        requestBody = requestBody.replace("$userKey", registration.getUserKey().value());
        requestBody = requestBody.replace("$arrival", "2025-10-02");

        webMvc.perform(put("/api/v1/events/" + event.getKey() + "/registrations/" + registration.getKey())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnNotFoundForUnknownEvent() throws Exception {
        var requestBody = TestResources.getString("/integration/api/registrations/update-registration-request.json");
        var registrationKey = randomUUID().toString();
        requestBody = requestBody.replace("$registrationKey", registrationKey);
        requestBody = requestBody.replace("$positionKey", PositionKeys.DECKHAND.value());
        requestBody = requestBody.replace("$userKey", TestUser.EVENT_PLANNER.getOidcId());
        requestBody = requestBody.replace("$arrival", "2025-10-02");

        webMvc.perform(put("/api/v1/events/" + randomUUID() + "/registrations/" + registrationKey)
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForUnknownRegistration() throws Exception {
        var event = EventFactory.createEvent();
        eventJpaRepository.save(EventJpaEntity.fromDomain(event));

        var nonExistingRegistration = createRegistration();
        var requestBody = TestResources.getString("/integration/api/registrations/update-registration-request.json");
        requestBody = requestBody.replace("$registrationKey", nonExistingRegistration.getKey().value());
        requestBody = requestBody.replace("$positionKey", PositionKeys.DECKHAND.value());
        requestBody = requestBody.replace("$userKey", TestUser.EVENT_PLANNER.getOidcId());
        requestBody = requestBody.replace("$arrival", event.getStart().toString().substring(0, 10));

        webMvc.perform(put("/api/v1/events/" + event.getKey() + "/registrations/" + nonExistingRegistration.getKey())
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldBlockUpdateOfOtherUsersRegistration() throws Exception {
        var event = EventFactory.createEvent();
        eventJpaRepository.save(EventJpaEntity.fromDomain(event));
        var registration = event.getRegistrations().getFirst();
        saveTestEvent(event);

        var requestBody = TestResources.getString("/integration/api/registrations/update-registration-request.json");
        requestBody = requestBody.replace("$registrationKey", registration.getKey().value());
        requestBody = requestBody.replace("$positionKey", PositionKeys.DECKHAND.value());
        requestBody = requestBody.replace("$userKey", TestUser.EVENT_PLANNER.getOidcId());
        requestBody = requestBody.replace("$arrival", event.getStart().toString().substring(0, 10));

        webMvc.perform(put("/api/v1/events/" + event.getKey() + "/registrations/" + registration.getKey())
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldUpdateRegistration() throws Exception {
        var registration = createRegistration()
            .withPosition(PositionKeys.DECKHAND)
            .withUserKey(TestUser.TEAM_MEMBER.getKey());
        var event = createEvent()
            .withRegistrations(List.of(
                registration,
                createRegistration(),
                createRegistration()
            ));
        saveTestEvent(event);

        var requestBody = TestResources.getString("/integration/api/registrations/update-registration-request.json");
        requestBody = requestBody.replace("$registrationKey", registration.getKey().value());
        requestBody = requestBody.replace("$positionKey", PositionKeys.MATE.value());
        requestBody = requestBody.replace("$userKey", registration.getUserKey().value());
        requestBody = requestBody.replace("$arrival", event.getStart().toString().substring(0, 10));

        webMvc.perform(put("/api/v1/events/" + event.getKey() + "/registrations/" + registration.getKey())
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value(event.getKey().value()));

        var savedRegistration = registrationJpaRepository.findById(registration.getKey().value()).orElseThrow();
        assertThat(savedRegistration.getPositionKey()).isEqualTo(PositionKeys.MATE.value());
        assertThat(savedRegistration.getArrival()).isEqualTo(event.getStart().toString().substring(0, 10));
        assertThat(savedRegistration.getNote()).isEqualTo("Updated note");
    }

    private Event saveTestEvent(Event event) {
        eventJpaRepository.save(EventJpaEntity.fromDomain(event));
        registrationJpaRepository.saveAll(event.getRegistrations().stream()
            .map(r -> RegistrationJpaEntity.fromDomain(r, event.getKey()))
            .toList());
        return event;
    }
}

