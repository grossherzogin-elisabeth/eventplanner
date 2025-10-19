package org.eventplanner.events.application.usecases;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.eventplanner.common.StringUtils;
import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.services.EventMatrixExportService;
import org.eventplanner.events.application.services.EventService;
import org.eventplanner.events.application.services.ExcelExportService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.entities.positions.Position;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.EventSignupType;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.beans.factory.annotation.Value;
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
    private final EventService eventService;
    private final ExcelExportService excelExportService;
    private final EventMatrixExportService eventMatrixExportService;
    private final UserService userService;
    private final PositionRepository positionRepository;
    private final QualificationRepository qualificationRepository;

    @Value("${templates.directory}")
    private String templatesDirectory;

    public @NonNull List<String> getAvailableTemplates(@NonNull final SignedInUser signedInUser) {
        var dir = new File(templatesDirectory);
        if (!dir.exists()) {
            log.warn("Template directory {} does not exist", templatesDirectory);
            return Collections.emptyList();
        }
        var files = dir.listFiles();
        if (files == null) {
            log.info("Template directory {} does not contain any template", templatesDirectory);
            return Collections.emptyList();
        }
        return Arrays.stream(files)
            .map(File::getName)
            .filter(name -> !name.startsWith("~$"))
            .filter(name -> name.endsWith(".xlsx"))
            .map(name -> name.substring(0, name.lastIndexOf(".")))
            .toList();
    }

    public @NonNull ByteArrayOutputStream exportEvent(
        @NonNull final SignedInUser signedInUser,
        @NonNull final EventKey eventKey,
        @NonNull final String templateName
    ) {
        signedInUser.assertHasPermission(Permission.READ_USER_DETAILS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var event = eventService.getEvent(signedInUser, eventKey);
        var model = getEventExportModel(event);
        var template = new File(templatesDirectory, templateName + ".xlsx");
        if (!template.exists()) {
            throw new NoSuchElementException("Cannot find template file");
        }
        log.info("Generating excel export {} for event {}", templateName, event.getName());
        return excelExportService.exportToExcel(template, model);
    }

    public @NonNull ByteArrayOutputStream exportEventMatrix(
        @NonNull final SignedInUser signedInUser,
        final int year
    ) {
        signedInUser.assertHasPermission(Permission.READ_USERS);
        signedInUser.assertHasPermission(Permission.READ_EVENTS);

        var events = eventService.getEvents(signedInUser, year);
        log.info("Generating event matric for {} events of year {}", events.size(), year);
        return eventMatrixExportService.exportEventMatrix(events);
    }

    private @NonNull Map<String, Object> getEventExportModel(@NonNull final Event event) {
        var positions = positionRepository.findAllAsMap();
        var users = userService.getDetailedUsers();

        Map<String, Object> model = new HashMap<>();
        model.put("event", event);
        var assignedRegistrations = resolveRegistrations(event, users, positions, true);
        var unassignedRegistrations = resolveRegistrations(event, users, positions, false);
        if (event.getSignupType().equals(EventSignupType.ASSIGNMENT)) {
            model.put("crew", assignedRegistrations);
            model.put("waitinglist", unassignedRegistrations);
        } else {
            var allRegistrations = new ArrayList<>();
            allRegistrations.addAll(assignedRegistrations);
            allRegistrations.addAll(unassignedRegistrations);
            model.put("crew", allRegistrations);
            model.put("waitinglist", Collections.emptyList());
        }
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
                if (StringUtils.isBlank(registration.getName())) {
                    log.info("Skipping registration without user key and name");
                    continue;
                }
                user = new UserDetails(
                    new UserKey(),
                    Instant.now(),
                    Instant.now(),
                    "",
                    registration.getName()
                );
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
