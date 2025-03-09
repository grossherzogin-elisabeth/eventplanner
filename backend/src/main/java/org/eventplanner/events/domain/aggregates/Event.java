package org.eventplanner.events.domain.aggregates;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eventplanner.events.domain.entities.events.EventDetails;
import org.eventplanner.events.domain.entities.events.EventSlot;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.EventState;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.eventplanner.events.domain.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.With;

@With
public record Event(
    @NonNull EventDetails details,
    @NonNull List<Registration> registrations
) {
    public @NonNull Optional<Registration> getRegistrationByKey(@Nullable RegistrationKey registrationKey) {
        if (registrationKey == null) {
            return Optional.empty();
        }
        return registrations().stream()
            .filter(r -> r.getKey().equals(registrationKey))
            .findAny();
    }

    public @NonNull Optional<Registration> getRegistrationByAccessKey(@Nullable String accessKey) {
        if (accessKey == null) {
            return Optional.empty();
        }
        return registrations().stream()
            .filter(r -> accessKey.equals(r.getAccessKey()))
            .findAny();
    }

    public @NonNull Optional<Registration> getRegistrationByUserKey(@Nullable UserKey userKey) {
        if (userKey == null) {
            return Optional.empty();
        }
        return registrations().stream()
            .filter(r -> userKey.equals(r.getUserKey()))
            .findAny();
    }

    public @NonNull List<Registration> getAssignedRegistrations() {
        var keys = getAssignedRegistrationKeys();
        return registrations.stream().filter(it -> keys.contains(it.getKey())).toList();
    }

    public @NonNull List<RegistrationKey> getAssignedRegistrationKeys() {
        var validRegistrationKeys = registrations.stream().map(Registration::getKey).toList();
        return details.getSlots().stream()
            .map(EventSlot::getAssignedRegistration)
            .filter(Objects::nonNull)
            .filter(validRegistrationKeys::contains)
            .toList();
    }

    public @NonNull Optional<EventSlot> getSlotByRegistrationKey(@NonNull RegistrationKey registrationKey) {
        return details.getSlots().stream()
            .filter(r -> registrationKey.equals(r.getAssignedRegistration()))
            .findAny();
    }

    public boolean isUpForFirstParticipationConfirmationRequest() {
        return details.getState().equals(EventState.PLANNED)
            && details.getParticipationConfirmationsRequestsSent() <= 0
            && startsWithinNextDays(14);
    }

    public boolean isUpForSecondParticipationConfirmationRequest() {
        return details.getState().equals(EventState.PLANNED)
            && details.getParticipationConfirmationsRequestsSent() <= 1
            && startsWithinNextDays(7);
    }

    public boolean startsWithinNextDays(int n) {
        return details.getStart().isBefore(ZonedDateTime.now().plusDays(n).toInstant())
            && details.getStart().isAfter(Instant.now());
    }
}
