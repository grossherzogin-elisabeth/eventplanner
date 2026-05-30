package org.eventplanner.integration.api.qualifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eventplanner.config.JsonMapperFactory;
import org.eventplanner.events.adapter.jpa.qualifications.QualificationJpaRepository;
import org.eventplanner.events.rest.qualifications.dto.QualificationRepresentation;
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
class CreateQualificationIntegrationTest {

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
        var requestBody = TestResources.getString("/integration/api/qualifications/create-qualification-request.json");
        webMvc.perform(post("/api/v1/qualifications")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthorization() throws Exception {
        var requestBody = TestResources.getString("/integration/api/qualifications/create-qualification-request.json");
        webMvc.perform(post("/api/v1/qualifications")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidRequests() throws Exception {
        var requestBody = "{}";
        webMvc.perform(post("/api/v1/qualifications")
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateNewQualification() throws Exception {
        var jsonMapper = JsonMapperFactory.defaultJsonMapper();

        var requestBody = TestResources.getString("/integration/api/qualifications/create-qualification-request.json");
        var createResponse = webMvc.perform(post("/api/v1/qualifications")
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Test Qualification"))
            .andExpect(jsonPath("$.icon").value("fa-certificate"))
            .andExpect(jsonPath("$.description").value("A test qualification for integration testing"))
            .andExpect(jsonPath("$.expires").value(false))
            .andExpect(jsonPath("$.key").exists())
            .andReturn().getResponse().getContentAsString();

        var created = jsonMapper.readValue(createResponse, QualificationRepresentation.class);

        var persisted = qualificationJpaRepository.findById(created.key());
        assertThat(persisted).isPresent();
        assertThat(persisted.orElseThrow().getName()).isEqualTo("Test Qualification");
        assertThat(persisted.orElseThrow().getIcon()).isEqualTo("fa-certificate");
        assertThat(persisted.orElseThrow().getDescription()).isEqualTo("A test qualification for integration testing");
        assertThat(persisted.orElseThrow().isExpires()).isFalse();
    }
}
