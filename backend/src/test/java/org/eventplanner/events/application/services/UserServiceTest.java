package org.eventplanner.events.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.config.JsonMapperFactory.defaultJsonMapper;
import static org.eventplanner.testdata.UserFactory.createUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.ports.UserRepository;
import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.eventplanner.events.domain.values.users.AuthKey;
import org.eventplanner.testdata.PositionKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

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
        encryptionService = new EncryptionService(defaultJsonMapper(), "password");

        testee = new UserService(
            userRepository,
            qualificationRepository,
            encryptionService,
            null
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

    @Test
    void shouldUpdateUserEmailOnAuthenticate() {
        var authKey = new AuthKey("auth");
        var oldEmail = "old@email.com";
        var newEmail = "new@email.com";
        var user = createUser().withAuthKey(authKey).withEmail(oldEmail);
        var authentication = mock(AuthenticatedPrincipal.class);

        when(userRepository.findByAuthKey(authKey)).thenReturn(Optional.of(user.encrypt(encryptionService::encrypt)));
        when(userRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = testee.authenticate(authKey, newEmail, user.getFirstName(), user.getLastName(), authentication);

        assertThat(result.email()).isEqualTo(newEmail);
    }

    @Test
    void shouldLinkUserOnAuthenticateWhenFoundByEmailWithoutAuthKey() {
        var authKey = new AuthKey("auth");
        var email = "someones@email.com";
        var user = createUser().withAuthKey(null).withEmail(email);
        var authentication = mock(OidcUser.class);

        when(userRepository.findByAuthKey(authKey)).thenReturn(Optional.empty());
        when(userRepository.findAll()).thenReturn(List.of(user.encrypt(encryptionService::encrypt)));
        when(userRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result =
            testee.authenticate(authKey, email, user.getFirstName(), user.getLastName(), authentication);

        assertThat(result.authKey()).isEqualTo(authKey);
        verify(userRepository).update(argThat((encrypted) ->
            Objects.equals(user.getEmail(), encryptionService.decrypt(encrypted.getEmail()))
                && Objects.equals(authKey, encrypted.getAuthKey())));
    }

    @Test
    void shouldAddTemporaryAdminRoleForWhitelistedUsers() {
        var adminEmail = "admin@email.com";
        var authKey = new AuthKey("auth");
        var user = createUser().withAuthKey(authKey).withEmail(adminEmail).withRoles(List.of(Role.TEAM_MEMBER));
        var authentication = mock(AuthenticatedPrincipal.class);

        testee = new UserService(
            userRepository,
            qualificationRepository,
            encryptionService,
            adminEmail
        );

        when(userRepository.findByAuthKey(authKey)).thenReturn(Optional.of(user.encrypt(encryptionService::encrypt)));
        when(userRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = testee.authenticate(authKey, adminEmail, user.getFirstName(), user.getLastName(), authentication);

        assertThat(result.roles()).contains(Role.ADMIN);
    }

    @Test
    void shouldNotPersistTemporaryAdminRole() {
        var adminEmail = "admin@email.com";
        var authKey = new AuthKey("auth");
        var user = createUser().withAuthKey(authKey).withEmail(adminEmail).withRoles(List.of(Role.TEAM_MEMBER));
        var authentication = mock(AuthenticatedPrincipal.class);

        testee = new UserService(
            userRepository,
            qualificationRepository,
            encryptionService,
            adminEmail
        );

        when(userRepository.findByAuthKey(authKey)).thenReturn(Optional.of(user.encrypt(encryptionService::encrypt)));
        when(userRepository.update(any())).thenAnswer(invocation -> invocation.getArgument(0));

        testee.authenticate(authKey, adminEmail, user.getFirstName(), user.getLastName(), authentication);

        verify(userRepository, never()).update(argThat((encrypted) -> {
            var savedRoles = encrypted.getRoles().stream()
                .map((r) -> encryptionService.decrypt(r, Role.class))
                .toList();
            return savedRoles.contains(Role.ADMIN);
        }));
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
