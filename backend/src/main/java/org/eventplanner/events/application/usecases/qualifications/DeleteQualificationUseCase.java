package org.eventplanner.events.application.usecases.qualifications;

import org.eventplanner.events.application.ports.QualificationRepository;
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

    @PreAuthorize("hasAuthority('qualifications:delete')")
    @Transactional
    public void deleteQualification(
        @NonNull final QualificationKey qualificationKey
    ) {
        log.info("Deleting qualification {}", qualificationKey);
        // TODO remove qualification from all users
        // TODO might want soft delete here
        qualificationRepository.deleteByKey(qualificationKey);
    }
}
