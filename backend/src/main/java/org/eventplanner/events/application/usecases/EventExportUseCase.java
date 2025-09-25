package org.eventplanner.events.application.usecases;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.services.ExcelExportService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.entities.positions.Position;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventExportUseCase {
    private final ExcelExportService excelExportService;
    private final EventRepository eventRepository;
    private final UserService userService;
    private final PositionRepository positionRepository;
    private final QualificationRepository qualificationRepository;

    public @NonNull ByteArrayOutputStream exportEvent(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey eventKey,
        @NonNull final String templateName
    ) {
        signedInUser.assertHasPermission(Permission.READ_USER_DETAILS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var model = getEventExportModel(eventKey);
        var template = resolveResourceFile("templates/excel/" + templateName + ".xlsx")
            .orElseThrow(() -> new NoSuchElementException("Cannot find template file"));
        return excelExportService.exportToExcel(template, model);
    }

    private @NonNull Map<String, Object> getEventExportModel(@NonNull final EventKey eventKey) {
        var event = eventRepository.findByKey(eventKey)
            .orElseThrow(() -> new NoSuchElementException("Event does not exist"));
        var positions = positionRepository.findAllAsMap();
        var users = userService.getDetailedUsers();

        Map<String, Object> model = new HashMap<>();
        model.put("event", event);
        model.put("crew", resolveRegistrations(event, users, positions, true));
        model.put("waitinglist", resolveRegistrations(event, users, positions, false));
        model.put(
            "qualifications", qualificationRepository.findAll().stream()
                .collect(Collectors.toMap(it -> it.getKey().value(), it -> it))
        );
        return model;
    }

    private @NonNull List<ResolvedRegistration> resolveRegistrations(
        @NonNull final Event event,
        @NonNull final List<UserDetails> users,
        @NonNull final Map<PositionKey, Position> positions,
        boolean assigned
    ) {
        var crew = new LinkedList<ResolvedRegistration>();
        var assignedRegistrationKeys = event.getAssignedRegistrationKeys();
        var assignedRegistrations = event.getRegistrations().stream()
            .filter(it -> assignedRegistrationKeys.contains(it.getKey()) == assigned)
            .sorted((a, b) -> {
                var pa = positions.get(a.getPosition());
                var pb = positions.get(b.getPosition());
                return pb.getPriority() - pa.getPriority();
            })
            .toList();

        var i = 0;
        for (var registration : assignedRegistrations) {
            var user = users.stream()
                .filter(it -> it.getKey().equals(registration.getUserKey()))
                .findFirst()
                .orElse(null);
            if (user == null) {
                continue;
            }
            crew.add(new ResolvedRegistration(
                i++,
                registration,
                user,
                positions.get(registration.getPosition())
            ));
        }

        return crew;
    }

    private @NonNull Optional<File> resolveResourceFile(@NonNull String path) {
        try {
            var url = getClass().getClassLoader().getResource(path);
            if (url == null) {
                return Optional.empty();
            }
            return Optional.of(new File(url.toURI()));
        } catch (Exception e) {
            log.error("Error read resource file", e);
        }
        return Optional.empty();
    }

    @Getter
    @AllArgsConstructor
    public static class ResolvedRegistration {
        private int number;
        private @NonNull Registration registration;
        private @NonNull UserDetails user;
        private @NonNull Position position;

        @Override
        public @NonNull String toString() {
            return user.getFullName();
        }
    }
}
