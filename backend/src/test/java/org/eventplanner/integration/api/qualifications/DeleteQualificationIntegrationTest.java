package org.eventplanner.integration.api.qualifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eventplanner.events.adapter.jpa.qualifications.QualificationJpaRepository;
import org.eventplanner.testdata.QualificationKeys;
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
class DeleteQualificationIntegrationTest {

    private static final String NON_EXISTING_QUALIFICATION_KEY = "00000000-0000-0000-0000-000000000000";

    private MockMvc webMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private QualificationJpaRepository qualificationJpaRepository;

    @BeforeEach
    void setup() {
        webMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        webMvc.perform(delete("/api/v1/qualifications/some-key"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthorization() throws Exception {
        webMvc.perform(delete("/api/v1/qualifications/some-key")
                .with(withAuthentication(TestUser.TEAM_MEMBER)))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNotFoundForNonExistingQualification() throws Exception {
        webMvc.perform(delete("/api/v1/qualifications/" + NON_EXISTING_QUALIFICATION_KEY)
                .with(withAuthentication(TestUser.ADMIN)))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteQualification() throws Exception {
        var qualificationKey = QualificationKeys.DECK.value();
        assertThat(qualificationJpaRepository.existsById(qualificationKey)).isTrue();

        webMvc.perform(delete("/api/v1/qualifications/" + qualificationKey)
                .with(withAuthentication(TestUser.ADMIN)))
            .andExpect(status().isOk());

        assertThat(qualificationJpaRepository.existsById(qualificationKey)).isFalse();
    }
}
