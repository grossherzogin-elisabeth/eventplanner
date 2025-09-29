package org.eventplanner.events.application.services;

import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.eventplanner.testutil.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.EventSlot;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.entities.positions.Position;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.EventSignupType;
import org.eventplanner.events.domain.values.events.EventState;
import org.eventplanner.events.domain.values.events.EventType;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.domain.values.positions.PositionKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.eventplanner.testdata.PositionFactory;
import org.eventplanner.testdata.SlotFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EventMatrixExportServiceTest {

    private static List<UserDetails> users;
    private static Map<PositionKey, Position> positions;
    private EventMatrixExportService testee;

    @BeforeAll
    static void beforeAll() {
        positions = PositionFactory.generateDefaultPositions().stream()
            .collect(Collectors.toMap(Position::getKey, it -> it));
        users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            var user = new UserDetails(
                new UserKey(String.valueOf(i)),
                Instant.now(),
                Instant.now(),
                "FirstName_" + i,
                "LastName_" + i
            );
            users.add(user);
        }
    }

    @BeforeEach
    void beforeEach() {
        var positionRepository = mock(PositionRepository.class);
        var userService = mock(UserService.class);
        when(positionRepository.findAll()).thenReturn(positions.values().stream().toList());
        when(positionRepository.findAllAsMap()).thenReturn(positions);
        when(userService.getDetailedUsers()).thenReturn(users);
        testee = new EventMatrixExportService(
            positionRepository,
            userService
        );
    }

    @Test
    void shouldThrowWhenExceptionWhenEventsAreEmpty() {
        var e = catchThrowable(() -> testee.exportEventMatrix(Collections.emptyList()));
        assertThat(e).isNotNull().isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("provideSingleEvents")
    void shouldRenderCorrectPositions(Event event) throws Exception {
        var out = testee.exportEventMatrix(List.of(event));
        var outFile = tempFile();
        try (OutputStream outputStream = new FileOutputStream(outFile)) {
            out.writeTo(outputStream);
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook(outFile)) {
            var sheet = workbook.getSheetAt(0);

            assertThat(sheet).cell(0, 0).hasValue(2025);
            assertThat(sheet).cell(2, 0).hasValue("Anzahl");

            var expectedPositions = event.getSlots().stream()
                .map(slot -> event.findRegistrationByKey(slot.getAssignedRegistration())
                    .map(Registration::getPosition)
                    .orElse(slot.getPositions().getFirst()))
                .map(positions::get)
                .sorted((a, b) -> b.getPriority() - a.getPriority())
                .toList();
            for (int i = 0; i < expectedPositions.size(); i++) {
                var slot = expectedPositions.get(i);
                assertThat(sheet).cell(i + 3, 0)
                    .hasValue(slot.getName())
                    .hasFillBackgroundColor(slot.getColor());
            }
            assertThat(sheet).cell(expectedPositions.size() + 3, 0).hasValue("Warteliste");
        } finally {
            FileUtils.delete(outFile);
        }
    }

    @ParameterizedTest
    @MethodSource("provideEvents")
    void shouldRenderHeadersCorrectly(List<Event> events) throws Exception {
        var out = testee.exportEventMatrix(events);
        var outFile = tempFile();
        try (OutputStream outputStream = new FileOutputStream(outFile)) {
            out.writeTo(outputStream);
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook(outFile)) {
            var sheet = workbook.getSheetAt(0);
            for (int i = 0; i < events.size(); i++) {
                var event = events.get(i);
                var col = i + 1;
                assertThat(sheet).cell(0, col).hasValue(event.getName());
                assertThat(sheet).cell(2, col).hasValue(event.getAssignedRegistrationKeys().size());

                var hasWaitingList = findRowWithValue(sheet, col, "Warteliste") > 0;
                assertThat(hasWaitingList).isTrue();
            }
        } finally {
            FileUtils.delete(outFile);
        }
    }

    @ParameterizedTest
    @MethodSource("provideEvents")
    void shouldRenderAllCrewMembersAboveWaitingListMark(List<Event> events) throws Exception {
        var out = testee.exportEventMatrix(events);
        var outFile = tempFile();
        try (OutputStream outputStream = new FileOutputStream(outFile)) {
            out.writeTo(outputStream);
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook(outFile)) {
            var sheet = workbook.getSheetAt(0);
            for (int i = 0; i < events.size(); i++) {
                var event = events.get(i);
                var col = i + 1;

                var waitingListIndex = findRowWithValue(sheet, col, "Warteliste");
                for (var registration : event.getAssignedRegistrations()) {
                    var position = positions.get(registration.getPosition());
                    var user = users.stream()
                        .filter(it -> it.getKey().equals(registration.getUserKey()))
                        .findFirst()
                        .orElseThrow();
                    // find the user
                    var userIndex = findRowWithValue(sheet, col, user.getFullName());
                    assertThat(userIndex).isGreaterThan(0).isLessThan(waitingListIndex);
                    assertThat(sheet).cell(userIndex, 0).hasValue(position.getName());
                }
            }
        } finally {
            FileUtils.delete(outFile);
        }
    }

    @ParameterizedTest
    @MethodSource("provideEvents")
    void shouldRenderWaitingListMembersBelowWaitinListMark(List<Event> events) throws Exception {
        var out = testee.exportEventMatrix(events);
        var outFile = tempFile();
        try (OutputStream outputStream = new FileOutputStream(outFile)) {
            out.writeTo(outputStream);
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook(outFile)) {
            var sheet = workbook.getSheetAt(0);
            for (int i = 0; i < events.size(); i++) {
                var event = events.get(i);
                var col = i + 1;

                var waitingListIndex = findRowWithValue(sheet, col, "Warteliste");
                var assignedRegistrations = event.getAssignedRegistrations();
                var waitingList = event.getRegistrations().stream()
                    .filter(it -> !assignedRegistrations.contains(it))
                    .toList();
                for (var registration : waitingList) {
                    var position = positions.get(registration.getPosition());
                    var user = users.stream()
                        .filter(it -> it.getKey().equals(registration.getUserKey()))
                        .findFirst()
                        .orElseThrow();
                    // find the user
                    var userIndex = findRowWithValue(sheet, col, user.getFullName());
                    assertThat(userIndex).isGreaterThan(waitingListIndex);
                    assertThat(sheet).cell(userIndex, 0).hasValue(position.getName());
                }
            }
        } finally {
            FileUtils.delete(outFile);
        }
    }

    private int findRowWithValue(XSSFSheet sheet, int col, String value) {
        for (int r = 0; r <= sheet.getLastRowNum(); r++) {
            var row = sheet.getRow(r);
            var cell = row.getCell(col);
            if (cell.getCellType().equals(CellType.STRING) && cell.getStringCellValue().equals(value)) {
                return r;
            }
        }
        return -1;
    }

    private File tempFile() {
        var file = new File(getClass().getSimpleName() + "_" + UUID.randomUUID().toString().substring(0, 8) + ".xlsx");
        file.deleteOnExit();
        return file;
    }

    private static Stream<Arguments> provideSingleEvents() {
        return Stream.of(
            Arguments.argumentSet("event without registrations", createEventWithoutRegistrations()),
            Arguments.argumentSet("event with full crew", createEventWithCrewOnly()),
            Arguments.argumentSet("event with half full crew", createEventWithHalfCrewOnly()),
            Arguments.argumentSet("event with waiting list", createEventWithWaitingListOnly()),
            Arguments.argumentSet("event with crew and waiting list", createEventWithCrewAndWaitingList())
        );
    }

    private static Stream<Arguments> provideEvents() {
        return Stream.of(
            List.of(
                createEventWithHalfCrewOnly(),
                createEventWithHalfCrewOnly()
            ),
            List.of(
                createEventWithoutRegistrations(),
                createEventWithoutRegistrations()
            ),
            List.of(
                createEventWithCrewOnly(),
                createEventWithCrewOnly()
            ),
            List.of(
                createEventWithWaitingListOnly(),
                createEventWithWaitingListOnly()
            ),
            List.of(
                createEventWithCrewAndWaitingList(),
                createEventWithCrewAndWaitingList()
            ),
            List.of(
                createEventWithoutRegistrations(),
                createEventWithCrewOnly(),
                createEventWithWaitingListOnly()
            ),
            List.of(
                createEventWithoutRegistrations(),
                createEventWithCrewOnly(),
                createEventWithHalfCrewOnly(),
                createEventWithWaitingListOnly(),
                createEventWithCrewAndWaitingList(),
                createEventWithCrewAndWaitingList()
            )
        ).map(Arguments::of);
    }

    private static Event createEventWithoutRegistrations() {
        return new Event(
            new EventKey(),
            EventType.SINGLE_DAY_EVENT,
            EventSignupType.ASSIGNMENT,
            "Event without any registrations",
            EventState.PLANNED,
            "Eventnote",
            "Eventdescription",
            Instant.parse("2025-08-12T09:00:00.000Z"),
            Instant.parse("2025-08-12T16:00:00.000Z"),
            Collections.emptyList(),
            SlotFactory.createDefaultSlots(),
            Collections.emptyList(),
            0
        );
    }

    private static Event createEventWithWaitingListOnly() {
        var event = new Event(
            new EventKey(),
            EventType.SINGLE_DAY_EVENT,
            EventSignupType.ASSIGNMENT,
            "Event with waitinglist",
            EventState.PLANNED,
            "Eventnote",
            "Eventdescription",
            Instant.parse("2025-08-13T09:00:00.000Z"),
            Instant.parse("2025-08-13T16:00:00.000Z"),
            Collections.emptyList(),
            SlotFactory.createDefaultSlots(),
            Collections.emptyList(),
            0
        );

        var randomUsers = new ArrayList<>(users);
        Collections.shuffle(randomUsers);

        for (int i = 0; i < event.getSlots().size(); i += 2) {
            var slot = event.getSlots().get(i);
            var registration = new Registration(
                new RegistrationKey(),
                slot.getPositions().getLast(),
                randomUsers.removeFirst().getKey(),
                null,
                null,
                null,
                null,
                null,
                null
            );
            event.addRegistration(registration);
        }

        return event;
    }

    private static Event createEventWithCrewOnly() {
        var event = new Event(
            new EventKey(),
            EventType.SINGLE_DAY_EVENT,
            EventSignupType.ASSIGNMENT,
            "Event with crew",
            EventState.PLANNED,
            "Eventnote",
            "Eventdescription",
            Instant.parse("2025-08-14T09:00:00.000Z"),
            Instant.parse("2025-08-14T16:00:00.000Z"),
            Collections.emptyList(),
            SlotFactory.createDefaultSlots(),
            new ArrayList<>(),
            0
        );

        var randomUsers = new ArrayList<>(users);
        Collections.shuffle(randomUsers);

        for (EventSlot slot : event.getSlots()) {
            var registration = new Registration(
                new RegistrationKey(),
                slot.getPositions().getLast(),
                randomUsers.removeFirst().getKey(),
                null,
                null,
                null,
                null,
                null,
                null
            );
            slot.setAssignedRegistration(registration.getKey());
            event.addRegistration(registration);
        }

        return event;
    }

    private static Event createEventWithHalfCrewOnly() {
        var event = new Event(
            new EventKey(),
            EventType.SINGLE_DAY_EVENT,
            EventSignupType.ASSIGNMENT,
            "Event with half crew",
            EventState.PLANNED,
            "Eventnote",
            "Eventdescription",
            Instant.parse("2025-08-14T09:00:00.000Z"),
            Instant.parse("2025-08-14T16:00:00.000Z"),
            Collections.emptyList(),
            SlotFactory.createDefaultSlots(),
            new ArrayList<>(),
            0
        );

        var randomUsers = new ArrayList<>(users);
        Collections.shuffle(randomUsers);

        for (int i = 0; i < event.getSlots().size(); i += 2) {
            var slot = event.getSlots().get(i);
            var registration = new Registration(
                new RegistrationKey(),
                slot.getPositions().getLast(),
                randomUsers.removeFirst().getKey(),
                null,
                null,
                null,
                null,
                null,
                null
            );
            slot.setAssignedRegistration(registration.getKey());
            event.addRegistration(registration);
        }

        return event;
    }

    private static Event createEventWithCrewAndWaitingList() {
        var event = new Event(
            new EventKey(),
            EventType.SINGLE_DAY_EVENT,
            EventSignupType.ASSIGNMENT,
            "Event with crew and waitinglist",
            EventState.PLANNED,
            "Eventnote",
            "Eventdescription",
            Instant.parse("2025-08-15T09:00:00.000Z"),
            Instant.parse("2025-08-15T16:00:00.000Z"),
            Collections.emptyList(),
            SlotFactory.createDefaultSlots(),
            Collections.emptyList(),
            0
        );
        var randomUsers = new ArrayList<>(users);
        Collections.shuffle(randomUsers);

        for (EventSlot slot : event.getSlots()) {
            var registration = new Registration(
                new RegistrationKey(),
                slot.getPositions().getLast(),
                randomUsers.removeFirst().getKey(),
                null,
                null,
                null,
                null,
                null,
                null
            );
            slot.setAssignedRegistration(registration.getKey());
            event.addRegistration(registration);
        }

        for (int i = 0; i < event.getSlots().size(); i += 2) {
            var slot = event.getSlots().get(i);
            var registration = new Registration(
                new RegistrationKey(),
                slot.getPositions().getLast(),
                randomUsers.removeFirst().getKey(),
                null,
                null,
                null,
                null,
                null,
                null
            );
            event.addRegistration(registration);
        }
        return event;
    }
}