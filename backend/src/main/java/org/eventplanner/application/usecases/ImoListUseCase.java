package org.eventplanner.application.usecases;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eventplanner.application.ports.EventRepository;
import org.eventplanner.application.services.ImoListService;
import org.eventplanner.domain.values.EventKey;
import org.eventplanner.domain.entities.SignedInUser;
import org.eventplanner.domain.values.Permission;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImoListUseCase {
    private final EventRepository eventRepository;
    private final ImoListService imoListService;

    public ByteArrayOutputStream downloadImoList(@NonNull SignedInUser signedInUser, @NonNull EventKey eventKey) throws
                                                                                                                 IOException {
        signedInUser.assertHasPermission(Permission.READ_USER_DETAILS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var event = this.eventRepository.findByKey(eventKey).orElseThrow();
        log.info("Generating IMO list for event {} ({})", event.getName(), eventKey);
        return imoListService.generateImoList(event);
    }
}
