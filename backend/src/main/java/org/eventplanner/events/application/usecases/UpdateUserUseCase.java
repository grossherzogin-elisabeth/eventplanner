package org.eventplanner.events.application.usecases;

import static java.util.Optional.ofNullable;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.services.AuthenticationService;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.specs.UpdateUserSpec;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.users.UserKey;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final NotificationService notificationService;
    private final QualificationRepository qualificationRepository;
    private final PositionRepository positionRepository;

    @Transactional
    @PreAuthorize("hasAuthority('users:write') or hasAuthority('users:write-self')")
    public @NonNull UserDetails updateUserSelf(@NonNull final UpdateUserSpec spec) {
        var signedInUser = authenticationService.getSignedInUser();
        log.info("User {} is updating their personal information", signedInUser.key());
        var user = updateUserInternal(signedInUser.key(), spec);
        notificationService.sendUserChangedPersonalDataNotification(Role.USER_MANAGER, user);
        return user;
    }

    @Transactional
    @PreAuthorize("hasAuthority('users:write')")
    public @NonNull UserDetails updateUser(
        @NonNull final UserKey userKey,
        @NonNull final UpdateUserSpec spec
    ) {
        log.info("Updating user {}", userKey);
        return updateUserInternal(userKey, spec);
    }

    private @NonNull UserDetails updateUserInternal(
        @NonNull final UserKey userKey,
        @NonNull final UpdateUserSpec spec
    ) {
        var signedInUser = authenticationService.getSignedInUser();
        var user = userService.getUserByKey(userKey).orElseThrow();
        var changedFields = spec.changes();
        log.info("Updating attributes {}", String.join(", ", changedFields));

        // these may be changed by a user themselves
        ofNullable(spec.gender()).map(String::trim).ifPresent(user::setGender);
        ofNullable(spec.title()).map(String::trim).ifPresent(user::setTitle);
        ofNullable(spec.nickName()).map(String::trim).ifPresent(user::setNickName);
        ofNullable(spec.nationality()).map(String::trim).ifPresent(user::setNationality);
        ofNullable(spec.passNr()).map(String::trim).ifPresent(user::setPassNr);
        ofNullable(spec.phone()).map(String::trim).ifPresent(user::setPhone);
        ofNullable(spec.phoneWork()).map(String::trim).ifPresent(user::setPhoneWork);
        ofNullable(spec.address()).ifPresent(user::setAddress);
        ofNullable(spec.mobile()).map(String::trim).ifPresent(user::setMobile);
        ofNullable(spec.emergencyContact()).ifPresent(user::setEmergencyContact);
        ofNullable(spec.diseases()).map(String::trim).ifPresent(user::setDiseases);
        ofNullable(spec.intolerances()).map(String::trim).ifPresent(user::setIntolerances);
        ofNullable(spec.medication()).map(String::trim).ifPresent(user::setMedication);
        ofNullable(spec.diet()).ifPresent(user::setDiet);

        // these may only be changed by admins
        var hasWritePermission = signedInUser.hasPermission(Permission.WRITE_USERS);
        if (hasWritePermission) {
            ofNullable(spec.authKey()).ifPresent(user::setAuthKey);
            ofNullable(spec.firstName()).map(String::trim).ifPresent(user::setFirstName);
            ofNullable(spec.secondName()).map(String::trim).ifPresent(user::setSecondName);
            ofNullable(spec.lastName()).map(String::trim).ifPresent(user::setLastName);
            ofNullable(spec.email()).map(String::trim).ifPresent(user::setEmail);
            ofNullable(spec.comment()).map(String::trim).ifPresent(user::setComment);
            ofNullable(spec.qualifications()).ifPresent(specs -> updateUserQualifications(user, specs));
            ofNullable(spec.roles()).ifPresent(user::setRoles);
            ofNullable(spec.verifiedAt()).ifPresent(user::setVerifiedAt);
        } else if (changedFields.contains("authKey")
            || changedFields.contains("firstName")
            || changedFields.contains("secondName")
            || changedFields.contains("lastName")
            || changedFields.contains("email")
            || changedFields.contains("comment")
            || changedFields.contains("qualifications")
            || changedFields.contains("roles")
            || changedFields.contains("verifiedAt")) {
            log.warn("Blocked updating some user attributes because of insufficient permissions");
        }
        // these may be changed by a user themselves, if there is no value yet
        if (hasWritePermission || user.getDateOfBirth() == null) {
            ofNullable(spec.dateOfBirth()).ifPresent(user::setDateOfBirth);
        }
        if (hasWritePermission || user.getPlaceOfBirth() == null) {
            ofNullable(spec.placeOfBirth()).map(String::trim).ifPresent(user::setPlaceOfBirth);
        }

        user.setUpdatedAt(Instant.now());
        return userService.updateUser(user);
    }

    /**
     * Adds, updates and removes qualifications on the user and sends corresponding notifications
     *
     * @param user  the user to update
     * @param specs updated qualifications
     */
    private void updateUserQualifications(
        @NonNull final UserDetails user,
        @NonNull final List<UpdateUserSpec.UpdateUserQualificationSpec> specs
    ) {
        var qualifications = qualificationRepository.findAllAsMap();
        var positions = positionRepository.findAllAsMap();

        var addedQualifications = new LinkedList<Qualification>();
        var updatedQualifications = new LinkedList<Qualification>();
        var removedQualifications = new LinkedList<Qualification>();

        for (final var spec : specs) {
            var qualification = qualifications.get(spec.qualificationKey());
            if (qualification == null) {
                log.error("Cannot assign unknown qualification {} to user {}", spec.qualificationKey(), user.getKey());
                continue;
            }
            var existingUserQualification = user.findQualification(qualification.getKey());
            if (existingUserQualification.isPresent()) {
                var oldExpiration = existingUserQualification.get().getExpiresAt();
                var newExpiration = spec.expiresAt();
                if (!Objects.equals(oldExpiration, newExpiration)) {
                    user.updateQualification(qualification, spec.expiresAt());
                    updatedQualifications.add(qualification);
                }
            } else {
                user.addQualification(qualification, spec.expiresAt());
                addedQualifications.add(qualification);
            }
        }

        var keys = specs.stream().map(UpdateUserSpec.UpdateUserQualificationSpec::qualificationKey).toList();
        for (final var userQualification : user.getQualifications()) {
            if (!keys.contains(userQualification.getQualificationKey())) {
                user.removeQualification(userQualification.getQualificationKey());
                var qualification = qualifications.get(userQualification.getQualificationKey());
                if (qualification != null) {
                    removedQualifications.add(qualification);
                }
            }
        }

        addedQualifications.forEach(q -> notificationService.sendQualificationAddedNotification(user, q, positions));
        removedQualifications.forEach(q -> notificationService.sendQualificationRemovedNotification(
            user,
            q,
            positions
        ));
        updatedQualifications.forEach(q -> notificationService.sendQualificationUpdatedNotification(
            user,
            q,
            positions
        ));
    }
}
