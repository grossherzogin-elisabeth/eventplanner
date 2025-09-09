package org.eventplanner.integration.api;

import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eventplanner.testutil.TestUser;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
class WebConfigIntegrationTest {
    private MockMvc webMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() {
        webMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @ParameterizedTest
    @ValueSource(strings = { "/api/v1/users", "/api/v1/any", "/api/any" })
    void shouldReturnUnauthorizedForMissingAuth(String path) throws Exception {
        webMvc.perform(get(path)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/api/v1/any", "/api/any" })
    void shouldReturnNotFoundForNonExistingEndpoints(String path) throws Exception {
        webMvc.perform(get(path)
                .with(withAuthentication(TestUser.USER_WITHOUT_ROLE))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/api/v1/users", "/api/v1/settings" })
    void shouldReturnForbiddenForUsersWithoutRole(String path) throws Exception {
        webMvc.perform(get(path)
                .with(withAuthentication(TestUser.USER_WITHOUT_ROLE))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "/index.html", "/any", "/any/longer" })
    void shouldFallbackToIndexHtml(String path) throws Exception {
        webMvc.perform(get(path)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html"))
            .andExpect(content().string(Matchers.containsString("This is a dummy index.html for testing")));
    }
}
