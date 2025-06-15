package org.eventplanner.events.application.usecases;

import java.util.ArrayList;

import org.eventplanner.events.application.ports.PositionRepository;
import org.eventplanner.events.application.ports.QualificationRepository;
import org.eventplanner.events.application.services.NotificationService;
import org.eventplanner.events.application.services.UserService;
import org.eventplanner.events.domain.entities.users.UserQualification;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserQualificationExpirationUseCase {

    private final UserService userService;
    private final NotificationService notificationService;
    private final QualificationRepository qualificationRepository;
    private final PositionRepository positionRepository;

    public void sendExpirationNotifications() {
        log.info("Sending user qualification expiration notifications");
        var users = userService.getDetailedUsers();
        var qualifications = qualificationRepository.findAllAsMap();
        var positions = positionRepository.findAllAsMap();
        for (var user : users) {
            var updatedQualifications = new ArrayList<UserQualification>();
            // send notifications for already expired qualifications
            user.getQualifications().stream()
                .filter(it -> !UserQualification.State.EXPIRED.equals(it.getState()))
                .filter(UserQualification::isExpired)
                .forEach(it -> {
                    var qualification = qualifications.get(it.getQualificationKey());
                    if (qualification != null) {
                        updatedQualifications.add(it);
                        it.setState(UserQualification.State.EXPIRED);
                        notificationService.sendQualificationExpiredNotification(user, qualification, positions);
                    }
                });

            // send notifications for soon expiring qualifications
            user.getQualifications().stream()
                .filter(it -> UserQualification.State.VALID.equals(it.getState()))
                .filter(UserQualification::willExpireSoon)
                .forEach(it -> {
                    var qualification = qualifications.get(it.getQualificationKey());
                    if (qualification != null) {
                        updatedQualifications.add(it);
                        it.setState(UserQualification.State.WILL_EXPIRE_SOON);
                        notificationService.sendQualificationWillExpireSoonNotification(user, qualification, positions);
                    }
                });

            if (!updatedQualifications.isEmpty()) {
                userService.updateUser(user);
            }
        }
    }
}
