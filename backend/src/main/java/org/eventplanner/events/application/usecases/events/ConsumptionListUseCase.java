package org.eventplanner.events.application.usecases.events;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eventplanner.events.application.services.ConsumptionListService;
import org.eventplanner.events.application.services.EventService;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.Permission;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumptionListUseCase {
    private final EventService eventService;
    private final ConsumptionListService consumptionListService;

    public ByteArrayOutputStream downloadConsumptionList(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey eventKey
    )
    throws IOException {
        signedInUser.assertHasPermission(Permission.READ_USERS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var event = this.eventService.findByKey(eventKey).orElseThrow();
        log.info("Generating consumption list for event {}", event.details().getName());
        return consumptionListService.generateConsumptionList(event);
    }
}
