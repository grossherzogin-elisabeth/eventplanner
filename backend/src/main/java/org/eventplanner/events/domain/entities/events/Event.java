package org.eventplanner.events.domain.entities.events;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eventplanner.events.domain.entities.users.SignedInUser;
import org.eventplanner.events.domain.values.auth.Permission;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.EventLocation;
import org.eventplanner.events.domain.values.events.EventSignupType;
import org.eventplanner.events.domain.values.events.EventState;
import org.eventplanner.events.domain.values.events.EventType;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@With
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Event {
    private @NonNull EventKey key = new EventKey();
    private @NonNull EventType type = EventType.OTHER;
    private @NonNull EventSignupType signupType = EventSignupType.ASSIGNMENT;
    private @NonNull String name = "";
    private @NonNull EventState state = EventState.DRAFT;
    private @NonNull String note = "";
    private @NonNull String description = "";
    private @NonNull Instant start = Instant.now();
    private @NonNull Instant end = Instant.now();
    private @NonNull List<EventLocation> locations = Collections.emptyList();
    private @NonNull List<EventSlot> slots = Collections.emptyList();
    private @NonNull List<Registration> registrations = Collections.emptyList();
    private @NonNull Integer confirmationsRequestsSent = 0;

    public @NonNull List<RegistrationKey> getAssignedRegistrationKeys() {
        return getSlots().stream()
            .map(EventSlot::getAssignedRegistration)
            .filter(Objects::nonNull)
            .toList();
    }

    public @NonNull List<Registration> getAssignedRegistrations() {
        var keys = getAssignedRegistrationKeys();
        return registrations.stream().filter(it -> keys.contains(it.getKey())).toList();
    }

    public @NonNull Optional<Registration> findRegistrationByKey(@Nullable final RegistrationKey key) {
        return registrations.stream().filter(it -> it.getKey().equals(key)).findFirst();
    }

    public @NonNull Optional<Registration> findRegistrationByUserKey(@NonNull final UserKey key) {
        return registrations.stream().filter(it -> key.equals(it.getUserKey())).findFirst();
    }

    public @NonNull Optional<Registration> findRegistrationByName(@NonNull final String name) {
        return registrations.stream().filter(it -> name.equals(it.getName())).findFirst();
    }

    public void addRegistration(@NonNull final Registration registration) {
        var temp = new LinkedList<>(registrations);
        temp.add(registration);
        registrations = temp.stream().toList();
    }

    public void removeRegistration(@NonNull final Registration registration) {
        var temp = new LinkedList<>(registrations);
        temp.removeIf(it -> it.getKey().equals(registration.getKey()));
        registrations = temp.stream().toList();

        var assignedSlot = findSlotByAssignedRegistrationKey(registration.getKey());
        if (assignedSlot.isPresent()) {
            assignedSlot.get().setAssignedRegistration(null);
            optimizeSlots();
        }
    }

    public @NonNull Optional<EventSlot> findSlotByAssignedRegistrationKey(
        @NonNull final RegistrationKey registrationKey
    ) {
        return slots.stream().filter(it -> registrationKey.equals(it.getAssignedRegistration())).findFirst();
    }

    public boolean isUpForConfirmationRequest() {
        return startsWithinNextDays(14);
    }

    public boolean isUpForConfirmationReminder() {
        return startsWithinNextDays(7);
    }

    public boolean startsWithinNextDays(int n) {
        return getStart().isBefore(ZonedDateTime.now().plusDays(n).toInstant());
    }

    public boolean isVisibleForUser(@NonNull final SignedInUser signedInUser) {
        return switch (state) {
            case DRAFT -> signedInUser.hasPermission(Permission.WRITE_EVENT_DETAILS);
            case CANCELED -> findRegistrationByUserKey(signedInUser.key()).isPresent();
            default -> true;
        };
    }

    public @NonNull Event clearConfidentialData(@NonNull final SignedInUser signedInUser) {
        if (!signedInUser.hasPermission(Permission.WRITE_EVENT_SLOTS)
            && List.of(EventState.DRAFT, EventState.OPEN_FOR_SIGNUP).contains(state)) {
            // clear assigned registrations on slots if crew is not published yet
            log.debug("Clearing assigned registrations on event {}", name);
            slots.forEach(slot -> slot.setAssignedRegistration(null));
        }
        if (!signedInUser.hasPermission(Permission.WRITE_REGISTRATIONS)) {
            // clear notes of all but the signed-in user
            log.debug("Clearing registration notes on event {}", name);
            registrations.stream()
                .filter(it -> it.getNote() != null)
                .filter(it -> !signedInUser.key().equals(it.getUserKey()))
                .forEach(it -> {
                    it.setNote(null);
                    it.setOvernightStay(null);
                    it.setArrival(null);
                    it.setAccessKey(null);
                    it.setConfirmedAt(null);
                });
        }
        return this;
    }

    public @NonNull Event removeInvalidSlotAssignments() {
        log.debug("Removing invalid slot assignments for event {}", name);
        var counter = 0;
        var validRegistrationKeys = registrations.stream().map(Registration::getKey).toList();
        for (EventSlot slot : slots) {
            if (slot.getAssignedRegistration() != null && !validRegistrationKeys.contains(slot.getAssignedRegistration())) {
                log.debug("Removing invalid slot assignment for registration {}", slot.getAssignedRegistration());
                slot.setAssignedRegistration(null);
                counter++;
            }
        }
        if (counter > 0) {
            log.info("Removed {} invalid slot assignments for event {}", counter, name);
        } else {
            log.debug("All slot assignments for event {} are valid", name);
        }
        return this;
    }

    /**
     * Reorders slots to make sure the higher ranked slots are filled first, making space for lower qualified
     * team members
     */
    public void optimizeSlots() {
        log.debug("Optimizing slot assignments for event {}", name);
        var counter = 0;
        for (int i = 0; i < slots.size(); i++) {
            var slot = slots.get(i);
            if (slot.getAssignedRegistration() != null) {
                continue;
            }
            // this slot is empty, try to move a lower prio slot up here
            for (int j = i; j < slots.size(); j++) {
                var otherSlot = slots.get(j);
                if (otherSlot.getAssignedRegistration() == null) {
                    continue;
                }
                var registration = findRegistrationByKey(otherSlot.getAssignedRegistration());
                if (registration.isPresent() && slot.getPositions().contains(registration.get().getPosition())) {
                    // the registration of a lower prio slot can also be assigned to this slot, move it up
                    slot.setAssignedRegistration(otherSlot.getAssignedRegistration());
                    otherSlot.setAssignedRegistration(null);
                    counter++;
                    break;
                }
            }
        }
        if (counter > 0) {
            log.info("Optimized slots by moving {} assignments on event {}", counter, name);
        }
    }
}