package org.eventplanner.events.application.usecases.qualifications;

import static org.eventplanner.testdata.QualificationFactory.createQualification;
import static org.eventplanner.testdata.UserFactory.createUser;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeleteQualificationUseCaseTest {

    private QualificationRepository qualificationRepository;
    private UserService userService;
    private DeleteQualificationUseCase testee;

    @BeforeEach
    void setup() {
        qualificationRepository = mock();
        userService = mock();
        testee = new DeleteQualificationUseCase(qualificationRepository, userService);
        when(userService.getDetailedUsers()).thenReturn(Collections.emptyList());
    }

    @Test
    void shouldDeleteQualification() {
        var key = new QualificationKey();

        testee.deleteQualification(key);

        verify(qualificationRepository).deleteByKey(key);
    }

    @Test
    void shouldRemoveQualificationFromAllUsers() {
        var qualification = createQualification();
        var other = createQualification();
        var userWithThisQualification = createUserWithQualifications(qualification);
        var userWithThisAndOtherQualification = createUserWithQualifications(qualification, other);
        var userWithoutQualification = createUserWithQualifications();
        when(userService.getDetailedUsers())
            .thenReturn(List.of(
                userWithThisQualification,
                userWithThisAndOtherQualification,
                userWithoutQualification
            ));

        testee.deleteQualification(qualification.getKey());

        verify(userService).updateUser(argThat(user ->
            user.getKey().equals(userWithThisQualification.getKey())
                && user.findQualification(qualification.getKey()).isEmpty()));
        verify(userService).updateUser(argThat(user ->
            user.getKey().equals(userWithThisAndOtherQualification.getKey())
                && user.findQualification(qualification.getKey()).isEmpty()
                && user.findQualification(other.getKey()).isPresent()));
        verify(userService, never()).updateUser(argThat(user ->
            user.getKey().equals(userWithoutQualification.getKey())));
    }

    private UserDetails createUserWithQualifications(Qualification... qualifications) {
        var user = createUser();
        for (var qualification : qualifications) {
            user.addQualification(qualification);
        }
        return user;
    }
}
