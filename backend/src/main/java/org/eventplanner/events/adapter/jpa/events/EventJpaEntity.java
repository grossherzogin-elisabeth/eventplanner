package org.eventplanner.events.adapter.jpa.events;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.EventSlot;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.EventLocation;
import org.eventplanner.events.domain.values.events.EventSignupType;
import org.eventplanner.events.domain.values.events.EventState;
import org.eventplanner.events.domain.values.events.EventType;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "events")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class EventJpaEntity {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @Column(name = "key", nullable = false, updatable = false)
    private String key;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "signup_type", nullable = false)
    private String signupType;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "note")
    private String note;

    @Column(name = "description")
    private String description;

    @Column(name = "start", nullable = false)
    private String start;

    @Column(name = "end", nullable = false)
    private String end;

    @Column(name = "locations")
    private String locationsRaw;

    @Column(name = "slots")
    private String slotsRaw;

    @Column(name = "participation_confirmations_requests_sent")
    private Integer confirmationsRequestsSent;

    public static @NonNull EventJpaEntity fromDomain(@NonNull Event domain) {
        var eventJpaEntity = new EventJpaEntity();
        eventJpaEntity.setType(domain.getType().value());
        eventJpaEntity.setSignupType(domain.getSignupType().value());
        eventJpaEntity.setKey(domain.getKey().value());
        eventJpaEntity.setYear(domain.getStart().atZone(ZoneId.systemDefault()).getYear());
        eventJpaEntity.setName(domain.getName());
        eventJpaEntity.setState(domain.getState().value());
        eventJpaEntity.setNote(domain.getNote());
        eventJpaEntity.setDescription(domain.getDescription());
        eventJpaEntity.setStart(domain.getStart().toString());
        eventJpaEntity.setEnd(domain.getEnd().toString());
        eventJpaEntity.setLocationsRaw(serializeLocations(domain.getLocations()));
        eventJpaEntity.setSlotsRaw(serializeSlots(domain.getSlots()));
        eventJpaEntity.setConfirmationsRequestsSent(domain.getConfirmationsRequestsSent());
        return eventJpaEntity;
    }

    public static @NonNull List<EventLocation> deserializeLocations(@NonNull String json) {
        try {
            var entities = objectMapper.readValue(
                json, new TypeReference<List<LocationJsonEntity>>() {
                }
            );
            return entities.stream().map(LocationJsonEntity::toDomain).toList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static @NonNull String serializeLocations(@NonNull List<EventLocation> locations) {
        try {
            var entities = locations.stream().map(LocationJsonEntity::fromDomain).toList();
            return objectMapper.writeValueAsString(entities);
        } catch (IOException e) {
            return "[]";
        }
    }

    public static @NonNull List<EventSlot> deserializeSlots(@NonNull String json) {
        try {
            var entities = objectMapper.readValue(
                json, new TypeReference<List<SlotJsonEntity>>() {
                }
            );
            return entities.stream().map(SlotJsonEntity::toDomain).toList();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static @NonNull String serializeSlots(@NonNull List<EventSlot> slots) {
        try {
            var entities = slots.stream().map(SlotJsonEntity::fromDomain).toList();
            return objectMapper.writeValueAsString(entities);
        } catch (IOException e) {
            return "[]";
        }
    }

    public @NonNull EventType mapEventType() {
        var mapped = EventType.fromString(type);
        if (mapped.isPresent()) {
            return mapped.get();
        }
        if (name.contains("Arbeitsdienst")) {
            return EventType.WORK_EVENT;
        }
        var startTime = Instant.parse(start);
        var endTime = Instant.parse(end);
        ZoneId timezone = ZoneId.of("Europe/Berlin");
        if (LocalDate.ofInstant(startTime, timezone)
            .isEqual(LocalDate.ofInstant(endTime, timezone))) {
            return EventType.SINGLE_DAY_EVENT;
        }
        if (Duration.between(startTime, endTime).toDays() <= 3) {
            return EventType.WEEKEND_EVENT;
        }
        return EventType.MULTI_DAY_EVENT;
    }

    public @NonNull Event toDomain(@NonNull List<Registration> registrations) {
        return new Event(
            new EventKey(key),
            mapEventType(),
            EventSignupType.fromString(signupType).orElse(EventSignupType.ASSIGNMENT),
            name,
            EventState.fromString(state).orElse(EventState.PLANNED),
            note != null ? note : "",
            description != null ? description : "",
            Instant.parse(start),
            Instant.parse(end),
            deserializeLocations(locationsRaw),
            deserializeSlots(slotsRaw),
            registrations,
            confirmationsRequestsSent
        );
    }
}
