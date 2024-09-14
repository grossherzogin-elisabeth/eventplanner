package org.eventplanner.testdata;

import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.values.EventState;
import org.eventplanner.users.adapter.UserRepository;
import org.eventplanner.users.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

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