package org.eventplanner.events.rest;

import java.util.List;

import org.eventplanner.events.EventUseCase;
import org.eventplanner.events.rest.dto.*;
import org.eventplanner.events.spec.UpdateRegistrationSpec;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.UserUseCase;
import org.eventplanner.users.values.UserKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@EnableMethodSecurity(securedEnabled = true)
public class EventController {

    private final UserUseCase userUseCase;
    private final EventUseCase eventUseCase;

    public EventController(
        @Autowired UserUseCase userUseCase,
        @Autowired EventUseCase eventUseCase
    ) {
        this.userUseCase = userUseCase;
        this.eventUseCase = eventUseCase;
    }

    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<List<EventRepresentation>> getEvents(@RequestParam("year") int year) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var events = eventUseCase.getEvents(signedInUser, year)
            .stream()
            .map(EventRepresentation::fromDomain)
            .toList();
        return ResponseEntity.ok(events);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{eventKey}")
    public ResponseEntity<EventRepresentation> getEventByKey(
        @PathVariable("eventKey") String eventKey
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = eventUseCase.getEventByKey(signedInUser, new EventKey(eventKey));
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.POST, path = "")
    public ResponseEntity<EventRepresentation> createEvent(@RequestBody CreateEventRequest spec) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = eventUseCase.createEvent(signedInUser, spec.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.PATCH, path = "/{eventKey}")
    public ResponseEntity<EventRepresentation> updateEvent(
        @PathVariable("eventKey") String eventKey,
        @RequestBody UpdateEventRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = eventUseCase.updateEvent(signedInUser, new EventKey(eventKey), spec.toDomain());
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{eventKey}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("eventKey") String eventKey) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        eventUseCase.deleteEvent(signedInUser, new EventKey(eventKey));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{eventKey}/registrations")
    public ResponseEntity<EventRepresentation> createRegistration(
        @PathVariable("eventKey") String eventKey,
        @RequestBody CreateRegistrationRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = eventUseCase.addRegistration(signedInUser, new EventKey(eventKey), spec.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{eventKey}/registrations/{registrationKey}")
    public ResponseEntity<EventRepresentation> deleteRegistration(
        @PathVariable("eventKey") String eventKey,
        @PathVariable("registrationKey") String registrationKey
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = eventUseCase.removeRegistration(signedInUser, new EventKey(eventKey), new RegistrationKey(registrationKey));
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{eventKey}/registrations/{registrationKey}")
    public ResponseEntity<EventRepresentation> updateRegistration(
        @PathVariable("eventKey") String eventKey,
        @PathVariable("registrationKey") String registrationKey,
        @RequestBody UpdateRegistrationRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = eventUseCase.updateRegistration(signedInUser, new EventKey(eventKey), new RegistrationKey(registrationKey), spec.toDomain());
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }
}
