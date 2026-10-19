package org.eventplanner.events.application.usecases.qualifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.QualificationFactory.createQualification;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReadQualificationUseCaseTest {

    private QualificationRepository qualificationRepository;
    private ReadQualificationUseCase testee;

    @BeforeEach
    void setup() {
        qualificationRepository = mock(QualificationRepository.class);
        testee = new ReadQualificationUseCase(qualificationRepository);
    }

    @Test
    void shouldReturnQualifications() {
        var qualifications = List.of(createQualification(), createQualification());
        when(qualificationRepository.findAll()).thenReturn(qualifications);

        assertThat(testee.getQualifications()).containsExactlyElementsOf(qualifications);
    }

    @Test
    void shouldReturnEmptyListWhenNoQualificationsExist() {
        when(qualificationRepository.findAll()).thenReturn(Collections.emptyList());

        assertThat(testee.getQualifications()).isEmpty();
    }
}
