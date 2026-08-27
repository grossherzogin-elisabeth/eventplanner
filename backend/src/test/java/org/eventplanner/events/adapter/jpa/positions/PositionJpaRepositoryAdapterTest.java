package org.eventplanner.events.adapter.jpa.positions;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.PositionFactory.generateDefaultPositions;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.eventplanner.config.RetryConfig;
import org.eventplanner.events.domain.entities.positions.Position;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = { PositionJpaRepositoryAdapter.class, RetryConfig.class })
@ActiveProfiles(profiles = { "test" })
class PositionJpaRepositoryAdapterTest {

    private static final Position POSITION = generateDefaultPositions().getFirst();

    @Autowired
    private PositionJpaRepositoryAdapter testee;

    @MockitoBean
    private PositionJpaRepository repository;

    @Test
    void shouldThrowWhenCreatingPositionThatAlreadyExists() {
        when(repository.existsById(any())).thenReturn(true);

        assertThatThrownBy(() -> testee.create(POSITION))
            .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any(PositionJpaEntity.class));
    }

    @Test
    void shouldThrowWhenUpdatingPositionThatDoesNotExist() {
        when(repository.existsById(any())).thenReturn(false);

        assertThatThrownBy(() -> testee.update(POSITION))
            .isInstanceOf(NoSuchElementException.class);
        verify(repository, never()).save(any(PositionJpaEntity.class));
    }

    @Test
    void shouldThrowWhenDeletingPositionThatDoesNotExist() {
        when(repository.existsById(any())).thenReturn(false);

        assertThatThrownBy(() -> testee.deleteByKey(POSITION.getKey()))
            .isInstanceOf(NoSuchElementException.class);
        verify(repository, never()).deleteById(POSITION.getKey().value());
    }

    @Test
    void shouldRetryCreate() {
        when(repository.existsById(any())).thenReturn(false);
        when(repository.save(any()))
            .thenThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .thenThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .thenReturn(mock(PositionJpaEntity.class));

        testee.create(POSITION);

        verify(repository, times(3)).save(any());
    }

    @Test
    void shouldRetryUpdate() {
        when(repository.existsById(any())).thenReturn(true);
        when(repository.save(any()))
            .thenThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .thenThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .thenReturn(mock(PositionJpaEntity.class));

        testee.update(POSITION);

        verify(repository, times(3)).save(any());
    }

    @Test
    void shouldRetryDelete() {
        when(repository.existsById(any())).thenReturn(true);
        doThrow(new CannotAcquireLockException("mocked 1st attempt"))
            .doThrow(new CannotAcquireLockException("mocked 2nd attempt"))
            .doNothing()
            .when(repository).deleteById(any());

        testee.deleteByKey(POSITION.getKey());

        verify(repository, times(3)).deleteById(any());
    }
}
