package org.eventplanner.events.rest.users;

import java.net.URI;
import java.util.List;

import org.eventplanner.events.application.usecases.UserUseCase;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.users.UserKey;
import org.eventplanner.events.rest.users.dto.CreateUserRequest;
import org.eventplanner.events.rest.users.dto.UpdateUserRequest;
import org.eventplanner.events.rest.users.dto.UserAdminListRepresentation;
import org.eventplanner.events.rest.users.dto.UserDetailsRepresentation;
import org.eventplanner.events.rest.users.dto.UserRepresentation;
import org.eventplanner.events.rest.users.dto.UserSelfRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;

    @GetMapping("")
    public ResponseEntity<List<?>> getUsers(
        @RequestParam(name = "details", required = false) Boolean details
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        if (signedInUser.hasPermission(Permission.READ_USER_DETAILS)) {
            var users = userUseCase.getDetailedUsers(signedInUser).stream();
            if (Boolean.TRUE.equals(details)) {
                return ResponseEntity.ok(users.map(UserDetailsRepresentation::fromDomain).toList());
            }
            return ResponseEntity.ok(users.map(UserAdminListRepresentation::fromDomain).toList());
        } else {
            var users = userUseCase.getUsers(signedInUser).stream();
            return ResponseEntity.ok(users.map(UserRepresentation::fromDomain).toList());
        }
    }

    @PostMapping("")
    public ResponseEntity<UserDetailsRepresentation> createUser(
        @Valid @RequestBody CreateUserRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var user = userUseCase.createUser(signedInUser, spec.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED)
            .location(URI.create("/api/v1/users/" + user.getKey().value()))
            .body(UserDetailsRepresentation.fromDomain(user));
    }

    @GetMapping("/{key}")
    public ResponseEntity<UserDetailsRepresentation> getUserByKey(
        @PathVariable("key") String userKey
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        return userUseCase.getUserByKey(signedInUser, new UserKey(userKey))
            .map(UserDetailsRepresentation::fromDomain)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/self")
    public ResponseEntity<UserSelfRepresentation> getSignedInUser() {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        return userUseCase.getUserByKey(signedInUser, signedInUser.key())
            .map(UserSelfRepresentation::fromDomain)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PatchMapping("/{key}")
    public ResponseEntity<UserDetailsRepresentation> updateUser(
        @PathVariable("key") String userKey,
        @RequestBody UpdateUserRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var user = userUseCase.updateUser(signedInUser, new UserKey(userKey), spec.toDomain());
        return ResponseEntity.ok(UserDetailsRepresentation.fromDomain(user));
    }

    @PatchMapping("/self")
    public ResponseEntity<UserDetailsRepresentation> updateSignedInUser(
        @RequestBody UpdateUserRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var user = userUseCase.updateUser(signedInUser, signedInUser.key(), spec.toDomain());
        return ResponseEntity.ok(UserDetailsRepresentation.fromDomain(user));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> updateUser(
        @PathVariable("key") String userKey
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        userUseCase.deleteUser(signedInUser, new UserKey(userKey));
        return ResponseEntity.ok().build();
    }
}
