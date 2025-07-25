package org.eventplanner.events.domain.values.notifications;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.lang.Nullable;

public enum NotificationType {

    // As a team member
    // I want to get notified when I get added to a waiting list
    // So that I can keep track of the events I signed up for
    ADDED_TO_WAITING_LIST("added_to_waiting_list"),

    // As a team member
    // I want to get notified when I get removed from a waiting list
    // So that I can keep track of the events I signed up for
    REMOVED_FROM_WAITING_LIST("removed_from_waiting_list"),

    // As a team member
    // I want to get notified when I get add to an events team
    // So that I can keep track of the events I will participate in
    ADDED_TO_CREW("added_to_crew"),

    // As a team member
    // I want to get notified when I get removed from an events team
    // So that I can keep track of the events I will participate in
    REMOVED_FROM_CREW("removed_from_crew"),

    // As a team planner
    // I want to get a participation confirmation of every team member
    // So that I know early if I need to find a replacement
    CONFIRM_REGISTRATION_REQUEST("confirm_participation_request"),

    // As a team planner
    // I want to get a participation confirmation of every team member in case the user did not respond to the first
    // request
    // So that I know early if I need to find a replacement
    CONFIRM_REGISTRATION_REMINDER("confirm_participation_reminder"),

    // As a team planner
    // I want to get a notification when someone cancels an event signup
    // So that I know early if I need to find a replacement
    CREW_REGISTRATION_CANCELED("crew_registration_canceled"),

    // As a team planner
    // I want to get a notification when someone signs up on an event
    // So that I know I can add them to the crew
    CREW_REGISTRATION_ADDED("crew_registration_added"),

    // As a user manager
    // I want to get a notification when a user changes their personal data
    // So that I can keep the user data up to date
    USER_DATA_CHANGED("user_data_changed"),

    // As a user manager
    // I want to get a notification when a new user registers
    // So that I can assign the correct role to the user

    // As an event leader
    // I want to get a summary of all event related information before the event starts
    // So that I have all information I need and can plan accordingly

    // As a team member
    // I want to get notified when a qualifications gets assigned to my account
    // So that I always know which qualifications I have
    QUALIFICATION_ADDED("qualification_added"),

    // As a team member
    // I want to get notified when a qualifications gets updated on my account
    // So that I always know which qualifications I have
    QUALIFICATION_UPDATED("qualification_updated"),

    // As a team member
    // I want to get notified when a qualifications gets removed from my account
    // So that I always know which qualifications I have
    QUALIFICATION_REMOVED("qualification_removed"),

    // As a team member
    // I want to get notified when one of my qualifications is close to expire
    // So that I can renew it in time
    QUALIFICATION_CLOSE_TO_EXPIRED("qualification_close_to_expired"),

    // As a team member
    // I want to get notified when one of my qualifications expires
    // So that I can renew it in time
    QUALIFICATION_EXPIRED("qualification_expired");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public static Optional<NotificationType> fromString(@Nullable String value) {
        return Arrays.stream(values())
            .filter(state -> state.toString().equals(value))
            .findFirst();
    }

    @Override
    public String toString() {
        return value;
    }
}
