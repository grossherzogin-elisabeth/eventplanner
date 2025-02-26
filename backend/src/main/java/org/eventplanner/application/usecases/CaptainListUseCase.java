package org.eventplanner.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.eventplanner.application.ports.EventRepository;
import org.eventplanner.application.services.CaptainListService;
import org.eventplanner.domain.values.EventKey;
import org.eventplanner.domain.entities.SignedInUser;
import org.eventplanner.domain.values.Permission;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptainListUseCase {
    private final EventRepository eventRepository;
    private final CaptainListService captainListService;

    public ByteArrayOutputStream downloadCaptainList(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey)
    throws
    IOException {
        signedInUser.assertHasPermission(Permission.READ_USER_DETAILS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        log.info("Generating Captainlist for event {} ({})", event.getName(), eventKey);
        return captainListService.generateCaptainList(event);
    }
}
