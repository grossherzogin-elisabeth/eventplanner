package org.eventplanner.events.adapter.jpa.positions;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.PositionFactory.generateDefaultPositions;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.eventplanner.config.RetryConfig;
import org.eventplanner.events.domain.entities.positions.Position;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
}
