package org.eventplanner.rest.account;

import org.eventplanner.application.usecases.UserUseCase;
import org.eventplanner.rest.account.dto.AccountRepresentation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/account")
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class AccountController {

    private final UserUseCase userUseCase;

    @GetMapping("")
    public ResponseEntity<AccountRepresentation> getSignedInUser() {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        return ResponseEntity.ok(AccountRepresentation.fromDomain(signedInUser));
    }
}
