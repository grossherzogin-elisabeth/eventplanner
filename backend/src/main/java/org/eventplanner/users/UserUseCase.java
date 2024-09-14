package org.eventplanner.users;

import io.micrometer.common.lang.Nullable;
import org.eventplanner.exceptions.UnauthorizedException;
import org.eventplanner.users.entities.EncryptedUserDetails;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.entities.User;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.service.UserService;
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

import static org.eventplanner.utils.ObjectUtils.applyNullable;

@Service
public class UserUseCase {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final UserService userService;
    private final Map<UserKey, EncryptedUserDetails> cache = new HashMap<>();

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

            var firstName = oidcUser.getAttributes().get("given_name");
            var lastName = oidcUser.getAttributes().get("family_name");
            if (firstName != null && lastName != null) {
                maybeUser = userService.getUserByName(firstName.toString(), lastName.toString());
                if (maybeUser.isPresent()) {
                    var user = maybeUser.get();
                    user.setAuthKey(authkey);
                    userService.updateUser(user);
                    return SignedInUser
                        .fromUser(user)
                        .withPermissionsFromAuthentication(authentication);
                }
            }

            return new SignedInUser(
                new UserKey("unknown"),
                authkey,
                Collections.emptyList(),
                Collections.emptyList(),
                oidcUser.getEmail()
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

    public UserDetails updateUser(@NonNull SignedInUser signedInUser, @NonNull UserKey key, @NonNull UpdateUserSpec spec) {
        signedInUser.assertHasPermission(Permission.WRITE_USERS);

        var user = userService.getUserByKey(key).orElseThrow();
        applyNullable(spec.gender(), user::setGender);
        applyNullable(spec.title(), user::setTitle);
        applyNullable(spec.firstName(), user::setFirstName);
        applyNullable(spec.secondName(), user::setSecondName);
        applyNullable(spec.lastName(), user::setLastName);
        applyNullable(spec.dateOfBirth(), user::setDateOfBirth);
        applyNullable(spec.placeOfBirth(), user::setPlaceOfBirth);
        applyNullable(spec.nationality(), user::setNationality);
        applyNullable(spec.passNr(), user::setPassNr);
        applyNullable(spec.email(), user::setEmail);
        applyNullable(spec.phone(), user::setPhone);
        applyNullable(spec.address(), user::setAddress);
        applyNullable(spec.mobile(), user::setMobile);
        applyNullable(spec.comment(), user::setComment);
        applyNullable(spec.qualifications(), user::setQualifications);
        applyNullable(spec.positions(), user::setPositions);
        applyNullable(spec.roles(), user::setRoles);

        return userService.updateUser(user);
    }
}
