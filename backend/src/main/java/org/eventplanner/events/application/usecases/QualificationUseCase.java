package org.eventplanner.events.application.usecases;

import java.util.List;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class QualificationUseCase {

    private final QualificationRepository qualificationRepository;

    public @NonNull List<Qualification> getQualifications(@NonNull final SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_QUALIFICATIONS);

        return this.qualificationRepository.findAll();
    }

    public @NonNull Qualification createQualification(
        @NonNull final SignedInUser signedInUser,
        @NonNull final Qualification qualification
    ) {
        signedInUser.assertHasPermission(Permission.WRITE_QUALIFICATIONS);

        qualification.setKey(new QualificationKey());
        log.info("Creating qualification {}", qualification.getKey());
        qualificationRepository.create(qualification);
        return qualification;
    }

    public @NonNull Qualification updateQualification(
        @NonNull final SignedInUser signedInUser,
        @NonNull final QualificationKey qualificationKey,
        @NonNull final Qualification qualification
    ) {
        signedInUser.assertHasPermission(Permission.WRITE_QUALIFICATIONS);

        if (!qualificationKey.equals(qualification.getKey())) {
            throw new IllegalArgumentException("Keys cannot be changed");
        }

        log.info("Updating qualification {}", qualificationKey);
        qualificationRepository.update(qualification);
        return qualification;
    }

    public void deleteQualification(
        @NonNull final SignedInUser signedInUser,
        @NonNull final QualificationKey qualificationKey
    ) {
        signedInUser.assertHasPermission(Permission.WRITE_QUALIFICATIONS);

        log.info("Deleting qualification {}", qualificationKey);
        // TODO remove qualification from all users
        // TODO might want soft delete here
        qualificationRepository.deleteByKey(qualificationKey);
    }
}
