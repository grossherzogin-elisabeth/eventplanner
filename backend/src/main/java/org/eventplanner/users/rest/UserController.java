package org.eventplanner.users.rest;

import java.net.URI;
import java.util.List;

import org.eventplanner.users.UserUseCase;
import org.eventplanner.users.rest.dto.CreateUserRequest;
import org.eventplanner.users.rest.dto.UpdateUserRequest;
import org.eventplanner.users.rest.dto.UserAdminListRepresentation;
import org.eventplanner.users.rest.dto.UserDetailsRepresentation;
import org.eventplanner.users.rest.dto.UserRepresentation;
import org.eventplanner.users.rest.dto.UserSelfRepresentation;
import org.eventplanner.users.values.Permission;
import org.eventplanner.users.values.UserKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@EnableMethodSecurity(securedEnabled = true)
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(@Autowired UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<List<?>> getUsers(
        @RequestParam(name = "details", required = false) boolean details
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        if (signedInUser.hasPermission(Permission.READ_USER_DETAILS)) {
            var users = userUseCase.getDetailedUsers(signedInUser).stream();
            if (details) {
                return ResponseEntity.ok(users.map(UserDetailsRepresentation::fromDomain).toList());
            }
            return ResponseEntity.ok(users.map(UserAdminListRepresentation::fromDomain).toList());
        } else {
            var users = userUseCase.getUsers(signedInUser).stream();
            return ResponseEntity.ok(users.map(UserRepresentation::fromDomain).toList());
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "")
    public ResponseEntity<UserDetailsRepresentation> createUser(
        @RequestBody CreateUserRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var user = userUseCase.createUser(signedInUser, spec.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED)
            .location(URI.create("/api/v1/users/" + user.getKey().value()))
            .body(UserDetailsRepresentation.fromDomain(user));
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{key}")
    public ResponseEntity<UserDetailsRepresentation> getUserByKey(
        @PathVariable("key") String userKey
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        return userUseCase.getUserByKey(signedInUser, new UserKey(userKey))
            .map(UserDetailsRepresentation::fromDomain)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/self")
    public ResponseEntity<UserSelfRepresentation> getSignedInUser() {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        return userUseCase.getUserByKey(signedInUser, signedInUser.key())
            .map(UserSelfRepresentation::fromDomain)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @RequestMapping(method = RequestMethod.PATCH, path = "/{key}")
    public ResponseEntity<UserDetailsRepresentation> updateUser(
        @PathVariable("key") String userKey,
        @RequestBody UpdateUserRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var user = userUseCase.updateUser(signedInUser, new UserKey(userKey), spec.toDomain());
        return ResponseEntity.ok(UserDetailsRepresentation.fromDomain(user));
    }

    @RequestMapping(method = RequestMethod.PATCH, path = "/self")
    public ResponseEntity<UserDetailsRepresentation> updateSignedInUser(
        @RequestBody UpdateUserRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var user = userUseCase.updateUser(signedInUser, signedInUser.key(), spec.toDomain());
        return ResponseEntity.ok(UserDetailsRepresentation.fromDomain(user));
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{key}")
    public ResponseEntity<Void> updateUser(
        @PathVariable("key") String userKey
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        userUseCase.deleteUser(signedInUser, new UserKey(userKey));
        return ResponseEntity.ok().build();
    }
}
