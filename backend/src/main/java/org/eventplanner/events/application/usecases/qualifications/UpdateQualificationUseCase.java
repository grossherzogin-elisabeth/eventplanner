package org.eventplanner.events.application.usecases.qualifications;

import static org.eventplanner.common.validation.ValidationUtils.convertToLogAttribute;

import java.util.NoSuchElementException;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.specs.UpdateQualificationSpec;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateQualificationUseCase {

    private final QualificationRepository qualificationRepository;
    private final Validator validator;

    @PreAuthorize("hasAuthority('qualifications:update')")
    @Transactional
    public @NonNull Qualification updateQualification(
        @NonNull final QualificationKey qualificationKey,
        @NonNull final UpdateQualificationSpec spec
    ) throws ConstraintViolationException {
        var qualification = qualificationRepository.findByKey(qualificationKey)
            .orElseThrow(() -> new NoSuchElementException("Qualification not found"));
        log.info("Updating qualification {}", qualificationKey);
        qualification = spec.apply(qualification);
        validate(qualification);
        qualificationRepository.update(qualification);
        return qualification;
    }

    @PreAuthorize("hasAuthority('qualifications:update')")
    @Transactional
    public @NonNull Qualification patchQualification(
        @NonNull final QualificationKey qualificationKey,
        @NonNull final UpdateQualificationSpec spec
    ) throws ConstraintViolationException {
        var qualification = qualificationRepository.findByKey(qualificationKey)
            .orElseThrow(() -> new NoSuchElementException("Qualification not found"));
        log.info("Patching qualification {}", qualificationKey);
        qualification = spec.patch(qualification);
        validate(qualification);
        qualificationRepository.update(qualification);
        return qualification;
    }

    private void validate(@NonNull final Qualification qualification) throws ConstraintViolationException {
        var violations = validator.validate(qualification);
        if (!violations.isEmpty()) {
            log.atWarn()
                .addKeyValue("violations", convertToLogAttribute(violations))
                .log("Rejected qualification update because of validation errors");
            throw new ConstraintViolationException(violations);
        }
    }
}
