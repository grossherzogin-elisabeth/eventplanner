package org.eventplanner.integration.api.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eventplanner.events.adapter.jpa.users.EncrypedUserDetailsJpaRepository;
import org.eventplanner.testutil.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
class DeleteUserIntegrationTest {

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
        webMvc.perform(delete("/api/v1/users/" + TestUser.USER_WITHOUT_ROLE))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthorization() throws Exception {
        webMvc.perform(delete("/api/v1/users/" + TestUser.USER_WITHOUT_ROLE)
                .with(withAuthentication(TestUser.TEAM_MEMBER)))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNotFoundForNonExistingUser() throws Exception {
        webMvc.perform(delete("/api/v1/users/" + NON_EXISTING_USER_KEY)
                .with(withAuthentication(TestUser.ADMIN)))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteUser() throws Exception {
        var userKey = TestUser.USER_WITHOUT_ROLE.getOidcId();
        assertThat(encrypedUserDetailsJpaRepository.existsById(userKey)).isTrue();

        webMvc.perform(delete("/api/v1/users/" + userKey)
                .with(withAuthentication(TestUser.ADMIN)))
            .andExpect(status().isOk());

        assertThat(encrypedUserDetailsJpaRepository.existsById(userKey)).isFalse();
    }
}

