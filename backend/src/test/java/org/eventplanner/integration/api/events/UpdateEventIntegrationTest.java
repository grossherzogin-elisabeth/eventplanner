package org.eventplanner.integration.api.events;

import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eventplanner.config.JsonMapperFactory;
import org.eventplanner.events.adapter.jpa.events.EventJpaRepository;
import org.eventplanner.events.rest.events.dto.CreateEventRequest;
import org.eventplanner.events.rest.events.dto.EventRepresentation;
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

    private static final String EVENT_KEY = "7fa48570-963a-4e95-b72f-acaf70c70a24";
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
        var requestBody = TestResources.getString("/integration/api/events/update-event-request.json");
        webMvc.perform(patch("/api/v1/events/" + EVENT_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized());
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
    }

    @Test
    void shouldUpdateEvent() throws Exception {
        JsonMapper jsonMapper = JsonMapperFactory.defaultJsonMapper();

        var requestBody = TestResources.getString("/integration/api/events/update-event-request.json");
        var request = jsonMapper.readValue(requestBody, CreateEventRequest.class);
        var updateEventResponse = webMvc.perform(patch("/api/v1/events/" + EVENT_KEY)
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(request.name()))
            .andExpect(jsonPath("$.note").value(request.note()))
            .andExpect(jsonPath("$.description").value(request.description()))
            .andExpect(jsonPath("$.type").value(request.type()))
            .andExpect(jsonPath("$.signupType").value(request.signupType()))
            .andExpect(jsonPath("$.start").value(request.start()))
            .andExpect(jsonPath("$.end").value(request.end()))
            .andExpect(jsonPath("$.locations").value(request.locations()))
            .andExpect(jsonPath("$.slots").value(request.slots()))
            .andReturn().getResponse().getContentAsString();

        var updatedEvent = jsonMapper.readValue(updateEventResponse, EventRepresentation.class);
        webMvc.perform(get("/api/v1/events/" + updatedEvent.key())
                .with(withAuthentication(TestUser.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(updateEventResponse));
    }
}
