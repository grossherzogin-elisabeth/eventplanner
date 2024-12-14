package org.eventplanner.qualifications.rest;

import java.util.List;

import org.eventplanner.qualifications.QualificationUseCase;
import org.eventplanner.qualifications.rest.dto.CreateQualificationRequest;
import org.eventplanner.qualifications.rest.dto.QualificationRepresentation;
import org.eventplanner.qualifications.rest.dto.UpdateQualificationRequest;
import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.users.UserUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/qualifications")
@EnableMethodSecurity(securedEnabled = true)
public class QualificationController {

    private final UserUseCase userUseCase;
    private final QualificationUseCase qualificationUseCase;

    public QualificationController(
        @Autowired UserUseCase userUseCase,
        @Autowired QualificationUseCase qualificationUseCase
    ) {
        this.userUseCase = userUseCase;
        this.qualificationUseCase = qualificationUseCase;
    }

    @PostMapping(path = "")
    public ResponseEntity<QualificationRepresentation> createQualification(
        @RequestBody CreateQualificationRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        var qualificationSpec = spec.toDomain();
        var qualification = qualificationUseCase.createQualification(signedInUser, qualificationSpec);
        return ResponseEntity.status(HttpStatus.CREATED).body(QualificationRepresentation.fromDomain(qualification));
    }

    @GetMapping(path = "")
    public ResponseEntity<List<QualificationRepresentation>> getQualifications() {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        var qualifications = qualificationUseCase.getQualifications(signedInUser).stream()
            .map(QualificationRepresentation::fromDomain)
            .toList();
        return ResponseEntity.ok(qualifications);
    }

    @PutMapping(path = "/{qualificationKey}")
    public ResponseEntity<QualificationRepresentation> updateQualification(
        @PathVariable String qualificationKey,
        @RequestBody UpdateQualificationRequest spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        var qualificationSpec = spec.toDomain(qualificationKey);
        var qualification =
            qualificationUseCase.updateQualification(signedInUser, qualificationSpec.getKey(), qualificationSpec);
        return ResponseEntity.ok(QualificationRepresentation.fromDomain(qualification));
    }

    @DeleteMapping(path = "/{qualificationKey}")
    public ResponseEntity<Void> deleteQualification(@PathVariable String qualificationKey) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        qualificationUseCase.deleteQualification(signedInUser, new QualificationKey(qualificationKey));
        return ResponseEntity.ok().build();
    }
}
