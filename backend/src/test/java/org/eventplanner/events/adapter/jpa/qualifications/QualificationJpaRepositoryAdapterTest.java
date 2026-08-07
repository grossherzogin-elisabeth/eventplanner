package org.eventplanner.events.adapter.jpa.qualifications;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.QualificationFactory.createQualification;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QualificationJpaRepositoryAdapterTest {

    private QualificationJpaRepository repository;
    private QualificationJpaRepositoryAdapter testee;

    @BeforeEach
    void setup() {
        repository = mock();
        testee = new QualificationJpaRepositoryAdapter(repository);
    }

    @Test
    void shouldThrowWhenCreatingQualificationThatAlreadyExists() {
        var qualification = createQualification();
        when(repository.existsById(qualification.getKey().value())).thenReturn(true);

        assertThatThrownBy(() -> testee.create(qualification))
            .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any(QualificationJpaEntity.class));
    }

    @Test
    void shouldThrowWhenUpdatingQualificationThatDoesNotExist() {
        var qualification = createQualification();
        when(repository.existsById(qualification.getKey().value())).thenReturn(false);

        assertThatThrownBy(() -> testee.update(qualification))
            .isInstanceOf(NoSuchElementException.class);
        verify(repository, never()).save(any(QualificationJpaEntity.class));
    }

    @Test
    void shouldThrowWhenDeletingQualificationThatDoesNotExist() {
        var key = new QualificationKey("qualification-1");
        when(repository.existsById(key.value())).thenReturn(false);

        assertThatThrownBy(() -> testee.deleteByKey(key))
            .isInstanceOf(NoSuchElementException.class);
        verify(repository, never()).deleteById(key.value());
    }
}
