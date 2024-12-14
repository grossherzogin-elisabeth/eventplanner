package org.eventplanner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.services.ConsumptionListService;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.values.Permission;
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

    public ByteArrayOutputStream downloadConsumptionList(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey)
    throws IOException {
        signedInUser.assertHasPermission(Permission.READ_USERS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        log.info("Generating consumption list for event {} ({})", event.getName(), eventKey);
        return consumptionListService.generateConsumptionList(event);
    }
}
