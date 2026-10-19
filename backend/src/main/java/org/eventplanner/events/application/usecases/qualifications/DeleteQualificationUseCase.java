package org.eventplanner.events.application.usecases.qualifications;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeleteQualificationUseCase {

    private final QualificationRepository qualificationRepository;
    private final UserService userService;

    @PreAuthorize("hasAuthority('qualifications:delete')")
    @Transactional
    public void deleteQualification(
        @NonNull final QualificationKey qualificationKey
    ) {
        log.debug("Checking if qualification is assigned to any user");
        var users = userService.getDetailedUsers();
        for (var user : users) {
            if (user.findQualification(qualificationKey).isPresent()) {
                log.info("Unassigning qualification {} from user {}", qualificationKey, user.getKey());
                user.removeQualification(qualificationKey);
                userService.updateUser(user);
            }
        }
        log.info("Deleting qualification {}", qualificationKey);
        qualificationRepository.deleteByKey(qualificationKey);
    }
}
