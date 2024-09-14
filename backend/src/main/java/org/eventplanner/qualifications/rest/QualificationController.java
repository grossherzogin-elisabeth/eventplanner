package org.eventplanner.qualifications.rest;

import org.eventplanner.qualifications.QualificationUseCase;
import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.rest.dto.QualificationRepresentation;
import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.users.UserUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @RequestMapping(method = RequestMethod.POST, path = "")
    public ResponseEntity<QualificationRepresentation> createQualification(@RequestBody QualificationRepresentation spec) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        var qualificationSpec = new Qualification(new QualificationKey(""), spec.name(), spec.icon(), spec.description(), spec.expires());
        var qualification = qualificationUseCase.createQualification(signedInUser, qualificationSpec);
        return ResponseEntity.status(HttpStatus.CREATED).body(QualificationRepresentation.fromDomain(qualification));
    }

    @RequestMapping(method = RequestMethod.GET, path = "")
    public ResponseEntity<List<QualificationRepresentation>> getQualifications() {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        var qualifications = qualificationUseCase.getQualifications(signedInUser).stream()
            .map(QualificationRepresentation::fromDomain)
            .toList();
        return ResponseEntity.ok(qualifications);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{qualificationKey}")
    public ResponseEntity<QualificationRepresentation> updateQualification(
        @PathVariable String qualificationKey,
        @RequestBody QualificationRepresentation spec
    ) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        var qualificationSpec = new Qualification(new QualificationKey(qualificationKey), spec.name(), spec.icon(), spec.description(), spec.expires());
        var qualification = qualificationUseCase.updateQualification(signedInUser, qualificationSpec.getKey(), qualificationSpec);
        return ResponseEntity.ok(QualificationRepresentation.fromDomain(qualification));
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{qualificationKey}")
    public ResponseEntity<Void> deleteQualification(@PathVariable String qualificationKey) {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());

        qualificationUseCase.deleteQualification(signedInUser, new QualificationKey(qualificationKey));
        return ResponseEntity.ok().build();
    }
}
