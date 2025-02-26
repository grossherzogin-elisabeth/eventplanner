package org.eventplanner.adapter.jpa.events;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.eventplanner.domain.entities.Event;
import org.eventplanner.domain.entities.EventSlot;
import org.eventplanner.domain.entities.Registration;
import org.eventplanner.domain.values.EventKey;
import org.eventplanner.domain.values.EventLocation;
import org.eventplanner.domain.values.EventState;
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
    private Integer participationConfirmationsRequestsSent;

    public static @NonNull EventJpaEntity fromDomain(@NonNull Event domain) {
        var eventJpaEntity = new EventJpaEntity();
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
        eventJpaEntity.setParticipationConfirmationsRequestsSent(domain.getParticipationConfirmationsRequestsSent());
        return eventJpaEntity;
    }

    public static List<EventLocation> deserializeLocations(String json) {
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

    public static String serializeLocations(List<EventLocation> locations) {
        try {
            var entities = locations.stream().map(LocationJsonEntity::fromDomain).toList();
            return objectMapper.writeValueAsString(entities);
        } catch (IOException e) {
            return "[]";
        }
    }

    public static List<EventSlot> deserializeSlots(String json) {
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

    public static String serializeSlots(List<EventSlot> slots) {
        try {
            var entities = slots.stream().map(SlotJsonEntity::fromDomain).toList();
            return objectMapper.writeValueAsString(entities);
        } catch (IOException e) {
            return "[]";
        }
    }

    public Event toDomain(@NonNull List<Registration> registrations) {
        return new Event(
            new EventKey(key),
            name,
            EventState.fromString(state).orElse(EventState.PLANNED),
            note != null ? note : "",
            description != null ? description : "",
            Instant.parse(start),
            Instant.parse(end),
            deserializeLocations(locationsRaw),
            deserializeSlots(slotsRaw),
            registrations,
            participationConfirmationsRequestsSent
        );
    }
}
