package org.eventplanner.integration.api.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eventplanner.events.adapter.jpa.users.EncrypedUserDetailsJpaRepository;
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
class UpdateUserIntegrationTest {

    private static final String NON_EXISTING_USER_KEY = "00000000-0000-0000-0000-000000000000";

    private MockMvc webMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EncrypedUserDetailsJpaRepository encrypedUserDetailsJpaRepository;

    @BeforeEach
    void setup() {
        webMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        var requestBody = TestResources.getString("/integration/api/users/update-user-request.json");
        webMvc.perform(patch("/api/v1/users/" + TestUser.USER_WITHOUT_ROLE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthorization() throws Exception {
        var requestBody = TestResources.getString("/integration/api/users/update-user-request.json");
        webMvc.perform(patch("/api/v1/users/" + TestUser.USER_WITHOUT_ROLE)
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnValidationErrors() throws Exception {
        var requestBody = """
            {
              "roles": ["ROLE_TEAM_MEMBER", "INVALID"],
              "diet": "INVALID",
              "email": "invalid-email",
              "dateOfBirth": "invalid-date",
              "verifiedAt": "invalid-date"
            }
            """;
        webMvc.perform(patch("/api/v1/users/" + TestUser.USER_WITHOUT_ROLE)
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isMap())
            .andExpect(jsonPath("$.errors.diet").value("invalid value"))
            .andExpect(jsonPath("$.errors.roles").value("invalid value"))
            .andExpect(jsonPath("$.errors.email").value("must be a well-formed email address"))
            .andExpect(jsonPath("$.errors.dateOfBirth").value("must conform with ISO 8601 date format 2007-12-03"))
            .andExpect(jsonPath("$.errors.verifiedAt").value(
                "must conform with ISO 8601 datetime format 2007-12-03T10:15:30.00Z"));
    }

    @Test
    void shouldReturnNotFoundForNonExistingUser() throws Exception {
        var requestBody = TestResources.getString("/integration/api/users/update-user-request.json");
        webMvc.perform(patch("/api/v1/users/" + NON_EXISTING_USER_KEY)
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateUser() throws Exception {
        var userKey = TestUser.USER_WITHOUT_ROLE.getOidcId();
        var before = encrypedUserDetailsJpaRepository.findById(userKey).orElseThrow();
        var beforeFirstName = before.getFirstName();
        var beforeLastName = before.getLastName();
        var beforeEmail = before.getEmail();
        var beforeRoles = before.getRolesRaw();
        var beforeUpdatedAt = before.getUpdatedAt();

        var requestBody = TestResources.getString("/integration/api/users/update-user-request.json");
        webMvc.perform(patch("/api/v1/users/" + userKey)
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value(userKey))
            .andExpect(jsonPath("$.firstName").value("Updated"))
            .andExpect(jsonPath("$.lastName").value("User"))
            .andExpect(jsonPath("$.email").value("updated.user@email.com"))
            .andExpect(jsonPath("$.roles").isArray())
            .andExpect(jsonPath("$.roles[0]").value("ROLE_TEAM_MEMBER"));

        var after = encrypedUserDetailsJpaRepository.findById(userKey).orElseThrow();
        assertThat(after.getUpdatedAt()).isNotEqualTo(beforeUpdatedAt);
        assertThat(after.getFirstName()).isNotEqualTo(beforeFirstName);
        assertThat(after.getLastName()).isNotEqualTo(beforeLastName);
        assertThat(after.getEmail()).isNotEqualTo(beforeEmail);
        assertThat(after.getRolesRaw()).isNotEqualTo(beforeRoles);
        assertThat(after.getRolesRaw()).isNotEqualTo("[]");
    }
}

