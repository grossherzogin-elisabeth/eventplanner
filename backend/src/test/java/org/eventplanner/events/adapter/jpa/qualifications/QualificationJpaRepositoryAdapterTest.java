package org.eventplanner.events.adapter.jpa.qualifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eventplanner.testdata.QualificationFactory.createQualification;

import java.util.NoSuchElementException;

import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QualificationJpaRepositoryAdapter.class)
@ActiveProfiles(profiles = { "test" })
class QualificationJpaRepositoryAdapterTest {

    @Autowired
    private QualificationJpaRepositoryAdapter testee;

    @Autowired
    private QualificationJpaRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    void shouldThrowWhenCreatingQualificationThatAlreadyExists() {
        var qualification = createQualification().withName("Existing");
        repository.save(QualificationJpaEntity.fromDomain(qualification));

        assertThatThrownBy(() -> testee.create(qualification.withName("Other")))
            .isInstanceOf(IllegalArgumentException.class);

        var saved = repository.findAll();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getName()).isEqualTo("Existing");
    }

    @Test
    void shouldThrowWhenUpdatingQualificationThatDoesNotExist() {
        var qualification = createQualification();

        assertThatThrownBy(() -> testee.update(qualification))
            .isInstanceOf(NoSuchElementException.class);

        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void shouldThrowWhenDeletingQualificationThatDoesNotExist() {
        var key = new QualificationKey("qualification-1");

        assertThatThrownBy(() -> testee.deleteByKey(key))
            .isInstanceOf(NoSuchElementException.class);
    }
}
