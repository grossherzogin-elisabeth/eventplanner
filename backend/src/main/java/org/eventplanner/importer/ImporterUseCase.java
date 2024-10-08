package org.eventplanner.importer;

import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.entities.Event;
import org.eventplanner.importer.entities.ImportError;
import org.eventplanner.importer.service.EventExcelImporter;
import org.eventplanner.importer.service.UserExcelImporter;
import org.eventplanner.users.adapter.UserRepository;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.entities.UserDetails;
import org.eventplanner.users.service.UserService;
import org.eventplanner.users.values.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImporterUseCase {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final String dataDirectory;
    private final String password;

    public ImporterUseCase(
        @Autowired EventRepository eventRepository,
        @Autowired UserRepository userRepository,
        @Autowired UserService userService,
        @Value("${custom.data-directory}") String dataDirectory,
        @Value("${custom.users-excel-password}") String password,
        @Value("${custom.generate-test-data}") boolean generateTestData
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.dataDirectory = dataDirectory;
        this.password = password;
        if (!generateTestData) {
            importOnStartup();
        }
    }

    private void importOnStartup() {
        var users = new File(dataDirectory + "/import/users.encrypted.xlsx");
        if (users.exists()) {
            log.info("Importing users from {}", users.getAbsolutePath());
            try {
                importUsersFromFile(users);
            } catch (Exception e) {
                log.error("Failed to import users on startup", e);
            }
        }

        var events2023 = new File(dataDirectory + "/import/events-2023.xlsx");
        if (events2023.exists()) {
            log.info("Importing events 2023 from {}", events2023.getAbsolutePath());
            try {
                importEventsFromFile(2023, events2023);
            } catch (Exception e) {
                log.error("Failed to import events 2023 on startup", e);
            }
        }

        var events2024 = new File(dataDirectory + "/import/events-2024.xlsx");
        if (events2024.exists()) {
            log.info("Importing events 2024 from {}", events2024.getAbsolutePath());
            try {
                importEventsFromFile(2024, events2024);
            } catch (Exception e) {
                log.error("Failed to import events 2024 on startup", e);
            }
        }
    }

    public List<ImportError> importEvents(@NonNull SignedInUser signedInUser, int year, InputStream stream) {
        signedInUser.assertHasPermission(Permission.WRITE_EVENTS);

        var tempFile = new File(dataDirectory + "/import/events-" + year + ".tmp.xlsx");
        try {
            new File(tempFile.getParent()).mkdirs();
            Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to save temporary events import file", e);
        }

        var errors = importEventsFromFile(year, tempFile);

        try {
            var file = new File(dataDirectory + "/import/events-" + year + ".xlsx");
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to save events import file", e);
        }

        return errors;
    }

    private List<ImportError> importEventsFromFile(int year, @NonNull File file) {
        var users = userService.getDetailedUsers();
        var errors = new ArrayList<ImportError>();
        var events = EventExcelImporter.readFromFile(file, year, users, errors);
        eventRepository.deleteAllByYear(year);
        for (Event event : events) {
            eventRepository.create(event);
        }
        return errors;
    }

    public void importUsers(@NonNull SignedInUser signedInUser, InputStream stream) {
        signedInUser.assertHasPermission(Permission.WRITE_USERS);

        var tempFile = new File(dataDirectory + "/import/users.tmp.xlsx");
        try {
            new File(tempFile.getParent()).mkdirs();
            Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to save temporary users import file", e);
        }

        importUsersFromFile(tempFile);

        try {
            var file = new File(dataDirectory + "/import/users.xlsx");
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to save users import file", e);
        }
    }

    private void importUsersFromFile(File file) {
        var users = UserExcelImporter.readFromFile(file, password);
        userRepository.deleteAll();
        for (UserDetails user : users) {
            userService.createUser(user);
        }
    }
}
