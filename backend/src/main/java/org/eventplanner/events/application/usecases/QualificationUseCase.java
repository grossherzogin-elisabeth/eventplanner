package org.eventplanner.events.application.usecases;

import java.util.List;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class QualificationUseCase {

    private final QualificationRepository qualificationRepository;

    @PreAuthorize("hasAuthority('qualifications:read')")
    public @NonNull List<Qualification> getQualifications() {
        log.debug("Reading qualifications");
        return this.qualificationRepository.findAll();
    }

    @PreAuthorize("hasAuthority('qualifications:write')")
    public @NonNull Qualification createQualification(
        @NonNull final Qualification qualification
    ) {
        qualification.setKey(new QualificationKey());
        log.info("Creating qualification {}", qualification.getKey());
        qualificationRepository.create(qualification);
        return qualification;
    }

    @PreAuthorize("hasAuthority('qualifications:write')")
    public @NonNull Qualification updateQualification(
        @NonNull final QualificationKey qualificationKey,
        @NonNull final Qualification qualification
    ) {
        if (!qualificationKey.equals(qualification.getKey())) {
            throw new IllegalArgumentException("Keys cannot be changed");
        }

        log.info("Updating qualification {}", qualificationKey);
        qualificationRepository.update(qualification);
        return qualification;
    }

    @PreAuthorize("hasAuthority('qualifications:write')")
    public void deleteQualification(
        @NonNull final QualificationKey qualificationKey
    ) {
        log.info("Deleting qualification {}", qualificationKey);
        // TODO remove qualification from all users
        // TODO might want soft delete here
        qualificationRepository.deleteByKey(qualificationKey);
    }
}
