package org.eventplanner.users.rest;

import org.eventplanner.users.UserUseCase;
import org.eventplanner.users.rest.dto.AccountRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/account")
@EnableMethodSecurity(securedEnabled = true)
public class AccountController {

    private final UserUseCase userUseCase;

    public AccountController(@Autowired UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<AccountRepresentation> getSignedInUser() {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok(AccountRepresentation.fromDomain(signedInUser));
    }
}
