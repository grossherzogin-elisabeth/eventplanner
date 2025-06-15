package org.eventplanner.events.application.usecases;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.services.ConsumptionListService;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.auth.Permission;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumtionListUseCase {
    private final EventRepository eventRepository;
    private final ConsumptionListService consumptionListService;

    public ByteArrayOutputStream downloadConsumptionList(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey eventKey
    )
    throws IOException {
        signedInUser.assertHasPermission(Permission.READ_USERS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        log.info("Generating consumption list for event {}", event.getName());
        return consumptionListService.generateConsumptionList(event);
    }
}
