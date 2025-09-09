package org.eventplanner.integration.api.events;

import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eventplanner.testutil.TestResources;
import org.eventplanner.testutil.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.json.JsonCompareMode;
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
class ReadEventIntegrationTest {

    private MockMvc webMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() {
        webMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void shouldReturnSingleEventAsTeamMember() throws Exception {
        var expected = TestResources.getString("/integration/api/events/read-event-as-team-member.json");
        webMvc.perform(get("/api/v1/events/7fa48570-963a-4e95-b72f-acaf70c70a24")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expected, JsonCompareMode.STRICT));
    }

    @Test
    void shouldReturnSingleEventAsEventPlanner() throws Exception {
        var expected = TestResources.getString("/integration/api/events/read-event-as-event-planner.json");
        webMvc.perform(get("/api/v1/events/7fa48570-963a-4e95-b72f-acaf70c70a24")
                .with(withAuthentication(TestUser.EVENT_PLANNER))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expected, JsonCompareMode.STRICT));
    }

    @Test
    void shouldNotReturnDraftEventForTeamMembers() throws Exception {
        webMvc.perform(get("/api/v1/events/689573b4-20c0-4279-bc2c-43a4d63e2c2b")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnDraftEventForEventPlanners() throws Exception {
        webMvc.perform(get("/api/v1/events/689573b4-20c0-4279-bc2c-43a4d63e2c2b")
                .with(withAuthentication(TestUser.EVENT_PLANNER))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}
