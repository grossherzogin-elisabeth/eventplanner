package org.eventplanner.testdata;

import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.values.EventState;
import org.eventplanner.positions.entities.Position;
import org.eventplanner.users.adapter.UserRepository;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.service.UserService;
import org.eventplanner.users.values.Address;
import org.eventplanner.users.values.UserKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Service
public class TestDataGenerationService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public TestDataGenerationService(
        @Autowired EventRepository eventRepository,
        @Autowired UserRepository userRepository,
        @Autowired UserService userService,
        @Value("${custom.generate-test-data}") boolean generateTestData
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        if (generateTestData) {
            generateTestData();
        }
    }

    private void generateTestData() {
        log.info("Generating test data");
        var currentYear = ZonedDateTime.now().getYear();

        // delete old test data
        eventRepository.deleteAllByYear(currentYear - 1);
        eventRepository.deleteAllByYear(currentYear);
        eventRepository.deleteAllByYear(currentYear + 1);

        // generate users
        var users = UserGenerator.createTestUsers(200);
        users.forEach(userService::createUser);

        // generate events
        EventGenerator.createTestEvents(currentYear - 1, users, EventState.PLANNED).forEach(eventRepository::create);
        EventGenerator.createTestEvents(currentYear, users, EventState.PLANNED).forEach(eventRepository::create);
        EventGenerator.createTestEvents(currentYear + 1, users, EventState.OPEN_FOR_SIGNUP).forEach(eventRepository::create);
    }
}