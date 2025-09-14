package org.eventplanner.events.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.config.ObjectMapperFactory.defaultObjectMapper;
import static org.eventplanner.testdata.UserFactory.createUser;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.testdata.PositionKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private QualificationRepository qualificationRepository;
    private UserRepository userRepository;
    private EncryptionService encryptionService;
    private UserService testee;

    @BeforeEach
    void setup() {
        qualificationRepository = mock();
        when(qualificationRepository.findAll()).thenReturn(List.of(
            qualificationWithoutExpiration(),
            qualificationWithExpiration(),
            qualificationWithPosition()
        ));
        userRepository = mock();
        encryptionService = new EncryptionService(defaultObjectMapper(), "password");

        testee = new UserService(
            userRepository,
            qualificationRepository,
            encryptionService
        );
    }

    @Test
    void shouldReturnSortedListOfUsers() {
        var userA = createUser().withFirstName("User A");
        var userB = createUser().withFirstName("User B").withNickName("A");
        var userC = createUser().withFirstName("User C").withNickName("B");
        var mockFindAll = Stream.of(userA, userB, userC)
            .map(u -> u.encrypt(encryptionService::encrypt))
            .toList();
        when(userRepository.findAll()).thenReturn(mockFindAll);

        var result = testee.getUsers();

        assertThat(result).containsExactly(userB.cropToUser(), userC.cropToUser(), userA.cropToUser());
    }

    @Test
    void shouldReturnListOfDetailedUsers() {
        var userA = createUser().withNickName("User A");
        var userB = createUser().withNickName("User B");
        var userC = createUser().withNickName("User C");
        var userD = createUser().withNickName("User D");
        userA.addQualification(qualificationWithExpiration());
        userB.addQualification(qualificationWithPosition());
        userC.addQualification(qualificationWithExpiration());
        userD.addQualification(qualificationWithExpiration(), Instant.now().plusSeconds(1000));
        var mockFindAll = Stream.of(userC, userB, userA, userD)
            .map(u -> u.encrypt(encryptionService::encrypt))
            .toList();
        when(userRepository.findAll()).thenReturn(mockFindAll);

        var result = testee.getDetailedUsers();

        assertThat(result).containsExactly(userA, userB, userC, userD);
    }

    @Test
    void shouldReturnUserDetails() {
        var user = createUser();
        user.addQualification(qualificationWithPosition());
        when(userRepository.findByKey(user.getKey()))
            .thenReturn(Optional.of(user.encrypt(encryptionService::encrypt)));

        var result = testee.getUserByKey(user.getKey());

        assertThat(result).isPresent().contains(user);
    }

    @Test
    void shouldFindUserByEmail() {
        var userA = createUser().withNickName("User A");
        var userB = createUser().withNickName("User B");
        var userC = createUser().withNickName("User C").withEmail("someone@email.com");
        var userD = createUser().withNickName("User D");
        userA.addQualification(qualificationWithExpiration());
        userB.addQualification(qualificationWithPosition());
        userC.addQualification(qualificationWithExpiration());
        userD.addQualification(qualificationWithExpiration(), Instant.now().plusSeconds(1000));
        var mockFindAll = Stream.of(userC, userB, userA, userD)
            .map(u -> u.encrypt(encryptionService::encrypt))
            .toList();
        when(userRepository.findAll()).thenReturn(mockFindAll);

        var result = testee.getUserByEmail(userC.getEmail());

        assertThat(result).isPresent().contains(userC);
    }

    @Test
    void shouldFindUserByAuthKey() {
        var authKey = new AuthKey("auth");
        var user = createUser().withAuthKey(authKey);
        user.addQualification(qualificationWithPosition());
        when(userRepository.findByAuthKey(authKey))
            .thenReturn(Optional.of(user.encrypt(encryptionService::encrypt)));

        var result = testee.getUserByAuthKey(authKey);

        assertThat(result).isPresent().contains(user);
    }

    private static Qualification qualificationWithoutExpiration() {
        return new Qualification(
            new QualificationKey("non-expiring-qualification"),
            "non-expiring qualification",
            null,
            null,
            false,
            Collections.emptyList()
        );
    }

    private static Qualification qualificationWithExpiration() {
        return new Qualification(
            new QualificationKey("expiring-qualification"),
            "expiring qualification",
            null,
            null,
            true,
            Collections.emptyList()
        );
    }

    private static Qualification qualificationWithPosition() {
        return new Qualification(
            new QualificationKey("qualification-with-position"),
            "qualification with position",
            null,
            null,
            false,
            List.of(PositionKeys.DECKHAND)
        );
    }
}
