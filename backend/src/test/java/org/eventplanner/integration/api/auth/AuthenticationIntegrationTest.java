package org.eventplanner.integration.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testutil.TestUser.withAuthentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.eventplanner.events.adapter.jpa.accesskeys.AccessKeyJpaEntity;
import org.eventplanner.events.adapter.jpa.accesskeys.AccessKeyJpaRepository;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.values.auth.AccessKey;
import org.eventplanner.testutil.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@AutoConfigureMockMvc
@Transactional // resets db changes after each test
class AuthenticationIntegrationTest {

    private MockMvc webMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AccessKeyJpaRepository accessKeyJpaRepository;

    @BeforeEach
    void setup() {
        webMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void shouldAuthenticateWithAccessKeyAndConvertToSignedInUser() throws Exception {
        var accessKey = "integration-access-key";
        accessKeyJpaRepository.save(new AccessKeyJpaEntity(
            accessKey,
            TestUser.TEAM_MEMBER.getKey().value(),
            Instant.now().toString()
        ));

        MvcResult result = webMvc.perform(get("/api/v1/account")
                .param("accessKey", accessKey)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value(TestUser.TEAM_MEMBER.getKey().value()))
            .andExpect(jsonPath("$.email").value(TestUser.TEAM_MEMBER.getEmail()))
            .andReturn();

        var authentication = getSessionAuthentication(result);
        assertThat(authentication).isInstanceOf(SignedInUser.class);
        assertThat(((SignedInUser) authentication).authentication()).isInstanceOf(AccessKey.class);
    }

    @Test
    void shouldAuthenticateWithOauthAndConvertToSignedInUser() throws Exception {
        MvcResult result = webMvc.perform(get("/api/v1/account")
                .with(withAuthentication(TestUser.TEAM_MEMBER))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.key").value(TestUser.TEAM_MEMBER.getKey().value()))
            .andExpect(jsonPath("$.email").value(TestUser.TEAM_MEMBER.getEmail()))
            .andReturn();

        var authentication = getSessionAuthentication(result);
        assertThat(authentication).isInstanceOf(SignedInUser.class);
        assertThat(((SignedInUser) authentication).authentication()).isInstanceOf(OidcUser.class);
    }

    private Authentication getSessionAuthentication(MvcResult result) {
        var securityContext = (SecurityContext) result.getRequest()
            .getSession(false)
            .getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertThat(securityContext).isNotNull();
        assertThat(securityContext.getAuthentication()).isNotNull();
        return securityContext.getAuthentication();
    }
}
