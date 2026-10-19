package org.eventplanner.events.application.usecases.qualifications;

import static org.eventplanner.common.validation.ValidationUtils.convertToLogAttribute;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.specs.CreateQualificationSpec;
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
public class CreateQualificationUseCase {

    private final QualificationRepository qualificationRepository;
    private final Validator validator;

    @PreAuthorize("hasAuthority('qualifications:create')")
    @Transactional
    public @NonNull Qualification createQualification(
        @NonNull final CreateQualificationSpec spec
    ) {
        var qualification = spec.toQualification();
        log.info("Creating qualification {}", qualification.getKey());
        validate(qualification);
        qualificationRepository.create(qualification);
        return qualification;
    }

    private void validate(@NonNull final Qualification qualification) throws ConstraintViolationException {
        var violations = validator.validate(qualification);
        if (!violations.isEmpty()) {
            log.atWarn()
                .addKeyValue("violations", convertToLogAttribute(violations))
                .log("Rejected qualification creation because of validation errors");
            throw new ConstraintViolationException(violations);
        }
    }
}
