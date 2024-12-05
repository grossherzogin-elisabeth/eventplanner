package org.eventplanner.users;

import static java.util.Optional.ofNullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eventplanner.exceptions.UnauthorizedException;
import org.eventplanner.notifications.service.NotificationService;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.entities.User;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.service.UserService;
import org.eventplanner.users.spec.CreateUserSpec;
import org.eventplanner.users.spec.UpdateUserSpec;
import org.eventplanner.users.values.AuthKey;
import org.eventplanner.users.values.Permission;
import org.eventplanner.users.values.Role;
import org.eventplanner.users.values.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import io.micrometer.common.lang.Nullable;

@Service
public class UserUseCase {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final UserService userService;
    private final NotificationService notificationService;

    public UserUseCase(@Autowired UserService userService, final NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    public @NonNull SignedInUser getSignedInUser(@Nullable Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException();
        }
        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            var authkey = new AuthKey(oidcUser.getSubject());
            var maybeUser = userService.getUserByAuthKey(authkey);
            if (maybeUser.isPresent()) {
                return SignedInUser
                    .fromUser(maybeUser.get())
                    .withPermissionsFromAuthentication(authentication);
            }
            maybeUser = userService.getUserByEmail(oidcUser.getEmail());
            if (maybeUser.isPresent()) {
                var user = maybeUser.get();
                user.setAuthKey(authkey);
                log.info("Linking user {} with oidc user {} by email", user.getKey(), authkey);
                userService.updateUser(user);
                return SignedInUser
                    .fromUser(user)
                    .withPermissionsFromAuthentication(authentication);
            }

            var firstName = oidcUser.getAttributes().get("given_name").toString();
            var lastName = oidcUser.getAttributes().get("family_name").toString();
            if (firstName != null && lastName != null) {
                maybeUser = userService.getUserByName(firstName, lastName);
                if (maybeUser.isPresent()) {
                    var user = maybeUser.get();
                    log.info("Linking user {} with oidc user {} by name", user.getKey(), authkey);
                    user.setAuthKey(authkey);
                    userService.updateUser(user);
                    return SignedInUser
                        .fromUser(user)
                        .withPermissionsFromAuthentication(authentication);
                }

                log.warn("Cannot find match for oidc user {}. Creating new user.", authkey);
                var newUser = new UserDetails(UserKey.fromName(firstName + " " + lastName), firstName, lastName);
                newUser.setEmail(oidcUser.getEmail());
                newUser.setAuthKey(authkey);
                newUser = userService.createUser(newUser);
                return SignedInUser
                    .fromUser(newUser)
                    .withPermissionsFromAuthentication(authentication);
            }

            // this should not happen
            return new SignedInUser(
                new UserKey("unknown"),
                authkey,
                Collections.emptyList(),
                Collections.emptyList(),
                oidcUser.getEmail(),
                Collections.emptyList()
            ).withPermissionsFromAuthentication(authentication);
        }
        if (authentication.getPrincipal() instanceof OAuth2User) {
            log.error("Provided authentication is an OAuth2User, which is not implemented!");
            throw new UnauthorizedException();
        }
        log.error("Authentication is of unknown type: {}", authentication.getClass().getName());
        throw new UnauthorizedException();
    }

    public @NonNull List<User> getUsers(@NonNull SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_USERS);
        return userService.getUsers();
    }

    public @NonNull List<UserDetails> getDetailedUsers(@NonNull SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_USER_DETAILS);
        return userService.getDetailedUsers();
    }

    public Optional<UserDetails> getUserByKey(@NonNull SignedInUser signedInUser, @NonNull UserKey key) {
        if (!signedInUser.key().equals(key)) {
            signedInUser.assertHasPermission(Permission.READ_USER_DETAILS);
        }
        return userService.getUserByKey(key);
    }

    public UserDetails createUser(@NonNull SignedInUser signedInUser, @NonNull CreateUserSpec spec) {
        signedInUser.assertHasPermission(Permission.WRITE_USERS);
        var newUser = new UserDetails(new UserKey(), spec.firstName(), spec.lastName());
        log.info("Creating user {}", newUser.getKey());
        newUser.setEmail(spec.email());
        return userService.createUser(newUser);
    }

    public UserDetails updateUser(
        @NonNull SignedInUser signedInUser,
        @NonNull UserKey userKey,
        @NonNull UpdateUserSpec spec
    ) {
        if (signedInUser.key().equals(userKey)) {
            signedInUser.assertHasPermission(Permission.WRITE_OWN_USER_DETAILS);
            log.info("User {} is updating their personal information", userKey);
        } else {
            signedInUser.assertHasPermission(Permission.WRITE_USERS);
            log.info("Updating user {}", userKey);
        }

        var user = userService.getUserByKey(userKey).orElseThrow();

        if (signedInUser.key().equals(userKey)) {
            userService.getUsersByRole(Role.USER_MANAGER)
                .forEach(userManager -> notificationService.sendUserChangedPersonalDataNotification(userManager, user));
        }

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
            ofNullable(spec.qualifications()).ifPresent(user::setQualifications);
            ofNullable(spec.roles()).ifPresent(user::setRoles);
        }
        // these may be changed by a user themselves, if there is no value yet
        if (hasWritePermission || user.getDateOfBirth() == null) {
            ofNullable(spec.dateOfBirth()).ifPresent(user::setDateOfBirth);
        }
        if (hasWritePermission || user.getPlaceOfBirth() == null) {
            ofNullable(spec.placeOfBirth()).map(String::trim).ifPresent(user::setPlaceOfBirth);
        }

        return userService.updateUser(user);

    }

    public void deleteUser(@NonNull SignedInUser signedInUser, @NonNull UserKey userKey) {
        signedInUser.assertHasPermission(Permission.DELETE_USERS);

        log.info("Deleting user {}", userKey);
        userService.deleteUser(userKey);
    }
}
