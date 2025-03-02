package org.eventplanner.events.application.usecases;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.services.CaptainListService;
import org.eventplanner.events.domain.entities.SignedInUser;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.Permission;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptainListUseCase {
    private final EventRepository eventRepository;
    private final CaptainListService captainListService;

    public @NonNull ByteArrayOutputStream downloadCaptainList(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey eventKey
    ) throws IOException {
        signedInUser.assertHasPermission(Permission.READ_USER_DETAILS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        log.info("Generating Captainlist for event {}", event.getName());
        return captainListService.generateCaptainList(event);
    }
}
