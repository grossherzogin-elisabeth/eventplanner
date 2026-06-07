package org.eventplanner.events.rest.qualifications;

import java.util.List;

import org.eventplanner.events.application.usecases.qualifications.QualificationUseCase;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.eventplanner.events.rest.qualifications.dto.CreateQualificationRequest;
import org.eventplanner.events.rest.qualifications.dto.QualificationRepresentation;
import org.eventplanner.events.rest.qualifications.dto.UpdateQualificationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

    private final QualificationUseCase qualificationUseCase;

    @PostMapping("")
    public ResponseEntity<QualificationRepresentation> createQualification(
        @Valid @RequestBody CreateQualificationRequest spec
    ) {
        var qualificationSpec = spec.toDomain();
        var qualification = qualificationUseCase.createQualification(qualificationSpec);
        return ResponseEntity.status(HttpStatus.CREATED).body(QualificationRepresentation.fromDomain(qualification));
    }

    @GetMapping("")
    public ResponseEntity<List<QualificationRepresentation>> getQualifications() {
        var qualifications = qualificationUseCase.getQualifications().stream()
            .map(QualificationRepresentation::fromDomain)
            .toList();
        return ResponseEntity.ok(qualifications);
    }

    @PutMapping("/{qualificationKey}")
    public ResponseEntity<QualificationRepresentation> updateQualification(
        @PathVariable String qualificationKey,
        @Valid @RequestBody UpdateQualificationRequest spec
    ) {
        var qualificationSpec = spec.toDomain(qualificationKey);
        var qualification =
            qualificationUseCase.updateQualification(qualificationSpec.getKey(), qualificationSpec);
        return ResponseEntity.ok(QualificationRepresentation.fromDomain(qualification));
    }

    @DeleteMapping("/{qualificationKey}")
    public ResponseEntity<Void> deleteQualification(@PathVariable String qualificationKey) {
        qualificationUseCase.deleteQualification(new QualificationKey(qualificationKey));
        return ResponseEntity.ok().build();
    }
}
