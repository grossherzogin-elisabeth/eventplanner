package org.eventplanner.integration.api.registrations;

import static java.util.UUID.randomUUID;
import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eventplanner.events.adapter.jpa.events.EventJpaEntity;
import org.eventplanner.events.adapter.jpa.events.EventJpaRepository;
import org.eventplanner.events.domain.values.notifications.NotificationType;
import org.eventplanner.testdata.EventFactory;
import org.eventplanner.testdata.PositionKeys;
import org.eventplanner.testutil.EmailSpy;
import org.eventplanner.testutil.TestResources;
import org.eventplanner.testutil.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class CreateRegistrationIntegrationTest extends EmailSpy {

    private MockMvc webMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EventJpaRepository eventJpaRepository;

    @BeforeEach
    void setup() {
        webMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        var requestBody = TestResources.getString("/integration/api/registrations/create-registration-request.json");
        requestBody = requestBody.replace("$userKey", randomUUID().toString());
        requestBody = requestBody.replace("$arrival", "2025-10-02");

        webMvc.perform(post("/api/v1/events/" + randomUUID() + "/registrations")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRequestsForUnknownEvents() throws Exception {
        var requestBody = "{}";
        webMvc.perform(post("/api/v1/events/" + randomUUID() + "/registrations")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Event does not exist"));
    }

    @Test
    void shouldRejectRequestsWithoutUserKey() throws Exception {
        var event = EventFactory.createEvent();
        eventJpaRepository.save(EventJpaEntity.fromDomain(event));

        var requestBody = "{}";
        webMvc.perform(post("/api/v1/events/" + event.getKey() + "/registrations")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Either a user key or a name must be provided"));
    }

    @Test
    void shouldBlockRegistrationOfOtherUsers() throws Exception {
        var event = EventFactory.createEvent();
        eventJpaRepository.save(EventJpaEntity.fromDomain(event));

        var requestBody = TestResources.getString("/integration/api/registrations/create-registration-request.json");
        requestBody = requestBody.replace("$userKey", TestUser.EVENT_PLANNER.getOidcId());
        requestBody = requestBody.replace("$positionKey", PositionKeys.DECKSHAND.value());
        requestBody = requestBody.replace("$arrival", event.getStart().toString());

        webMvc.perform(post("/api/v1/events/" + event.getKey() + "/registrations")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());

        verifyEmailsSent(never(), NotificationType.ADDED_TO_WAITING_LIST);
        verifyEmailsSent(never(), NotificationType.CREW_REGISTRATION_ADDED);
    }

    @Test
    void shouldCreateRegistrationAndSendNotifications() throws Exception {
        var event = EventFactory.createEvent();
        eventJpaRepository.save(EventJpaEntity.fromDomain(event));

        var requestBody = TestResources.getString("/integration/api/registrations/create-registration-request.json");
        requestBody = requestBody.replace("$userKey", TestUser.TEAM_MEMBER.getOidcId());
        requestBody = requestBody.replace("$positionKey", PositionKeys.DECKSHAND.value());
        requestBody = requestBody.replace("$arrival", event.getStart().toString());

        webMvc.perform(post("/api/v1/events/" + event.getKey() + "/registrations")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.key").value(event.getKey().value()))
            .andExpect(jsonPath("$.registrations", iterableWithSize(1)))
            .andExpect(jsonPath("$.registrations.*.userKey").value(TestUser.TEAM_MEMBER.getOidcId()));

        // Webhooks sent can be verified at
        // https://webhook.site/#!/view/9e2b7496-3983-46cb-82df-57f4d9d9a964/d05d45ac-d580-4a32-b2dd-26df997dc911/1
        verifyEmailSent(NotificationType.ADDED_TO_WAITING_LIST, TestUser.TEAM_MEMBER);
        verifyEmailsSent(atLeastOnce(), NotificationType.CREW_REGISTRATION_ADDED);
    }
}
