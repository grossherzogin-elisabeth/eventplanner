package org.eventplanner.events.rest.events;

import java.util.List;

import org.eventplanner.events.application.usecases.EventExportUseCase;
import org.eventplanner.events.application.usecases.EventUseCase;
import org.eventplanner.events.application.usecases.RegistrationConfirmationUseCase;
import org.eventplanner.events.application.usecases.UpdateEventUseCase;
import org.eventplanner.events.application.usecases.UserUseCase;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.rest.events.dto.CreateEventRequest;
import org.eventplanner.events.rest.events.dto.EventRepresentation;
import org.eventplanner.events.rest.events.dto.EventSlotRepresentation;
import org.eventplanner.events.rest.events.dto.OptimizeEventSlotsRequest;
import org.eventplanner.events.rest.events.dto.UpdateEventRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/events")
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class EventController {

    private final UserUseCase userUseCase;
    private final EventUseCase eventUseCase;
    private final UpdateEventUseCase updateEventUseCase;
    private final EventExportUseCase eventExportUseCase;
    private final RegistrationConfirmationUseCase registrationConfirmationUseCase;

    @GetMapping("")
    public ResponseEntity<?> getEvents(
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @RequestParam("year") int year
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        if (accept.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            var stream = eventExportUseCase.exportEventMatrix(signedInUser, year);
            byte[] binary = stream.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(binary);
            return ResponseEntity.ok()
                .contentLength(binary.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
        }

        var events = eventUseCase.getEvents(signedInUser, year)
            .stream()
            .map(EventRepresentation::fromDomain)
            .toList();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventKey}")
    public ResponseEntity<EventRepresentation> getEventByKey(
        @PathVariable("eventKey") String eventKey,
        @Nullable @RequestParam("accessKey") String accessKey
    ) {
        try {
            var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
            var event = eventUseCase.getEventByKey(signedInUser, new EventKey(eventKey));
            return ResponseEntity.ok(EventRepresentation.fromDomain(event));
        } catch (UnauthorizedException e) {
            if (accessKey != null) {
                var event = registrationConfirmationUseCase.getEventByAccessKey(new EventKey(eventKey), accessKey);
                return ResponseEntity.ok(EventRepresentation.fromDomain(event));
            } else {
                throw e;
            }
        }
    }

    @PostMapping("")
    public ResponseEntity<EventRepresentation> createEvent(@RequestBody @Valid CreateEventRequest spec) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = eventUseCase.createEvent(signedInUser, spec.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(EventRepresentation.fromDomain(event));
    }

    @PatchMapping("/{eventKey}")
    public ResponseEntity<EventRepresentation> updateEvent(
        @PathVariable("eventKey") String eventKey,
        @RequestBody UpdateEventRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = updateEventUseCase.updateEvent(signedInUser, spec.toDomain(new EventKey(eventKey)));
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }

    @DeleteMapping("/{eventKey}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("eventKey") String eventKey) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        eventUseCase.deleteEvent(signedInUser, new EventKey(eventKey));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/export/templates")
    public ResponseEntity<List<String>> getExportTemplates() {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var templates = eventExportUseCase.getAvailableTemplates(signedInUser);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{eventKey}/export/{templateName}")
    public ResponseEntity<Resource> exportEvent(
        @PathVariable("eventKey") String eventKey,
        @PathVariable("templateName") String templateName
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var outputStream = eventExportUseCase.exportEvent(signedInUser, new EventKey(eventKey), templateName);
        byte[] bytes = outputStream.toByteArray();
        return ResponseEntity.ok()
            .contentLength(bytes.length)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(new ByteArrayResource(bytes));
    }

    @PostMapping("/{eventKey}/optimized-slots")
    public ResponseEntity<List<EventSlotRepresentation>> optimizeEventSlots(
        @PathVariable("eventKey") String eventKey,
        @RequestBody OptimizeEventSlotsRequest request
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        var event = request.toDomain(eventUseCase.getEventByKey(signedInUser, new EventKey(eventKey)));
        event = eventUseCase.optimizeEventSlots(signedInUser, event);
        var representations = event.getSlots().stream().map(EventSlotRepresentation::fromDomain).toList();
        return ResponseEntity.status(HttpStatus.OK).body(representations);
    }
}
