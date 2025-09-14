package org.eventplanner.events.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.eventplanner.testdata.QualificationFactory.createQualification;
import static org.eventplanner.testdata.UserFactory.createUser;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.entities.users.UserQualification;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.eventplanner.testdata.PositionKeys;
import org.junit.jupiter.api.Test;

class UserDetailsTest {

    @Test
    void shouldBuildFullName() {
        var testee = createUser()
            .withTitle("Title")
            .withNickName(null)
            .withFirstName("Firstname")
            .withSecondName("Second Name")
            .withLastName("Lastname");
        assertThat(testee.getFullName()).isEqualTo("Title Firstname Second Name Lastname");
    }

    @Test
    void shouldReplaceFirstNameWithNickname() {
        var testee = createUser()
            .withTitle(null)
            .withNickName("Nickname")
            .withFirstName("Firstname")
            .withSecondName(null)
            .withLastName("Lastname");
        assertThat(testee.getFullName()).isEqualTo("Nickname Lastname");
    }

    @Test
    void shouldAddPosition() {
        var testee = createUser();
        testee.addPosition(PositionKeys.CAPTAIN);
        assertThat(testee.getPositions()).containsExactly(PositionKeys.CAPTAIN);
    }

    @Test
    void shouldNotAddDuplicatePositions() {
        var testee = createUser().withPositions(List.of(PositionKeys.CAPTAIN));
        testee.addPosition(PositionKeys.CAPTAIN);
        assertThat(testee.getPositions()).containsExactly(PositionKeys.CAPTAIN);
    }

    @Test
    void shouldAddPositionsFromQualification() {
        var qualification = new Qualification(
            new QualificationKey("key"),
            "Qualification",
            null,
            null,
            true,
            List.of(PositionKeys.MATE, PositionKeys.CAPTAIN)
        );
        var testee = createUser().withPositions(List.of(PositionKeys.CAPTAIN));

        testee.addQualification(qualification);

        assertThat(testee.getPositions()).containsExactly(PositionKeys.CAPTAIN, PositionKeys.MATE);
    }

    @Test
    void shouldAddExpiredQualification() {
        var qualification = new Qualification(
            new QualificationKey("key"),
            "Qualification",
            null,
            null,
            true,
            Collections.emptyList()
        );
        var testee = createUser();

        testee.addQualification(qualification);

        assertThat(testee.getQualifications()).containsExactly(new UserQualification(
            qualification.getKey(),
            null,
            true,
            UserQualification.State.EXPIRED
        ));
    }

    @Test
    void shouldAddValidQualification() {
        var qualification = new Qualification(
            new QualificationKey("key"),
            "Qualification",
            null,
            null,
            true,
            Collections.emptyList()
        );
        var testee = createUser();

        var expires = Instant.now().plusSeconds(1000);
        testee.addQualification(qualification, expires);

        assertThat(testee.getQualifications()).containsExactly(new UserQualification(
            qualification.getKey(),
            expires,
            true,
            UserQualification.State.VALID
        ));
    }

    @Test
    void shouldNotAddDuplicateQualifications() {
        var qualification = createQualification();
        var testee = createUser();
        testee.addQualification(qualification);
        var thrown = catchThrowable(() -> testee.addQualification(qualification));
        assertThat(thrown).isNotNull().isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldRemoveQualification() {
        var qualification = createQualification();
        var testee = createUser();
        testee.addQualification(qualification);
        testee.removeQualification(qualification.getKey());
        assertThat(testee.getQualifications()).isEmpty();
    }

    @Test
    void shouldReturnCorrectQualification() {
        var qualificationA = createQualification();
        var qualificationB = createQualification();
        var testee = createUser();
        testee.addQualification(qualificationA);
        testee.addQualification(qualificationB);
        var result = testee.getQualification(qualificationA.getKey());
        assertThat(result).isPresent();
        assertThat(result.get().getQualificationKey()).isEqualTo(qualificationA.getKey());
    }
}
