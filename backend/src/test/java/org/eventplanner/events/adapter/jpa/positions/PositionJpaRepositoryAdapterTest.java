package org.eventplanner.events.adapter.jpa.positions;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.PositionFactory.generateDefaultPositions;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.eventplanner.events.domain.values.positions.PositionKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PositionJpaRepositoryAdapterTest {

    private PositionJpaRepository repository;
    private PositionJpaRepositoryAdapter testee;

    @BeforeEach
    void setup() {
        repository = mock();
        testee = new PositionJpaRepositoryAdapter(repository);
    }

    @Test
    void shouldThrowWhenCreatingPositionThatAlreadyExists() {
        var position = generateDefaultPositions().get(0);
        when(repository.existsById(position.getKey().value())).thenReturn(true);

        assertThatThrownBy(() -> testee.create(position))
            .isInstanceOf(IllegalArgumentException.class);
        verify(repository, never()).save(any(PositionJpaEntity.class));
    }

    @Test
    void shouldThrowWhenUpdatingPositionThatDoesNotExist() {
        var position = generateDefaultPositions().get(0);
        when(repository.existsById(position.getKey().value())).thenReturn(false);

        assertThatThrownBy(() -> testee.update(position))
            .isInstanceOf(NoSuchElementException.class);
        verify(repository, never()).save(any(PositionJpaEntity.class));
    }

    @Test
    void shouldThrowWhenDeletingPositionThatDoesNotExist() {
        var key = new PositionKey("position-1");
        when(repository.existsById(key.value())).thenReturn(false);

        assertThatThrownBy(() -> testee.deleteByKey(key))
            .isInstanceOf(NoSuchElementException.class);
        verify(repository, never()).deleteById(key.value());
    }
}
