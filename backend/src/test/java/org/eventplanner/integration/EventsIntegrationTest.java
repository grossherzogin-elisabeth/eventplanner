package org.eventplanner.integration;

import static org.eventplanner.integration.util.Auth.TestUser.TEAM_MEMBER;
import static org.eventplanner.utils.IsIterableContainingOnly.allMatch;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.eventplanner.integration.util.Auth;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@AutoConfigureMockMvc
@Transactional // resets db changes after each test
class EventsIntegrationTest {
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
    void shouldRequireAuthentication() throws Exception {
        webMvc.perform(get("/api/v1/events?year=2025")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldListEvents() throws Exception {
        webMvc.perform(get("/api/v1/events?year=2025")
                .with(Auth.withAuthentication(TEAM_MEMBER))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].key").value("7fa48570-963a-4e95-b72f-acaf70c70a24"))
            .andExpect(jsonPath("$[1].key").value("98b55fb5-7f10-42c7-9b94-2e5c9b92f264"));
    }

    @Test
    void shouldReturnSingleEventAsTeamMember() throws Exception {
        webMvc.perform(get("/api/v1/events/7fa48570-963a-4e95-b72f-acaf70c70a24")
                .with(Auth.withAuthentication(Auth.TestUser.TEAM_MEMBER))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.registrations[*]", doesNotContainPrivateUserData(Auth.TestUser.TEAM_MEMBER)));
    }

    public static Matcher<Iterable<? super Map<String, Object>>> doesNotContainPrivateUserData(Auth.TestUser user) {
        return allMatch(new RegistrationMatcher(user));
    }

    @RequiredArgsConstructor
    static class RegistrationMatcher extends BaseMatcher<Map<String, Object>> {
        private final Auth.TestUser user;

        @Override
        public boolean matches(final Object actual) {
            if (actual instanceof Map<?, ?> attributes) {
                var userKey = attributes.get("userKey");
                if (user.getOidcId().equals(userKey)) {
                    return true;
                }
                return !attributes.containsKey("note")
                    && !attributes.containsKey("overnightStay")
                    && !attributes.containsKey("confirmed")
                    && !attributes.containsKey("arrival");
            }
            return false;
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("items without private data of other users");
        }
    }
}
