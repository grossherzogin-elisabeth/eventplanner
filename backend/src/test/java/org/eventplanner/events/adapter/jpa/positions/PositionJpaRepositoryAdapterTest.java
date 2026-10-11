package org.eventplanner.events.adapter.jpa.positions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.PositionFactory.generateDefaultPositions;

import java.util.NoSuchElementException;

import org.eventplanner.events.domain.entities.positions.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PositionJpaRepositoryAdapter.class)
@ActiveProfiles(profiles = { "test" })
class PositionJpaRepositoryAdapterTest {

    private static final Position POSITION = generateDefaultPositions().getFirst();

    @Autowired
    private PositionJpaRepositoryAdapter testee;

    @Autowired
    private PositionJpaRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    void shouldThrowWhenCreatingPositionThatAlreadyExists() {
        repository.save(PositionJpaEntity.fromDomain(POSITION));

        assertThatThrownBy(() -> testee.create(POSITION))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowWhenUpdatingPositionThatDoesNotExist() {
        assertThatThrownBy(() -> testee.update(POSITION))
            .isInstanceOf(NoSuchElementException.class);
        
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void shouldThrowWhenDeletingPositionThatDoesNotExist() {
        assertThatThrownBy(() -> testee.deleteByKey(POSITION.getKey()))
            .isInstanceOf(NoSuchElementException.class);
    }
}
