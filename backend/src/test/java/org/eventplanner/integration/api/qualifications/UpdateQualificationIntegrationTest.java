package org.eventplanner.integration.api.qualifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eventplanner.events.adapter.jpa.qualifications.QualificationJpaRepository;
import org.eventplanner.testdata.QualificationKeys;
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
class UpdateQualificationIntegrationTest {

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
        var requestBody = TestResources.getString("/integration/api/qualifications/update-qualification-request.json");
        webMvc.perform(put("/api/v1/qualifications/some-key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthorization() throws Exception {
        var requestBody = TestResources.getString("/integration/api/qualifications/update-qualification-request.json");
        webMvc.perform(put("/api/v1/qualifications/some-key")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNotFoundForNonExistingQualification() throws Exception {
        var requestBody = TestResources.getString("/integration/api/qualifications/update-qualification-request.json");
        webMvc.perform(put("/api/v1/qualifications/" + NON_EXISTING_QUALIFICATION_KEY)
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateQualification() throws Exception {
        var qualificationKey = QualificationKeys.DECK.value();

        var updateRequest =
            TestResources.getString("/integration/api/qualifications/update-qualification-request.json");
        webMvc.perform(put("/api/v1/qualifications/" + qualificationKey)
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value(qualificationKey))
            .andExpect(jsonPath("$.name").value("Updated Qualification"))
            .andExpect(jsonPath("$.icon").value("fa-updated"))
            .andExpect(jsonPath("$.description").value("Updated description"))
            .andExpect(jsonPath("$.expires").value(true));

        var persisted = qualificationJpaRepository.findById(qualificationKey);
        assertThat(persisted).isPresent();
        assertThat(persisted.orElseThrow().getName()).isEqualTo("Updated Qualification");
        assertThat(persisted.orElseThrow().getIcon()).isEqualTo("fa-updated");
        assertThat(persisted.orElseThrow().getDescription()).isEqualTo("Updated description");
        assertThat(persisted.orElseThrow().isExpires()).isTrue();
    }
}
