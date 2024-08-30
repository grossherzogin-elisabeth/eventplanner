package org.eventplanner.importer.rest;

import org.eventplanner.exceptions.HandledException;
import org.eventplanner.importer.ImporterUseCase;
import org.eventplanner.importer.rest.dto.ImportErrorRepresentation;
import org.eventplanner.users.UserUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/import")
@EnableMethodSecurity(securedEnabled = true)
public class ImporterController {

    private static final Logger log = LoggerFactory.getLogger(ImporterController.class);
    private final ImporterUseCase importerUseCase;
    private final UserUseCase userUseCase;

    public ImporterController(
        @Autowired UserUseCase userUseCase,
        @Autowired ImporterUseCase importerUseCase
    ) {
        this.userUseCase = userUseCase;
        this.importerUseCase = importerUseCase;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/events/{year}")
    public ResponseEntity<List<ImportErrorRepresentation>> importEvents(@PathVariable int year, @RequestParam("file") MultipartFile file) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        try (var stream = file.getInputStream()) {
            var errors = this.importerUseCase.importEvents(signedInUser, year, stream).stream()
                .map(ImportErrorRepresentation::fromDomain)
                .toList();
            return ResponseEntity.ok(errors);
        } catch (HandledException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to import events", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/users")
    public ResponseEntity<Void> importUsers(@RequestParam("file") MultipartFile file) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        try (var stream = file.getInputStream()) {
            this.importerUseCase.importUsers(signedInUser, stream);
            return ResponseEntity.ok().build();
        } catch (HandledException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to import users", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
