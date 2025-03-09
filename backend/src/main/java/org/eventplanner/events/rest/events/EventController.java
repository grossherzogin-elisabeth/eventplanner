package org.eventplanner.events.rest.events;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eventplanner.events.application.usecases.UserUseCase;
import org.eventplanner.events.application.usecases.events.CaptainListUseCase;
import org.eventplanner.events.application.usecases.events.ConfirmParticipationUseCase;
import org.eventplanner.events.application.usecases.events.ConsumptionListUseCase;
import org.eventplanner.events.application.usecases.events.EventUseCase;
import org.eventplanner.events.application.usecases.events.ImoListUseCase;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.rest.events.dto.CreateEventRequest;
import org.eventplanner.events.rest.events.dto.EventRepresentation;
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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/events")
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class EventController {

    private final UserUseCase userUseCase;
    private final EventUseCase eventUseCase;
    private final ImoListUseCase imoListUseCase;
    private final ConsumptionListUseCase consumptionListUseCase;
    private final CaptainListUseCase captainListUseCase;
    private final ConfirmParticipationUseCase confirmParticipationUseCase;

    @GetMapping("")
    public ResponseEntity<?> getEvents(
        @RequestHeader(HttpHeaders.ACCEPT) String accept,
        @RequestParam("year") int year
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        if (accept.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            var stream = eventUseCase.exportEvents(signedInUser, year);
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
                var event = eventUseCase.getEventByAccessKey(new EventKey(eventKey), accessKey);
                return ResponseEntity.ok(EventRepresentation.fromDomain(event));
            } else {
                throw e;
            }
        }
    }

    @PostMapping("")
    public ResponseEntity<EventRepresentation> createEvent(@RequestBody CreateEventRequest spec) {
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
        var event = eventUseCase.updateEvent(signedInUser, new EventKey(eventKey), spec.toDomain());
        return ResponseEntity.ok(EventRepresentation.fromDomain(event));
    }

    @DeleteMapping("/{eventKey}")
    public ResponseEntity<Void> deleteEvent(@PathVariable("eventKey") String eventKey) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        eventUseCase.deleteEvent(signedInUser, new EventKey(eventKey));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{eventKey}/imo-list")
    public ResponseEntity<Resource> downloadImoList(@PathVariable("eventKey") String eventKey) throws IOException {

        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        ByteArrayOutputStream imoListStream = imoListUseCase.downloadImoList(signedInUser, new EventKey(eventKey));
        byte[] imoListByteArray = imoListStream.toByteArray();

        ByteArrayResource resource = new ByteArrayResource(imoListByteArray);

        return ResponseEntity.ok()
            .contentLength(imoListByteArray.length)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }

    @GetMapping("/{eventKey}/consumption-list")
    public ResponseEntity<Resource> downloadConsumptionList(@PathVariable("eventKey") String eventKey)
    throws IOException {

        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        ByteArrayOutputStream consumptionListStream =
            consumptionListUseCase.downloadConsumptionList(signedInUser, new EventKey(eventKey));
        byte[] consumptionListByteArray = consumptionListStream.toByteArray();

        ByteArrayResource resource = new ByteArrayResource(consumptionListByteArray);

        return ResponseEntity.ok()
            .contentLength(consumptionListByteArray.length)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }

    @GetMapping("/{eventKey}/captain-list")
    public ResponseEntity<Resource> downloadCaptainList(@PathVariable("eventKey") String eventKey)
    throws IOException {

        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        ByteArrayOutputStream captainListStream =
            captainListUseCase.downloadCaptainList(signedInUser, new EventKey(eventKey));
        byte[] captainListByteArray = captainListStream.toByteArray();

        ByteArrayResource resource = new ByteArrayResource(captainListByteArray);

        return ResponseEntity.ok()
            .contentLength(captainListByteArray.length)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }
}
