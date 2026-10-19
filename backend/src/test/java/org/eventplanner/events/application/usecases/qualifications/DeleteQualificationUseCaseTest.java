package org.eventplanner.events.application.usecases.qualifications;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeleteQualificationUseCaseTest {

    private QualificationRepository qualificationRepository;
    private DeleteQualificationUseCase testee;

    @BeforeEach
    void setup() {
        qualificationRepository = mock();
        testee = new DeleteQualificationUseCase(qualificationRepository);
    }

    @Test
    void shouldDeleteQualificationByKey() {
        var key = new QualificationKey();

        testee.deleteQualification(key);

        verify(qualificationRepository).deleteByKey(key);
    }
}
