package org.eventplanner.events.rest.account;

import org.eventplanner.events.application.usecases.AuthenticationUseCase;
import org.eventplanner.events.rest.account.dto.AccountRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/account")
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class AccountController {

    private final AuthenticationUseCase authenticationUseCase;

    @GetMapping("")
    public ResponseEntity<AccountRepresentation> getSignedInUser() {
        var signedInUser = authenticationUseCase.getSignedInUser();
        return ResponseEntity.ok(AccountRepresentation.fromDomain(signedInUser));
    }
}
