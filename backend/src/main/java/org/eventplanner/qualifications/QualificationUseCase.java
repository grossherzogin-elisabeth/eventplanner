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

        qualificationRepository.create(qualification);
        return qualification;
    }

    public Qualification updateQualification(@NonNull SignedInUser signedInUser, QualificationKey qualificationKey, Qualification qualification) {
        signedInUser.assertHasPermission(Permission.WRITE_QUALIFICATIONS);

        qualificationRepository.update(qualification);
        return qualification;
    }

    public void deleteQualification(@NonNull SignedInUser signedInUser, QualificationKey qualificationKey) {
        signedInUser.assertHasPermission(Permission.WRITE_QUALIFICATIONS);

        qualificationRepository.deleteByKey(qualificationKey);
    }
}
