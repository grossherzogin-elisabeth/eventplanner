package org.eventplanner.integration.api.registrations;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;
import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.eventplanner.events.adapter.jpa.events.EventJpaEntity;
import org.eventplanner.events.adapter.jpa.events.EventJpaRepository;
import org.eventplanner.events.adapter.jpa.events.RegistrationJpaEntity;
import org.eventplanner.events.adapter.jpa.events.RegistrationJpaRepository;
import org.eventplanner.events.application.services.AuthenticationService;
import org.eventplanner.events.domain.entities.events.Event;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@AutoConfigureMockMvc
@Transactional // resets db changes after each test
class ConfirmRegistrationIntegrationTest extends EmailSpy {

    private MockMvc webMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EventJpaRepository eventJpaRepository;

    @Autowired
    private RegistrationJpaRepository registrationJpaRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @BeforeEach
    void setup() {
        webMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        webMvc.perform(put("/api/v1/events/" + randomUUID() + "/registrations/" + randomUUID() + "/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthorization() throws Exception {
        var registration = createRegistration().withUserKey(TestUser.USER_WITHOUT_ROLE.getKey());
        registration.setConfirmedAt(null);
        var event = createEvent().withRegistrations(List.of(registration, createRegistration(), createRegistration()));
        saveTestEvent(event);

        webMvc.perform(put("/api/v1/events/" + event.getKey() + "/registrations/" + registration.getKey() + "/confirm")
                .with(withAuthentication(TestUser.USER_WITHOUT_ROLE))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNotFoundForUnknownEvent() throws Exception {
        webMvc.perform(put("/api/v1/events/" + randomUUID() + "/registrations/" + randomUUID() + "/confirm")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundForUnknownRegistration() throws Exception {
        var event = createEvent();
        saveTestEvent(event);

        webMvc.perform(put("/api/v1/events/" + event.getKey() + "/registrations/" + randomUUID() + "/confirm")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldConfirmRegistration() throws Exception {
        var registration = createRegistration().withUserKey(TestUser.TEAM_MEMBER.getKey());
        registration.setConfirmedAt(null);
        var event = createEvent().withRegistrations(List.of(registration, createRegistration(), createRegistration()));
        saveTestEvent(event);

        webMvc.perform(put("/api/v1/events/" + event.getKey() + "/registrations/" + registration.getKey() + "/confirm")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        var updatedRegistration = registrationJpaRepository.findById(registration.getKey().value()).orElseThrow();
        assertThat(updatedRegistration.getConfirmedAt()).isNotNull();
    }

    @Test
    void shouldConfirmRegistrationWithAccessKeyAuthentication() throws Exception {
        var registration = createRegistration().withUserKey(TestUser.TEAM_MEMBER.getKey());
        registration.setConfirmedAt(null);
        var event = createEvent().withRegistrations(List.of(registration, createRegistration(), createRegistration()));
        saveTestEvent(event);

        var accessKey = authenticationService.createAccessKey(TestUser.TEAM_MEMBER.getKey());

        webMvc.perform(put("/api/v1/events/" + event.getKey() + "/registrations/" + registration.getKey() + "/confirm")
                .header("Access-Key", accessKey.value())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        var updatedRegistration = registrationJpaRepository.findById(registration.getKey().value()).orElseThrow();
        assertThat(updatedRegistration.getConfirmedAt()).isNotNull();
    }

    @Test
    void shouldNotConfirmRegistrationOfAnotherUser() throws Exception {
        var registration = createRegistration().withUserKey(TestUser.EVENT_PLANNER.getKey());
        registration.setConfirmedAt(null);
        var event = createEvent().withRegistrations(List.of(registration, createRegistration(), createRegistration()));
        saveTestEvent(event);

        webMvc.perform(put("/api/v1/events/" + event.getKey() + "/registrations/" + registration.getKey() + "/confirm")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());

        var updatedRegistration = registrationJpaRepository.findById(registration.getKey().value()).orElseThrow();
        assertThat(updatedRegistration.getConfirmedAt()).isNull();
    }

    private void saveTestEvent(Event event) {
        eventJpaRepository.save(EventJpaEntity.fromDomain(event));
        registrationJpaRepository.saveAll(event.getRegistrations().stream()
            .map(r -> RegistrationJpaEntity.fromDomain(r, event))
            .toList());
    }
}
