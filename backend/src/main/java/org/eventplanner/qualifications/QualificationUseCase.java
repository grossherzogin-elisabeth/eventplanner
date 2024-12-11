package org.eventplanner.qualifications;

import java.util.List;

import org.eventplanner.qualifications.adapter.QualificationRepository;
import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.values.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QualificationUseCase {

    private final QualificationRepository qualificationRepository;

    public QualificationUseCase(@Autowired QualificationRepository qualificationRepository) {
        this.qualificationRepository = qualificationRepository;
    }

    public List<Qualification> getQualifications(@NonNull SignedInUser signedInUser) {
        signedInUser.assertHasPermission(Permission.READ_QUALIFICATIONS);

        return this.qualificationRepository.findAll();
    }

    public Qualification createQualification(@NonNull SignedInUser signedInUser, Qualification qualification) {
        signedInUser.assertHasPermission(Permission.WRITE_QUALIFICATIONS);

        qualification.setKey(new QualificationKey());
        log.info("Creating qualification {}", qualification.getKey());
        qualificationRepository.create(qualification);
        return qualification;
    }

    public Qualification updateQualification(@NonNull SignedInUser signedInUser, QualificationKey qualificationKey, Qualification qualification) {
        signedInUser.assertHasPermission(Permission.WRITE_QUALIFICATIONS);

        if (!qualificationKey.equals(qualification.getKey())) {
            throw new IllegalArgumentException("Keys cannot be changed");
        }

        log.info("Updating qualification {}", qualificationKey);
        qualificationRepository.update(qualification);
        return qualification;
    }

    public void deleteQualification(@NonNull SignedInUser signedInUser, QualificationKey qualificationKey) {
        signedInUser.assertHasPermission(Permission.WRITE_QUALIFICATIONS);

        log.info("Deleting qualification {}", qualificationKey);
        // TODO remove qualification from all users
        // TODO might want soft delete here
        qualificationRepository.deleteByKey(qualificationKey);
    }
}
