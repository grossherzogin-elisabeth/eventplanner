package org.eventplanner.qualifications;

import org.eventplanner.exceptions.NotImplementedException;
import org.eventplanner.qualifications.adapter.QualificationRepository;
import org.eventplanner.qualifications.entities.Qualification;
import org.eventplanner.qualifications.values.QualificationKey;
import org.eventplanner.users.entities.SignedInUser;
import org.eventplanner.users.values.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static org.eventplanner.common.ObjectUtils.applyNullable;

@Service
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

        qualification.setKey(new QualificationKey(UUID.randomUUID().toString()));
        qualificationRepository.create(qualification);
        return qualification;
    }

    public Qualification updateQualification(@NonNull SignedInUser signedInUser, QualificationKey qualificationKey, Qualification qualification) {
        signedInUser.assertHasPermission(Permission.WRITE_QUALIFICATIONS);

        if (!qualificationKey.equals(qualification.getKey())) {
            throw new IllegalArgumentException("Keys cannot be changed");
        }

        qualificationRepository.update(qualification);
        return qualification;
    }

    public void deleteQualification(@NonNull SignedInUser signedInUser, QualificationKey qualificationKey) {
        signedInUser.assertHasPermission(Permission.WRITE_QUALIFICATIONS);

        // TODO remove qualification from all users
        // TODO might want soft delete here
        qualificationRepository.deleteByKey(qualificationKey);
    }
}
