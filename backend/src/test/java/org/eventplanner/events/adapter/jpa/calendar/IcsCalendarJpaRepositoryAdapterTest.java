package org.eventplanner.events.adapter.jpa.calendar;

import org.eventplanner.events.domain.entities.calendar.IcsCalendarInfo;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class IcsCalendarJpaRepositoryAdapterTest {

    @Autowired
    private IcsCalendarJpaRepository repository;

    @Test
    public void testCreate_validUserKey_createsAndReturnsIcsCalendarInfo() {
        // Arrange
        IcsCalendarJpaRepositoryAdapter adapter = new IcsCalendarJpaRepositoryAdapter(repository);
        UserKey userKey = new UserKey("testUser");

        // Act
        IcsCalendarInfo result = adapter.create(userKey);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getKey()).isNotNull().isNotEmpty();
        assertThat(result.getToken()).isNotNull().isNotEmpty();
        assertThat(result.getUser()).isEqualTo(userKey);

        // Verify the entity in the database
        var savedEntity = repository.findById(result.getKey());
        assertThat(savedEntity).isPresent();
        assertThat(savedEntity.get().getKey()).isEqualTo(result.getKey());
        assertThat(savedEntity.get().getToken()).isEqualTo(result.getToken());
        assertThat(savedEntity.get().getUserKey()).isEqualTo(result.getUser().toString());
    }
}