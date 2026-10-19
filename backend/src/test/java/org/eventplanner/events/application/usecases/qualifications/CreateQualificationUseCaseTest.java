package org.eventplanner.events.application.usecases.qualifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.PositionKeys.MATE;
import static org.eventplanner.testdata.PositionKeys.TRAINER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.eventplanner.beans.ValidatorFactory;
import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.domain.specs.CreateQualificationSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolationException;

class CreateQualificationUseCaseTest {

    private QualificationRepository qualificationRepository;
    private CreateQualificationUseCase testee;

    @BeforeEach
    void setup() {
        qualificationRepository = mock();
        testee = new CreateQualificationUseCase(qualificationRepository, ValidatorFactory.defaultValidator());
    }

    @Test
    void shouldCreateNewQualificationKey() {
        var created = testee.createQualification(new CreateQualificationSpec(
            "New Qualification",
            "icon.png",
            "This is a new qualification",
            false,
            List.of(MATE, TRAINER)
        ));

        assertThat(created.getKey()).isNotNull();
        verify(qualificationRepository).create(any());
    }

    @Test
    void shouldValidateQualificationBeforeCreate() {
        var spec = new CreateQualificationSpec(
            "\t", // invalid because name may not be blank
            null, // invalid because icon may not be null
            "This is an invalid new qualification",
            null,
            null
        );

        assertThatThrownBy(() -> testee.createQualification(spec))
            .isInstanceOf(ConstraintViolationException.class);

        verify(qualificationRepository, never()).update(any());
    }
}
