package org.eventplanner.events.application.usecases.qualifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.QualificationFactory.createQualification;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eventplanner.beans.ValidatorFactory;
import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.specs.UpdateQualificationSpec;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.eventplanner.testdata.PositionKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolationException;

class UpdateQualificationUseCaseTest {

    private UpdateQualificationUseCase testee;
    private QualificationRepository qualificationRepository;
    private Qualification qualification;
    private QualificationKey key;

    @BeforeEach
    void setup() {
        qualification = createQualification().withGrantsPositions(List.of(PositionKeys.MATE));
        key = qualification.getKey();
        qualificationRepository = mock();
        when(qualificationRepository.findByKey(key)).thenReturn(Optional.of(qualification));
        testee = new UpdateQualificationUseCase(qualificationRepository, ValidatorFactory.defaultValidator());
    }

    @Test
    void shouldUpdateAllAttribute() {
        var spec = new UpdateQualificationSpec(
            "Updated name",
            "updated-icon",
            "Updated description",
            null,
            null
        );
        var expected = qualification.withName(spec.name())
            .withIcon(spec.icon())
            .withDescription(spec.description())
            .withExpires(false)
            .withGrantsPositions(Collections.emptyList());

        var updated = testee.updateQualification(key, spec);

        assertThat(updated).isEqualTo(expected);
        verify(qualificationRepository).update(updated);
    }

    @Test
    void shouldValidateAfterUpdate() {
        var spec = new UpdateQualificationSpec(
            "Updated name",
            "updated-icon",
            null,
            null,
            null
        );

        assertThatThrownBy(() -> testee.updateQualification(key, spec))
            .isInstanceOf(ConstraintViolationException.class);

        verify(qualificationRepository, never()).update(any());
    }

    @Test
    void shouldPatchOnlyNonNullAttributes() {
        var spec = new UpdateQualificationSpec().withName("Updated name");
        var expected = qualification.withName(spec.name());

        var updated = testee.patchQualification(key, spec);

        assertThat(updated).isEqualTo(expected);
        verify(qualificationRepository).update(updated);
    }

    @Test
    void shouldValidateAfterPatch() {
        var spec = new UpdateQualificationSpec().withName(" ");

        assertThatThrownBy(() -> testee.patchQualification(key, spec))
            .isInstanceOf(ConstraintViolationException.class);

        verify(qualificationRepository, never()).update(any());
    }
}
