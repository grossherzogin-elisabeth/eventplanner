package org.eventplanner.events.application.usecases.qualifications;

import java.util.List;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReadQualificationUseCase {

    private final QualificationRepository qualificationRepository;

    @PreAuthorize("hasAuthority('qualifications:list')")
    public @NonNull List<Qualification> getQualifications() {
        log.debug("Reading qualifications");
        return this.qualificationRepository.findAll();
    }
}
