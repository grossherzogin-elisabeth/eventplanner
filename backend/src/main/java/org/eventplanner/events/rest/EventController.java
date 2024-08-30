package org.eventplanner.events.rest;

import org.eventplanner.events.EventUseCase;
import org.eventplanner.events.rest.dto.*;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.UserUseCase;
import org.eventplanner.users.values.UserKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @RequestMapping(method = RequestMethod.GET, path = "/by-year/{year}")
    public ResponseEntity<List<EventRepresentation>> getEventsByYear(@PathVariable("year") int year) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var events = eventUseCase.getEvents(signedInUser, year)
            .stream()
            .map(EventRepresentation::fromDomain)
            .toList();
        return ResponseEntity.ok(events);
    }

    @RequestMapping(method = RequestMethod.POST, path = "")
    public ResponseEntity<EventRepresentation> createEvent(@RequestBody CreateEventRequest spec) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = eventUseCase.createEvent(signedInUser, spec.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/by-key/{eventKey}")
    public ResponseEntity<EventRepresentation> updateEvent(@PathVariable("eventKey") String eventKey, @RequestBody UpdateEventRequest spec) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = eventUseCase.updateEvent(signedInUser, new EventKey(eventKey), spec.toDomain());
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/by-key/{eventKey}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("eventKey") String eventKey) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        eventUseCase.deleteEvent(signedInUser, new EventKey(eventKey));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @RequestMapping(method = RequestMethod.POST, path = "/by-key/{eventKey}/waitinglist")
    public ResponseEntity<EventRepresentation> addUserToWaitingList(@PathVariable("eventKey") String eventKey, @RequestBody AddUserToWaitingListRequest spec) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = eventUseCase.addUserToWaitingList(
            signedInUser,
            new EventKey(eventKey),
            new UserKey(spec.userKey()),
            new PositionKey(spec.positionKey()));
        return ResponseEntity.status(HttpStatus.CREATED).body(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/by-key/{eventKey}/waitinglist/{userKey}")
    public ResponseEntity<EventRepresentation> removeUserFromWaitingList(@PathVariable("eventKey") String eventKey, @PathVariable("userKey") String userKey) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = eventUseCase.removeUserFromWaitingList(
            signedInUser,
            new EventKey(eventKey),
            new UserKey(userKey));
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/by-key/{eventKey}/registrations")
    public ResponseEntity<EventRepresentation> updateEventTeam(@PathVariable("eventKey") String eventKey, @RequestBody UpdateEventTeamRequest spec) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = eventUseCase.updateEventTeam(
            signedInUser,
            new EventKey(eventKey),
            spec.toDomain());
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/by-key/{eventKey}/registrations/{userKey}")
    public ResponseEntity<EventRepresentation> removeUserFromEventTeam(@PathVariable("eventKey") String eventKey, @PathVariable("userKey") String userKey) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = eventUseCase.removeUserFromTeam(
            signedInUser,
            new EventKey(eventKey),
            new UserKey(userKey));
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }
}
