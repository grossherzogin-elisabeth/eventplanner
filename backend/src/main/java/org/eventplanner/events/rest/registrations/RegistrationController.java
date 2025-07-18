package org.eventplanner.events.rest.registrations;

import java.util.Objects;

import org.eventplanner.events.application.usecases.RegistrationConfirmationUseCase;
import org.eventplanner.events.application.usecases.RegistrationUseCase;
import org.eventplanner.events.application.usecases.UserUseCase;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.rest.events.dto.EventRepresentation;
import org.eventplanner.events.rest.registrations.dto.CreateRegistrationRequest;
import org.eventplanner.events.rest.registrations.dto.UpdateRegistrationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/events")
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class RegistrationController {

    private final UserUseCase userUseCase;
    private final RegistrationUseCase registrationUseCase;
    private final RegistrationConfirmationUseCase registrationConfirmationUseCase;

    @PostMapping("/{eventKey}/registrations")
    public ResponseEntity<EventRepresentation> createRegistration(
        @PathVariable("eventKey") String eventKey,
        @RequestBody CreateRegistrationRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var isSelfSignup = Objects.equals(signedInUser.key().value(), spec.userKey());
        var event =
            registrationUseCase.createRegistration(signedInUser, spec.toDomain(new EventKey(eventKey), isSelfSignup));
        return ResponseEntity.status(HttpStatus.CREATED).body(EventRepresentation.fromDomain(event));
    }

    @DeleteMapping("/{eventKey}/registrations/{registrationKey}")
    public ResponseEntity<EventRepresentation> deleteRegistration(
        @PathVariable("eventKey") String eventKey,
        @PathVariable("registrationKey") String registrationKey
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = registrationUseCase.removeRegistration(
            signedInUser,
            new EventKey(eventKey),
            new RegistrationKey(registrationKey)
        );
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }

    @PutMapping("/{eventKey}/registrations/{registrationKey}")
    public ResponseEntity<EventRepresentation> updateRegistration(
        @PathVariable("eventKey") String eventKey,
        @PathVariable("registrationKey") String registrationKey,
        @RequestBody UpdateRegistrationRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = registrationUseCase.updateRegistration(
            signedInUser,
            spec.toDomain(
                new EventKey(eventKey)
            )
        );
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }

    @GetMapping("/{eventKey}/registrations/{registrationKey}/confirm")
    public ResponseEntity<Void> confirmRegistration(
        @PathVariable("eventKey") String eventKey,
        @PathVariable("registrationKey") String registrationKey,
        @RequestParam("accessKey") String accessKey
    ) {
        registrationConfirmationUseCase.confirmRegistration(
            new EventKey(eventKey),
            new RegistrationKey(registrationKey),
            accessKey
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{eventKey}/registrations/{registrationKey}/decline")
    public ResponseEntity<Void> declineRegistration(
        @PathVariable("eventKey") String eventKey,
        @PathVariable("registrationKey") String registrationKey,
        @RequestParam("accessKey") String accessKey
    ) {
        registrationConfirmationUseCase.declineRegistration(
            new EventKey(eventKey),
            new RegistrationKey(registrationKey),
            accessKey
        );
        return ResponseEntity.ok().build();
    }
}
