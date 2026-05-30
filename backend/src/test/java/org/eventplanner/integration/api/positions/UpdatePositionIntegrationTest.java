package org.eventplanner.integration.api.positions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eventplanner.events.adapter.jpa.positions.PositionJpaRepository;
import org.eventplanner.testdata.PositionKeys;
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
class UpdatePositionIntegrationTest {

    private static final String NON_EXISTING_POSITION_KEY = "00000000-0000-0000-0000-000000000000";

    private MockMvc webMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PositionJpaRepository positionJpaRepository;

    @BeforeEach
    void setup() {
        webMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        var requestBody = TestResources.getString("/integration/api/positions/update-position-request.json");
        webMvc.perform(put("/api/v1/positions/some-key")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthorization() throws Exception {
        var requestBody = TestResources.getString("/integration/api/positions/update-position-request.json");
        webMvc.perform(put("/api/v1/positions/some-key")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNotFoundForNonExistingPosition() throws Exception {
        var requestBody = TestResources.getString("/integration/api/positions/update-position-request.json");
        webMvc.perform(put("/api/v1/positions/" + NON_EXISTING_POSITION_KEY)
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdatePosition() throws Exception {
        var positionKey = PositionKeys.TRAINER.value();

        var updateRequest = TestResources.getString("/integration/api/positions/update-position-request.json");
        webMvc.perform(put("/api/v1/positions/" + positionKey)
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(updateRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value(positionKey))
            .andExpect(jsonPath("$.name").value("Updated Position"))
            .andExpect(jsonPath("$.color").value("#00FF00"))
            .andExpect(jsonPath("$.prio").value(95))
            .andExpect(jsonPath("$.imoListRank").value("UpdatedRank"));

        var persisted = positionJpaRepository.findById(positionKey);
        assertThat(persisted).isPresent();
        assertThat(persisted.orElseThrow().getName()).isEqualTo("Updated Position");
        assertThat(persisted.orElseThrow().getColor()).isEqualTo("#00FF00");
        assertThat(persisted.orElseThrow().getPrio()).isEqualTo(95);
        assertThat(persisted.orElseThrow().getImoListRank()).isEqualTo("UpdatedRank");
    }
}
