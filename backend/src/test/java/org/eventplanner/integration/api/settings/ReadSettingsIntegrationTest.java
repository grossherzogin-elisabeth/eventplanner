package org.eventplanner.integration.api.settings;

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
class ReadSettingsIntegrationTest {

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
        webMvc.perform(get("/api/v1/settings")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAdminRole() throws Exception {
        webMvc.perform(get("/api/v1/settings")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnSettingsForAdmins() throws Exception {
        var expected = TestResources.getString("/integration/settings/read-settings.json");
        webMvc.perform(get("/api/v1/settings")
                .with(withAuthentication(TestUser.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(expected, JsonCompareMode.STRICT));
    }
}
