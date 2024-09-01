package org.eventplanner.testdata;

import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.entities.Event;
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

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Service
public class TestDataGenerationService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final String password;
    private final Environment env;

    public TestDataGenerationService(
        @Autowired EventRepository eventRepository,
        @Autowired UserRepository userRepository,
        @Autowired UserService userService,
        @Value("${custom.users-excel-password}") String password,
        Environment env
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.password = password;
        this.env = env;
        if (Arrays.stream(env.getActiveProfiles()).toList().contains("test")) {
            generateTestData();
        }
    }

    private void generateTestData() {
        log.info("Generating test data");

        for (UserDetails user : createTestUsers()) {
            userService.createUser(user);
        }

        var events = new LinkedList<Event>();
        for (Event event : events) {
            eventRepository.create(event);
        }
    }

    public List<UserDetails> createTestUsers() {
        var users = new LinkedList<UserDetails>();
        for (int i = 0; i < 200; i++) {
            var name = Names.get(i).split(" ");
            var firstName = name[0];
            var lastName = name[1];
            var user = new UserDetails(new UserKey("user-" + i), firstName, lastName);
            user.setEmail(firstName + "." + lastName + "@example.com");
            user.setAddress(new Address("Teststraße 1", null, "Teststadt", "12345"));
            user.setDateOfBirth(ZonedDateTime.now());
            user.setMobile("+49 123456789");
            user.setPhone("+49 123456789");
            user.setPlaceOfBirth("Teststadt");
            user.setPassNr("PA12345");
            user.setComment("This is an auto generated test user");

            users.add(user);
        }
        return users;
    }
}