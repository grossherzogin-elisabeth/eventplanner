package org.eventplanner.importer;

import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.entities.Event;
import org.eventplanner.importer.entities.ImportError;
import org.eventplanner.importer.service.EventExcelImporter;
import org.eventplanner.importer.service.PositionJsonImporter;
import org.eventplanner.importer.service.QualificationJsonImporter;
import org.eventplanner.importer.service.UserExcelImporter;
import org.eventplanner.positions.adapter.PositionRepository;
import org.eventplanner.positions.entities.Position;
import org.eventplanner.qualifications.adapter.QualificationRepository;
import org.eventplanner.qualifications.entities.Qualification;
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
    private final PositionRepository positionRepository;
    private final QualificationRepository qualificationRepository;
    private final UserService userService;
    private final String dataImportDirectory;
    private final String password;

    public ImporterUseCase(
            @Autowired EventRepository eventRepository,
            @Autowired UserRepository userRepository,
            @Autowired UserService userService,
            PositionRepository positionRepository,
            QualificationRepository qualificationRepository,
            @Value("${data.import.directory}") String dataImportDirectory,
            @Value("${data.import.users-excel-password}") String password,
            @Value("${data.generate-test-data}") boolean generateTestData
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.positionRepository = positionRepository;
        this.qualificationRepository = qualificationRepository;
        this.userService = userService;
        this.dataImportDirectory = dataImportDirectory;
        this.password = password;
        if (!generateTestData) {
            importOnStartup();
        }
    }

    private void importOnStartup() {
        var positions = new File(dataImportDirectory + "/positions");
        if (positions.exists()) {
            log.info("Importing positions from {}", positions.getAbsolutePath());
            importPositionsFromDirectory(positions);
        }

        var qualifications = new File(dataImportDirectory + "/qualifications");
        if (qualifications.exists()) {
            log.info("Importing qualifications from {}", qualifications.getAbsolutePath());
            importQualificationsFromDirectory(qualifications);
        }

        var users = new File(dataImportDirectory + "/users.encrypted.xlsx");
        if (users.exists()) {
            log.info("Importing users from {}", users.getAbsolutePath());
            try {
                importUsersFromFile(users);
            } catch (Exception e) {
                log.error("Failed to import users on startup", e);
            }
        }

        var events2023 = new File(dataImportDirectory + "/events-2023.xlsx");
        if (events2023.exists()) {
            log.info("Importing events 2023 from {}", events2023.getAbsolutePath());
            try {
                importEventsFromFile(2023, events2023);
            } catch (Exception e) {
                log.error("Failed to import events 2023 on startup", e);
            }
        }

        var events2024 = new File(dataImportDirectory + "/events-2024.xlsx");
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

        var tempFile = new File(dataImportDirectory + "/events-" + year + ".tmp.xlsx");
        try {
            new File(tempFile.getParent()).mkdirs();
            Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to save temporary events import file", e);
        }

        var errors = importEventsFromFile(year, tempFile);

        try {
            var file = new File(dataImportDirectory + "/events-" + year + ".xlsx");
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

        var tempFile = new File(dataImportDirectory + "/users.tmp.xlsx");
        try {
            new File(tempFile.getParent()).mkdirs();
            Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to save temporary users import file", e);
        }

        importUsersFromFile(tempFile);

        try {
            var file = new File(dataImportDirectory + "/users.encrypted.xlsx");
            Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Failed to save users import file", e);
        }
    }

    private void importUsersFromFile(File file) {
        var users = UserExcelImporter.readFromFile(file, password, qualificationRepository.findAll());
        userRepository.deleteAll();
        for (UserDetails user : users) {
            userService.createUser(user);
        }
    }

    private void importPositionsFromDirectory(File directory) {
        var positions = PositionJsonImporter.readFromDirectory(directory);
        positionRepository.deleteAll();
        for (Position position : positions) {
            positionRepository.create(position);
        }
    }

    private void importQualificationsFromDirectory(File directory) {
        var qualifications = QualificationJsonImporter.readFromDirectory(directory);
        qualificationRepository.deleteAll();
        for (Qualification qualification : qualifications) {
            qualificationRepository.create(qualification);
        }
    }
}
