package org.eventplanner.users.rest;

import org.eventplanner.users.UserUseCase;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@EnableMethodSecurity(securedEnabled = true)
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(@Autowired UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<List<?>> getUsers(@RequestParam(name = "details", required = false) boolean details) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        if (details && signedInUser.hasPermission(Permission.READ_USER_DETAILS)) {
            var users = userUseCase.getDetailedUsers(signedInUser).stream();
            return ResponseEntity.ok(users.map(UserDetailsRepresentation::fromDomain).toList());
        } else {
            var users = userUseCase.getUsers(signedInUser).stream();
            return ResponseEntity.ok(users.map(UserRepresentation::fromDomain).toList());
        }
    }

    @RequestMapping(method = RequestMethod.GET, path = "/by-key/{key}")
    public ResponseEntity<UserDetailsRepresentation> getUserByKey(@PathVariable("key") String key) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        return userUseCase.getUserByKey(signedInUser, new UserKey(key))
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
}
