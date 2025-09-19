package org.eventplanner.events.adapter.jpa.calendar;

import org.eventplanner.events.domain.entities.calendar.IcsCalendarInfo;
import org.eventplanner.events.domain.values.users.UserKey;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import(IcsCalendarJpaRepositoryAdapter.class)
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:sqlite::memory:?cache=shared",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class IcsCalendarJpaRepositoryAdapterTest {

    private static final Logger log = LoggerFactory.getLogger(IcsCalendarJpaRepositoryAdapterTest.class);
    @Autowired
    private IcsCalendarJpaRepository repository;

    @Autowired
    private IcsCalendarJpaRepositoryAdapter adapter;

    @Test
    public void testCreate_validUserKey_createsAndReturnsIcsCalendarInfo() {

        // Arrange
        UserKey userKey = new UserKey();

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