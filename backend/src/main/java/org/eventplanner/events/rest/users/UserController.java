package org.eventplanner.events.rest.users;

import java.net.URI;
import java.util.List;

import org.eventplanner.events.application.usecases.AuthenticationUseCase;
import org.eventplanner.events.application.usecases.users.CreateUserUseCase;
import org.eventplanner.events.application.usecases.users.DeleteUserUseCase;
import org.eventplanner.events.application.usecases.users.ReadUserUseCase;
import org.eventplanner.events.application.usecases.users.UpdateUserUseCase;
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

    private final AuthenticationUseCase authenticationUseCase;
    private final ReadUserUseCase readUserUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    @GetMapping("")
    public ResponseEntity<List<?>> getUsers(
        @RequestParam(name = "details", required = false) Boolean details
    ) {
        var signedInUser = authenticationUseCase.getSignedInUser();
        if (signedInUser.hasPermission(Permission.READ_USER_DETAILS)) {
            var users = readUserUseCase.getDetailedUsers().stream();
            if (Boolean.TRUE.equals(details)) {
                return ResponseEntity.ok(users.map(UserDetailsRepresentation::fromDomain).toList());
            }
            return ResponseEntity.ok(users.map(UserAdminListRepresentation::fromDomain).toList());
        } else {
            var users = readUserUseCase.getUsers().stream();
            return ResponseEntity.ok(users.map(UserRepresentation::fromDomain).toList());
        }
    }

    @PostMapping("")
    public ResponseEntity<UserDetailsRepresentation> createUser(
        @Valid @RequestBody CreateUserRequest spec
    ) {
        var user = createUserUseCase.createUser(spec.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED)
            .location(URI.create("/api/v1/users/" + user.getKey().value()))
            .body(UserDetailsRepresentation.fromDomain(user));
    }

    @GetMapping("/{key}")
    public ResponseEntity<UserDetailsRepresentation> getUserByKey(
        @PathVariable("key") String userKey
    ) {
        return readUserUseCase.getUserByKey(new UserKey(userKey))
            .map(UserDetailsRepresentation::fromDomain)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/self")
    public ResponseEntity<UserSelfRepresentation> getSignedInUser() {
        var signedInUser =
            authenticationUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        return readUserUseCase.getUserByKey(signedInUser.key())
            .map(UserSelfRepresentation::fromDomain)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PatchMapping("/{key}")
    public ResponseEntity<UserDetailsRepresentation> updateUser(
        @PathVariable("key") String userKey,
        @Valid @RequestBody UpdateUserRequest spec
    ) {
        var user = updateUserUseCase.updateUser(new UserKey(userKey), spec.toDomain());
        return ResponseEntity.ok(UserDetailsRepresentation.fromDomain(user));
    }

    @PatchMapping("/self")
    public ResponseEntity<UserDetailsRepresentation> updateSignedInUser(
        @Valid @RequestBody UpdateUserRequest spec
    ) {
        var user = updateUserUseCase.updateUserSelf(spec.toDomain());
        return ResponseEntity.ok(UserDetailsRepresentation.fromDomain(user));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteUser(@PathVariable String key) {
        deleteUserUseCase.deleteUser(new UserKey(key));
        return ResponseEntity.ok().build();
    }
}
