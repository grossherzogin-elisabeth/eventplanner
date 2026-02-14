package org.eventplanner.integration.api.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.eventplanner.config.ObjectMapperFactory;
import org.eventplanner.events.adapter.jpa.users.EncrypedUserDetailsJpaRepository;
import org.eventplanner.events.rest.users.dto.CreateUserRequest;
import org.eventplanner.events.rest.users.dto.UserDetailsRepresentation;
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
import tools.jackson.databind.ObjectMapper;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@AutoConfigureMockMvc
@Transactional // resets db changes after each test
class CreateUserIntegrationTest {

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
        var requestBody = TestResources.getString("/integration/api/users/create-user-request.json");
        webMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireAuthorization() throws Exception {
        var requestBody = TestResources.getString("/integration/api/users/create-user-request.json");
        webMvc.perform(post("/api/v1/users")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidRequests() throws Exception {
        var requestBody = "{}";
        webMvc.perform(post("/api/v1/users")
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors").isMap())
            .andExpect(jsonPath("$.errors.firstName").value("must not be blank"))
            .andExpect(jsonPath("$.errors.lastName").value("must not be blank"))
            .andExpect(jsonPath("$.errors.email").value("must not be blank"));
    }

    @Test
    void shouldCreateNewUser() throws Exception {
        ObjectMapper objectMapper = ObjectMapperFactory.defaultObjectMapper();

        var requestBody = TestResources.getString("/integration/api/users/create-user-request.json");
        var request = objectMapper.readValue(requestBody, CreateUserRequest.class);
        var createUserResponse = webMvc.perform(post("/api/v1/users")
                .with(withAuthentication(TestUser.ADMIN))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value(request.firstName()))
            .andExpect(jsonPath("$.lastName").value(request.lastName()))
            .andExpect(jsonPath("$.email").value(request.email()))
            .andReturn().getResponse().getContentAsString();

        // verify user exist in database
        var createdUser = objectMapper.readValue(createUserResponse, UserDetailsRepresentation.class);
        assertThat(encrypedUserDetailsJpaRepository.existsById(createdUser.key())).isTrue();

        // verify create user returns same response as the user details request
        webMvc.perform(get("/api/v1/users/" + createdUser.key())
                .with(withAuthentication(TestUser.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(createUserResponse));
    }
}
