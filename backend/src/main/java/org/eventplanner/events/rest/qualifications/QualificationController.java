package org.eventplanner.events.rest.qualifications;

import java.util.List;

import org.eventplanner.events.application.usecases.qualifications.CreateQualificationUseCase;
import org.eventplanner.events.application.usecases.qualifications.DeleteQualificationUseCase;
import org.eventplanner.events.application.usecases.qualifications.ReadQualificationUseCase;
import org.eventplanner.events.application.usecases.qualifications.UpdateQualificationUseCase;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.eventplanner.events.rest.qualifications.dto.CreateQualificationRequest;
import org.eventplanner.events.rest.qualifications.dto.QualificationRepresentation;
import org.eventplanner.events.rest.qualifications.dto.UpdateQualificationRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/qualifications")
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class QualificationController {

    private final CreateQualificationUseCase createQualificationUseCase;
    private final ReadQualificationUseCase readQualificationUseCase;
    private final UpdateQualificationUseCase updateQualificationUseCase;
    private final DeleteQualificationUseCase deleteQualificationUseCase;

    @PostMapping("")
    public @NonNull ResponseEntity<QualificationRepresentation> createQualification(
        @NonNull @Valid @RequestBody CreateQualificationRequest spec
    ) {
        var qualificationSpec = spec.toDomain();
        var qualification = createQualificationUseCase.createQualification(qualificationSpec);
        return ResponseEntity.status(HttpStatus.CREATED).body(QualificationRepresentation.fromDomain(qualification));
    }

    @GetMapping("")
    public @NonNull ResponseEntity<List<QualificationRepresentation>> getQualifications() {
        var qualifications = readQualificationUseCase.getQualifications().stream()
            .map(QualificationRepresentation::fromDomain)
            .toList();
        return ResponseEntity.ok(qualifications);
    }

    @PutMapping("/{qualificationKey}")
    public @NonNull ResponseEntity<QualificationRepresentation> updateQualification(
        @NonNull @PathVariable String qualificationKey,
        @NonNull @Valid @RequestBody UpdateQualificationRequest spec
    ) {
        var key = new QualificationKey(qualificationKey);
        var qualificationSpec = spec.toDomain();
        var qualification = updateQualificationUseCase.updateQualification(key, qualificationSpec);
        return ResponseEntity.ok(QualificationRepresentation.fromDomain(qualification));
    }

    @PatchMapping("/{qualificationKey}")
    public @NonNull ResponseEntity<QualificationRepresentation> patchQualification(
        @NonNull @PathVariable String qualificationKey,
        @NonNull @Valid @RequestBody UpdateQualificationRequest spec
    ) {
        var key = new QualificationKey(qualificationKey);
        var qualificationSpec = spec.toDomain();
        var qualification = updateQualificationUseCase.patchQualification(key, qualificationSpec);
        return ResponseEntity.ok(QualificationRepresentation.fromDomain(qualification));
    }

    @DeleteMapping("/{qualificationKey}")
    public @NonNull ResponseEntity<Void> deleteQualification(
        @NonNull @PathVariable String qualificationKey
    ) {
        deleteQualificationUseCase.deleteQualification(new QualificationKey(qualificationKey));
        return ResponseEntity.noContent().build();
    }
}
