package org.eventplanner.integration.api.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eventplanner.config.JsonMapperFactory;
import org.eventplanner.events.adapter.jpa.events.EventJpaEntity;
import org.eventplanner.events.adapter.jpa.events.EventJpaRepository;
import org.eventplanner.events.rest.events.dto.EventLocationRepresentation;
import org.eventplanner.events.rest.events.dto.EventRepresentation;
import org.eventplanner.events.rest.events.dto.EventSlotRepresentation;
import org.eventplanner.events.rest.events.dto.UpdateEventRequest;
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

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@AutoConfigureMockMvc
@Transactional // resets db changes after each test
class UpdateEventIntegrationTest {

    private static final JsonMapper JSON_MAPPER = JsonMapperFactory.defaultJsonMapper();
    private static final String EVENT_KEY = "7fa48570-963a-4e95-b72f-acaf70c70a24";
    private static final String NON_EXISTING_EVENT_KEY = "00000000-0000-0000-0000-000000000000";

    private MockMvc webMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EventJpaRepository eventJpaRepository;

    private EventJpaEntity eventBeforeUpdate;

    @BeforeEach
    void setup() {
        webMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
        eventBeforeUpdate = eventJpaRepository.findById(EVENT_KEY).orElseThrow();
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        var requestBody = TestResources.getString("/integration/api/events/update-event-request.json");
        webMvc.perform(patch("/api/v1/events/" + EVENT_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized());
        var eventAfterUpdate = eventJpaRepository.findById(EVENT_KEY).orElseThrow();
        assertThat(eventAfterUpdate).isEqualTo(eventBeforeUpdate);
    }

    @Test
    void shouldRequireAuthorization() throws Exception {
        var requestBody = TestResources.getString("/integration/api/events/update-event-request.json");
        webMvc.perform(patch("/api/v1/events/" + EVENT_KEY)
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
        var eventAfterUpdate = eventJpaRepository.findById(EVENT_KEY).orElseThrow();
        assertThat(eventAfterUpdate).isEqualTo(eventBeforeUpdate);
    }

    @Test
    void shouldReturnValidationErrors() throws Exception {
        var requestBody = TestResources.getString("/integration/api/events/update-event-invalid-request.json");
        webMvc.perform(patch("/api/v1/events/" + EVENT_KEY)
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.type").isNotEmpty())
            .andExpect(jsonPath("$.errors.signupType").isNotEmpty())
            .andExpect(jsonPath("$.errors.start").isNotEmpty())
            .andExpect(jsonPath("$.errors.end").isNotEmpty());
        var eventAfterUpdate = eventJpaRepository.findById(EVENT_KEY).orElseThrow();
        assertThat(eventAfterUpdate).isEqualTo(eventBeforeUpdate);
    }

    @Test
    void shouldReturnNotFoundForNonExistingEvent() throws Exception {
        var requestBody = TestResources.getString("/integration/api/events/update-event-request.json");
        webMvc.perform(patch("/api/v1/events/" + NON_EXISTING_EVENT_KEY)
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound());
        var eventAfterUpdate = eventJpaRepository.findById(EVENT_KEY).orElseThrow();
        assertThat(eventAfterUpdate).isEqualTo(eventBeforeUpdate);
    }

    @Test
    void shouldUpdateDefaultEventFields() throws Exception {
        var requestBody = TestResources.getString("/integration/api/events/update-event-request.json");
        var request = JSON_MAPPER.readValue(requestBody, UpdateEventRequest.class);
        var updateEventResponse = webMvc.perform(patch("/api/v1/events/" + EVENT_KEY)
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(request.name()))
            .andExpect(jsonPath("$.type").value(request.type()))
            .andExpect(jsonPath("$.signupType").value(request.signupType()))
            .andExpect(jsonPath("$.state").value(request.state()))
            .andExpect(jsonPath("$.note").value(request.note()))
            .andExpect(jsonPath("$.description").value(request.description()))
            .andExpect(jsonPath("$.start").value(request.start()))
            .andExpect(jsonPath("$.end").value(request.end()))
            .andReturn().getResponse().getContentAsString();

        var updatedEvent = JSON_MAPPER.readValue(updateEventResponse, EventRepresentation.class);
        webMvc.perform(get("/api/v1/events/" + updatedEvent.key())
                .with(withAuthentication(TestUser.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(updateEventResponse));

        var eventAfterUpdate = eventJpaRepository.findById(EVENT_KEY).orElseThrow();
        assertThat(eventAfterUpdate.getName()).isEqualTo(request.name());
        assertThat(eventAfterUpdate.getType()).isEqualTo(request.type());
        assertThat(eventAfterUpdate.getSignupType()).isEqualTo(request.signupType());
        assertThat(eventAfterUpdate.getState()).isEqualTo(request.state());
        assertThat(eventAfterUpdate.getNote()).isEqualTo(request.note());
        assertThat(eventAfterUpdate.getDescription()).isEqualTo(request.description());
        assertThat(eventAfterUpdate.getStart()).isEqualTo(request.start());
        assertThat(eventAfterUpdate.getEnd()).isEqualTo(request.end());
    }

    @Test
    void shouldUpdateLocations() throws Exception {
        var requestBody = TestResources.getString("/integration/api/events/update-event-locations-request.json");
        var request = JSON_MAPPER.readValue(requestBody, UpdateEventRequest.class);

        var updateEventResponse = webMvc.perform(patch("/api/v1/events/" + EVENT_KEY)
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        var updatedEvent = JSON_MAPPER.readValue(updateEventResponse, EventRepresentation.class);
        assertThat(updatedEvent.locations()).isEqualTo(request.locations());

        var expectedLocations = EventJpaEntity.serializeLocations(
            request.locations().stream().map(EventLocationRepresentation::toDomain).toList()
        );
        var eventAfterUpdate = eventJpaRepository.findById(EVENT_KEY).orElseThrow();
        assertThat(eventAfterUpdate.getLocationsRaw()).isEqualTo(expectedLocations);
    }

    @Test
    void shouldUpdateSlots() throws Exception {
        var requestBody = TestResources.getString("/integration/api/events/update-event-slots-request.json");
        var request = JSON_MAPPER.readValue(requestBody, UpdateEventRequest.class);

        var updateEventResponse = webMvc.perform(patch("/api/v1/events/" + EVENT_KEY)
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        var updatedEvent = JSON_MAPPER.readValue(updateEventResponse, EventRepresentation.class);
        assertThat(updatedEvent.slots()).isEqualTo(request.slots());

        var expectedSlots = EventJpaEntity.serializeSlots(
            request.slots().stream().map(EventSlotRepresentation::toDomain).toList()
        );
        var eventAfterUpdate = eventJpaRepository.findById(EVENT_KEY).orElseThrow();
        assertThat(eventAfterUpdate.getSlotsRaw()).isEqualTo(expectedSlots);
    }
}
