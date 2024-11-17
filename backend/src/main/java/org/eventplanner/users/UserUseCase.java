package org.eventplanner.users;

import io.micrometer.common.lang.Nullable;
import org.eventplanner.exceptions.UnauthorizedException;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.entities.User;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.service.UserService;
import org.eventplanner.users.spec.CreateUserSpec;
import org.eventplanner.users.spec.UpdateUserSpec;
import org.eventplanner.users.values.AuthKey;
import org.eventplanner.users.values.Permission;
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

import java.util.*;

import static org.eventplanner.common.ObjectUtils.applyNullable;

@Service
public class UserUseCase {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final UserService userService;

    public UserUseCase(@Autowired UserService userService) {
        this.userService = userService;
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
                    user.setAuthKey(authkey);
                    userService.updateUser(user);
                    return SignedInUser
                        .fromUser(user)
                        .withPermissionsFromAuthentication(authentication);
                }

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
        signedInUser.assertHasPermission(Permission.READ_FULL_USER_DETAILS);
        return userService.getDetailedUsers();
    }

    public Optional<UserDetails> getUserByKey(@NonNull SignedInUser signedInUser, @NonNull UserKey key) {
        if (!signedInUser.key().equals(key)) {
            signedInUser.assertHasPermission(Permission.READ_FULL_USER_DETAILS);
        }
        return userService.getUserByKey(key);
    }

    public UserDetails createUser(@NonNull SignedInUser signedInUser, @NonNull CreateUserSpec spec) {
        signedInUser.assertHasPermission(Permission.WRITE_USERS);
        var newUser = new UserDetails(new UserKey(), spec.firstName(), spec.lastName());
        newUser.setEmail(spec.email());
        return userService.createUser(newUser);
    }

    public UserDetails updateUser(@NonNull SignedInUser signedInUser, @NonNull UserKey key, @NonNull UpdateUserSpec spec) {
        signedInUser.assertHasPermission(Permission.WRITE_USERS);

        var user = userService.getUserByKey(key).orElseThrow();
        applyNullable(spec.authKey(), user::setAuthKey);
        applyNullable(spec.gender(), it -> user.setGender(it.trim()));
        applyNullable(spec.title(), it -> user.setTitle(it.trim()));
        applyNullable(spec.firstName(), it -> user.setFirstName(it.trim()));
        applyNullable(spec.nickName(), it -> user.setNickName(it.trim()));
        applyNullable(spec.secondName(), it -> user.setSecondName(it.trim()));
        applyNullable(spec.lastName(), it -> user.setLastName(it.trim()));
        applyNullable(spec.dateOfBirth(), user::setDateOfBirth);
        applyNullable(spec.placeOfBirth(), it -> user.setPlaceOfBirth(it.trim()));
        applyNullable(spec.nationality(), it -> user.setNationality(it.trim()));
        applyNullable(spec.passNr(), it -> user.setPassNr(it.trim()));
        applyNullable(spec.email(), it -> user.setEmail(it.trim()));
        applyNullable(spec.phone(), it -> user.setPhone(it.trim()));
        applyNullable(spec.phoneWork(), it -> user.setPhoneWork(it.trim()));
        applyNullable(spec.address(), user::setAddress);
        applyNullable(spec.mobile(), it -> user.setMobile(it.trim()));
        applyNullable(spec.comment(), it -> user.setComment(it.trim()));
        applyNullable(spec.qualifications(), user::setQualifications);
        applyNullable(spec.roles(), user::setRoles);
        applyNullable(spec.emergencyContact(), user::setEmergencyContact);
        applyNullable(spec.diseases(), it -> user.setDiseases(it.trim()));
        applyNullable(spec.intolerances(), it -> user.setIntolerances(it.trim()));
        applyNullable(spec.medication(), it -> user.setMedication(it.trim()));
        applyNullable(spec.diet(), user::setDiet);

        return userService.updateUser(user);
    }

    public void deleteUser(@NonNull SignedInUser signedInUser, @NonNull UserKey userKey) {
        signedInUser.assertHasPermission(Permission.DELETE_USERS);

        userService.deleteUser(userKey);
    }
}
