package org.eventplanner.integration.api.positions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eventplanner.config.JsonMapperFactory;
import org.eventplanner.events.adapter.jpa.positions.PositionJpaRepository;
import org.eventplanner.events.rest.positions.dto.PositionRepresentation;
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
class CreatePositionIntegrationTest {

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
        var requestBody = TestResources.getString("/integration/api/positions/create-position-request.json");
        webMvc.perform(post("/api/v1/positions")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthorization() throws Exception {
        var requestBody = TestResources.getString("/integration/api/positions/create-position-request.json");
        webMvc.perform(post("/api/v1/positions")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnValidationErrors() throws Exception {
        var requestBody = "{ \"name\": \"\" }";
        webMvc.perform(post("/api/v1/positions")
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isMap())
            .andExpect(jsonPath("$.errors.name").value("must not be blank"))
            .andExpect(jsonPath("$.errors.color").value("must not be blank"))
            .andExpect(jsonPath("$.errors.prio").value("must not be null"))
            .andExpect(jsonPath("$.errors.imoListRank").value("must not be blank"));
    }

    @Test
    void shouldCreateNewPosition() throws Exception {
        var jsonMapper = JsonMapperFactory.defaultJsonMapper();

        var requestBody = TestResources.getString("/integration/api/positions/create-position-request.json");
        var createResponse = webMvc.perform(post("/api/v1/positions")
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Test Position"))
            .andExpect(jsonPath("$.color").value("#FF0000"))
            .andExpect(jsonPath("$.prio").value(75))
            .andExpect(jsonPath("$.imoListRank").value("TestRank"))
            .andExpect(jsonPath("$.key").exists())
            .andReturn().getResponse().getContentAsString();

        var created = jsonMapper.readValue(createResponse, PositionRepresentation.class);

        var persisted = positionJpaRepository.findById(created.key());
        assertThat(persisted).isPresent();
        assertThat(persisted.orElseThrow().getName()).isEqualTo("Test Position");
        assertThat(persisted.orElseThrow().getColor()).isEqualTo("#FF0000");
        assertThat(persisted.orElseThrow().getPrio()).isEqualTo(75);
        assertThat(persisted.orElseThrow().getImoListRank()).isEqualTo("TestRank");
    }
}
