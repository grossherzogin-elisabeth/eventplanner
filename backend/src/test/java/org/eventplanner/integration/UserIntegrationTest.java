package org.eventplanner.integration;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

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
class UserIntegrationTest {

    private MockMvc webMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() throws IOException {
        webMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void shouldReturnForbidden() throws Exception {
        webMvc.perform(get("/api/v1/users")
                .with(Auth.withAuthentication(Auth.TestUser.USER_WITHOUT_ROLE))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorized() throws Exception {
        webMvc.perform(get("/api/v1/users")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnListOfMinimalUsers() throws Exception {
        webMvc.perform(get("/api/v1/users")
                .with(Auth.withAuthentication(Auth.TestUser.TEAM_MEMBER))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(
                """
                    [
                      { "key": "3449174e-c8e1-49b7-8ef0-35c8c5f23a0c", "firstName": "Bruce", "nickName": null, "lastName": "Benner" },
                      { "key": "80717734-d546-4b7b-a756-5f4f266b5ae6", "firstName": "Stephen", "nickName": "Doctor", "lastName": "Strange" },
                      { "key": "3f18dea3-44fa-461d-b5c0-d101bf4f1b98", "firstName": "Anthony", "nickName": "Tony", "lastName": "Stark" },
                      { "key": "b0c4bf2d-d390-4490-8a77-59214eb9a81c", "firstName": "Loki", "nickName": null, "lastName": "Odinson" },
                      { "key": "5bfe85c9-f0e6-4033-9ba3-cfdb677ddaee", "firstName": "Natasha", "nickName": null, "lastName": "Romanoff" },
                      { "key": "684e0bf5-c720-4c9a-a95d-9378351c35ee", "firstName": "Nick", "nickName": null, "lastName": "Fury" },
                      { "key": "c45ba65f-fb7f-474d-b86b-ca6f3143084a", "firstName": "Peter", "nickName": null, "lastName": "Parker" },
                      { "key": "fc766e6e-dec2-4fc8-89ef-4c158786fd4e", "firstName": "Steven", "nickName": "Steve", "lastName": "Rogers" },
                      { "key": "9ae78599-5bc0-4a6d-a164-1ba59ec04adc", "firstName": "Thor", "nickName": null, "lastName": "Odinson" },
                      { "key": "61741a06-58d7-43a9-b55a-80d07f281bba", "firstName": "Wanda", "nickName": null, "lastName": "Maximoff" }
                    ]
                    """, JsonCompareMode.STRICT
            ));
    }

    @Test
    void shouldReturnListOfExtendedUsers() throws Exception {
        webMvc.perform(get("/api/v1/users")
                .with(Auth.withAuthentication(Auth.TestUser.ADMIN))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(
                """
                    [
                      {
                        "key": "3449174e-c8e1-49b7-8ef0-35c8c5f23a0c",
                        "firstName": "Bruce",
                        "nickName": null,
                        "lastName": "Benner",
                        "positions": [
                          "08c609d2-26c9-4848-b17b-8feac2e2a91f"
                        ],
                        "roles": [
                          "ROLE_TEAM_MEMBER"
                        ],
                        "qualifications": [
                          {
                            "qualificationKey": "659fccf8-2bfc-4123-b7cd-98be4b8e4f8d",
                            "expiresAt": null,
                            "expires": false
                          }
                        ],
                        "email": "bruce.benner@test.email",
                        "verified": false
                      },
                      {
                        "key": "80717734-d546-4b7b-a756-5f4f266b5ae6",
                        "firstName": "Stephen",
                        "nickName": "Doctor",
                        "lastName": "Strange",
                        "positions": [
                          "3ea10356-8f2f-49df-abc8-b9ecd1a6934e"
                        ],
                        "roles": [
                          "ROLE_TEAM_MEMBER",
                          "ROLE_TEAM_PLANNER"
                        ],
                        "qualifications": [
                          {
                            "qualificationKey": "d470b84e-4da8-40cd-87bd-05c0b0421927",
                            "expiresAt": null,
                            "expires": false
                          }
                        ],
                        "email": "stephen.strange@email.de",
                        "verified": false
                      },
                      {
                        "key": "3f18dea3-44fa-461d-b5c0-d101bf4f1b98",
                        "firstName": "Anthony",
                        "nickName": "Tony",
                        "lastName": "Stark",
                        "positions": [
                          "08c609d2-26c9-4848-b17b-8feac2e2a91f"
                        ],
                        "roles": [
                          "ROLE_ADMIN",
                          "ROLE_TEAM_MEMBER"
                        ],
                        "qualifications": [
                          {
                            "qualificationKey": "659fccf8-2bfc-4123-b7cd-98be4b8e4f8d",
                            "expiresAt": null,
                            "expires": false
                          }
                        ],
                        "email": "tony.stark@test.email",
                        "verified": false
                      },
                      {
                        "key": "b0c4bf2d-d390-4490-8a77-59214eb9a81c",
                        "firstName": "Loki",
                        "nickName": null,
                        "lastName": "Odinson",
                        "positions": [],
                        "roles": [],
                        "qualifications": [],
                        "email": "loki.odinson@asgard.email",
                        "verified": false
                      },
                      {
                        "key": "5bfe85c9-f0e6-4033-9ba3-cfdb677ddaee",
                        "firstName": "Natasha",
                        "nickName": null,
                        "lastName": "Romanoff",
                        "positions": [
                          "24dd5aef-e819-4d9d-8b19-d9d592c4ff8f"
                        ],
                        "roles": [
                          "ROLE_TEAM_MEMBER"
                        ],
                        "qualifications": [
                          {
                            "qualificationKey": "225f84e5-ebc1-4e76-8ada-98df9f8bf4ad",
                            "expiresAt": null,
                            "expires": false
                          }
                        ],
                        "email": "natasha.romanoff@test.email",
                        "verified": false
                      },
                      {
                        "key": "684e0bf5-c720-4c9a-a95d-9378351c35ee",
                        "firstName": "Nick",
                        "nickName": null,
                        "lastName": "Fury",
                        "positions": [],
                        "roles": [
                          "ROLE_USER_MANAGER",
                          "ROLE_TEAM_PLANNER",
                          "ROLE_EVENT_PLANNER"
                        ],
                        "qualifications": [],
                        "email": "nick.fury@test.email",
                        "verified": false
                      },
                      {
                        "key": "c45ba65f-fb7f-474d-b86b-ca6f3143084a",
                        "firstName": "Peter",
                        "nickName": null,
                        "lastName": "Parker",
                        "positions": [
                          "24dd5aef-e819-4d9d-8b19-d9d592c4ff8f"
                        ],
                        "roles": [
                          "ROLE_TEAM_MEMBER"
                        ],
                        "qualifications": [
                          {
                            "qualificationKey": "225f84e5-ebc1-4e76-8ada-98df9f8bf4ad",
                            "expiresAt": null,
                            "expires": false
                          }
                        ],
                        "email": "peter.parker@test.email",
                        "verified": false
                      },
                      {
                        "key": "fc766e6e-dec2-4fc8-89ef-4c158786fd4e",
                        "firstName": "Steven",
                        "nickName": "Steve",
                        "lastName": "Rogers",
                        "positions": [
                          "4b2ff7f9-9ec9-44d0-8de3-57bbefed2545"
                        ],
                        "roles": [
                          "ROLE_TEAM_MEMBER",
                          "ROLE_TEAM_PLANNER"
                        ],
                        "qualifications": [
                          {
                            "qualificationKey": "7e819522-6103-491c-8ef0-cbc0c2c208dd",
                            "expiresAt": "2078-12-01T10:39:00Z",
                            "expires": true
                          }
                        ],
                        "email": "captain.america@test.email",
                        "verified": true
                      },
                      {
                        "key": "9ae78599-5bc0-4a6d-a164-1ba59ec04adc",
                        "firstName": "Thor",
                        "nickName": null,
                        "lastName": "Odinson",
                        "positions": [
                          "3ea10356-8f2f-49df-abc8-b9ecd1a6934e"
                        ],
                        "roles": [
                          "ROLE_TEAM_MEMBER"
                        ],
                        "qualifications": [
                          {
                            "qualificationKey": "d470b84e-4da8-40cd-87bd-05c0b0421927",
                            "expiresAt": null,
                            "expires": false
                          }
                        ],
                        "email": "thor.odinson@asgard.email",
                        "verified": false
                      },
                      {
                        "key": "61741a06-58d7-43a9-b55a-80d07f281bba",
                        "firstName": "Wanda",
                        "nickName": null,
                        "lastName": "Maximoff",
                        "positions": [
                          "24dd5aef-e819-4d9d-8b19-d9d592c4ff8f"
                        ],
                        "roles": [
                          "ROLE_TEAM_MEMBER"
                        ],
                        "qualifications": [
                          {
                            "qualificationKey": "225f84e5-ebc1-4e76-8ada-98df9f8bf4ad",
                            "expiresAt": null,
                            "expires": false
                          }
                        ],
                        "email": "wanda.maximoff@test.email",
                        "verified": false
                      }
                    ]
                    """, JsonCompareMode.STRICT
            ));
    }
}
