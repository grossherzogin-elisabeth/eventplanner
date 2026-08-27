package org.eventplanner.events.adapter.jpa.qualifications;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.QualificationFactory.createQualification;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.eventplanner.config.RetryConfig;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = { QualificationJpaRepositoryAdapter.class, RetryConfig.class })
@ActiveProfiles(profiles = { "test" })
class QualificationJpaRepositoryAdapterTest {

    @Autowired
    private QualificationJpaRepositoryAdapter testee;

    @MockitoBean
    private QualificationJpaRepository repository;

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

    @Test
    void shouldRetryCreate() {
        var qualification = createQualification();
        var entity = QualificationJpaEntity.fromDomain(qualification);
        when(repository.existsById(any())).thenReturn(false);
        when(repository.save(any()))
            .thenThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .thenThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .thenReturn(entity);

        testee.create(createQualification());

        verify(repository, times(3)).save(any());
    }

    @Test
    void shouldRetryUpdate() {
        var qualification = createQualification();
        var entity = QualificationJpaEntity.fromDomain(qualification);
        when(repository.existsById(any())).thenReturn(true);
        when(repository.save(any()))
            .thenThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .thenThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .thenReturn(entity);

        testee.update(createQualification());

        verify(repository, times(3)).save(any());
    }

    @Test
    void shouldRetryDelete() {
        var qualification = createQualification();
        when(repository.existsById(any())).thenReturn(true);
        doThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .doThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .doNothing()
            .when(repository).deleteById(any());

        testee.deleteByKey(qualification.getKey());

        verify(repository, times(3)).deleteById(any());
    }
}
