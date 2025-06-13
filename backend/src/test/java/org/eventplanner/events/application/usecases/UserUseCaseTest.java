package org.eventplanner.events.application.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.Qualification;
import org.eventplanner.events.domain.entities.SignedInUser;
import org.eventplanner.events.domain.entities.UserDetails;
import org.eventplanner.events.domain.specs.UpdateUserSpec;
import org.eventplanner.events.domain.values.Permission;
import org.eventplanner.testdata.QualificationFactory;
import org.eventplanner.testdata.SignedInUserFactory;
import org.eventplanner.testdata.UserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserUseCaseTest {

    private static final Qualification NON_EXPIRING_QUALIFICATION =
        QualificationFactory.createQualification().withExpires(false);
    private static final Qualification EXPIRING_QUALIFICATION = QualificationFactory.createQualification();
    private static final Qualification SOON_EXPIRING_QUALIFICATION = QualificationFactory.createQualification();
    private static final Qualification EXPIRED_QUALIFICATION = QualificationFactory.createQualification();
    private static final Qualification NON_EXPIRING_NEW_QUALIFICATION =
        QualificationFactory.createQualification().withExpires(false);
    private static final Qualification EXPIRING_NEW_QUALIFICATION = QualificationFactory.createQualification();

    private SignedInUser signedInUser;
    private UserDetails user;
    private List<Qualification> qualifications;

    private UserUseCase testee;
    private NotificationService notificationService;
    private UserService userService;
    private QualificationRepository qualificationRepository;

    @BeforeEach
    void setup() {
        notificationService = mock();

        qualificationRepository = mock();
        qualifications = List.of(
            NON_EXPIRING_QUALIFICATION,
            EXPIRING_QUALIFICATION,
            SOON_EXPIRING_QUALIFICATION,
            EXPIRED_QUALIFICATION,
            NON_EXPIRING_NEW_QUALIFICATION,
            EXPIRING_NEW_QUALIFICATION
        );
        when(qualificationRepository.findAll()).thenReturn(qualifications);

        user = UserFactory.createUser();
        user.addQualification(NON_EXPIRING_QUALIFICATION);
        user.addQualification(EXPIRING_QUALIFICATION, Instant.now().plusSeconds(10000000));
        user.addQualification(SOON_EXPIRING_QUALIFICATION, Instant.now().plusSeconds(100));
        user.addQualification(EXPIRED_QUALIFICATION, Instant.now().minusSeconds(100));
        userService = mock();
        when(userService.getDetailedUsers()).thenReturn(List.of(user));
        when(userService.getUserByKey(user.getKey())).thenReturn(Optional.of(user));
        when(userService.getUsers()).thenReturn(List.of(user.cropToUser()));
        when(userService.updateUser(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        testee = new UserUseCase(
            userService,
            notificationService,
            qualificationRepository
        );
    }

    @Test
    void shouldNotSendAnyNotification() {
        signedInUser = SignedInUserFactory.createSignedInUser().withPermissions(Permission.WRITE_USERS);
        var spec = createUpdateQualificationSpecs();
        var updateUser = testee.updateUser(
            signedInUser, user.getKey(), UpdateUserSpec
                .builder()
                .qualifications(spec)
                .build()
        );

        assertThat(updateUser.getQualifications()).hasSize(4);
        assertThat(updateUser.getQualification(NON_EXPIRING_QUALIFICATION.getKey())).isPresent();
        assertThat(updateUser.getQualification(EXPIRING_QUALIFICATION.getKey())).isPresent();
        assertThat(updateUser.getQualification(SOON_EXPIRING_QUALIFICATION.getKey())).isPresent();
        assertThat(updateUser.getQualification(EXPIRED_QUALIFICATION.getKey())).isPresent();
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldAddQualifications() {
        signedInUser = SignedInUserFactory.createSignedInUser().withPermissions(Permission.WRITE_USERS);
        var spec = createUpdateQualificationSpecs();
        spec.add(new UpdateUserSpec.UpdateUserQualificationSpec(
            NON_EXPIRING_NEW_QUALIFICATION.getKey(),
            null
        ));
        spec.add(new UpdateUserSpec.UpdateUserQualificationSpec(
            EXPIRING_NEW_QUALIFICATION.getKey(),
            Instant.now().plusSeconds(100000000)
        ));
        var updatedUser = testee.updateUser(
            signedInUser, user.getKey(), UpdateUserSpec
                .builder()
                .qualifications(spec)
                .build()
        );

        assertThat(updatedUser.getQualifications()).hasSize(6);
        assertThat(updatedUser.getQualification(NON_EXPIRING_QUALIFICATION.getKey())).isPresent();
        assertThat(updatedUser.getQualification(EXPIRING_QUALIFICATION.getKey())).isPresent();
        assertThat(updatedUser.getQualification(SOON_EXPIRING_QUALIFICATION.getKey())).isPresent();
        assertThat(updatedUser.getQualification(EXPIRED_QUALIFICATION.getKey())).isPresent();
        assertThat(updatedUser.getQualification(NON_EXPIRING_NEW_QUALIFICATION.getKey())).isPresent();
        assertThat(updatedUser.getQualification(EXPIRING_NEW_QUALIFICATION.getKey())).isPresent();
        verify(notificationService).sendQualificationAddedNotification(user, NON_EXPIRING_NEW_QUALIFICATION);
        verify(notificationService).sendQualificationAddedNotification(user, EXPIRING_NEW_QUALIFICATION);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldRemoveQualification() {
        signedInUser = SignedInUserFactory.createSignedInUser().withPermissions(Permission.WRITE_USERS);
        var spec = createUpdateQualificationSpecs();
        spec.removeIf(it -> it.qualificationKey().equals(NON_EXPIRING_QUALIFICATION.getKey()));
        var updatedUser = testee.updateUser(
            signedInUser, user.getKey(), UpdateUserSpec
                .builder()
                .qualifications(spec)
                .build()
        );
        assertThat(updatedUser.getQualifications()).hasSize(3);
        assertThat(updatedUser.getQualification(NON_EXPIRING_QUALIFICATION.getKey())).isEmpty();
        assertThat(updatedUser.getQualification(EXPIRING_QUALIFICATION.getKey())).isPresent();
        assertThat(updatedUser.getQualification(SOON_EXPIRING_QUALIFICATION.getKey())).isPresent();
        assertThat(updatedUser.getQualification(EXPIRED_QUALIFICATION.getKey())).isPresent();
        verify(notificationService).sendQualificationRemovedNotification(user, NON_EXPIRING_QUALIFICATION);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void shouldUpdateQualification() {
        signedInUser = SignedInUserFactory.createSignedInUser().withPermissions(Permission.WRITE_USERS);
        var spec = createUpdateQualificationSpecs();
        var updateIndex = -1;
        for (int i = 0; i < spec.size(); i++) {
            if (spec.get(i).qualificationKey().equals(EXPIRED_QUALIFICATION.getKey())) {
                updateIndex = i;
                break;
            }
        }

        spec.set(
            updateIndex, new UpdateUserSpec.UpdateUserQualificationSpec(
                spec.get(updateIndex).qualificationKey(),
                Instant.now().plusSeconds(100000000)
            )
        );
        var updatedUser = testee.updateUser(
            signedInUser, user.getKey(), UpdateUserSpec
                .builder()
                .qualifications(spec)
                .build()
        );
        assertThat(updatedUser.getQualifications()).hasSize(4);
        assertThat(updatedUser.getQualification(NON_EXPIRING_QUALIFICATION.getKey())).isPresent();
        assertThat(updatedUser.getQualification(EXPIRING_QUALIFICATION.getKey())).isPresent();
        assertThat(updatedUser.getQualification(SOON_EXPIRING_QUALIFICATION.getKey())).isPresent();
        assertThat(updatedUser.getQualification(EXPIRED_QUALIFICATION.getKey())).isPresent();
        verify(notificationService).sendQualificationUpdatedNotification(user, EXPIRED_QUALIFICATION);
        verifyNoMoreInteractions(notificationService);
    }

    private List<UpdateUserSpec.UpdateUserQualificationSpec> createUpdateQualificationSpecs() {
        var immutable = user.getQualifications().stream()
            .map(it -> new UpdateUserSpec.UpdateUserQualificationSpec(
                it.getQualificationKey(),
                it.getExpiresAt()
            ))
            .toList();
        return new LinkedList<>(immutable); // make the list mutable
    }
}
