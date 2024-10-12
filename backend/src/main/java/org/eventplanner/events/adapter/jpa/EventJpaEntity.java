package org.eventplanner.events.adapter.jpa;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.*;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.events.values.EventState;
import org.eventplanner.events.values.Location;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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

    @Column(name = "registrations")
    private String registrationsRaw;

    @Transient
    private List<Location> locations;

    @Transient
    private List<Slot> slots;

    @Transient
    private List<Registration> registrations;

    public static @NonNull EventJpaEntity fromDomain(@NonNull Event domain) {
        var eventJpaEntity = new EventJpaEntity();
        eventJpaEntity.setKey(domain.getKey().value());
        eventJpaEntity.setYear(domain.getStart().getYear());
        eventJpaEntity.setName(domain.getName());
        eventJpaEntity.setState(domain.getState().value());
        eventJpaEntity.setNote(domain.getNote());
        eventJpaEntity.setDescription(domain.getDescription());
        eventJpaEntity.setStart(domain.getStart().format(DateTimeFormatter.ISO_DATE_TIME));
        eventJpaEntity.setEnd(domain.getEnd().format(DateTimeFormatter.ISO_DATE_TIME));
        eventJpaEntity.setLocations(domain.getLocations());
        eventJpaEntity.setSlots(domain.getSlots());
        eventJpaEntity.setRegistrations(domain.getRegistrations());
        return eventJpaEntity;
    }

    public List<Location> getLocations() {
        if (locations != null) {
            return locations;
        }
        try {
            var entities = objectMapper.readValue(locationsRaw, new TypeReference<List<LocationJsonEntity>>() {});
            locations = entities.stream().map(LocationJsonEntity::toDomain).toList();
        } catch (IOException e) {
            locations = Collections.emptyList();
        }
        return locations;
    }

    public void setLocations(List<Location> locations) {
        try {
            var entities = locations.stream().map(LocationJsonEntity::fromDomain).toList();
            this.locationsRaw = objectMapper.writeValueAsString(entities);
        } catch (IOException e) {
            this.locationsRaw = "[]";
        }
    }

    public List<Slot> getSlots() {
        if (slots != null) {
            return slots;
        }
        try {
            var entities = objectMapper.readValue(slotsRaw, new TypeReference<List<SlotJsonEntity>>() {});
            slots = entities.stream().map(SlotJsonEntity::toDomain).toList();
        } catch (IOException e) {
            slots = Collections.emptyList();
        }
        return slots;
    }

    public void setSlots(List<Slot> slots) {
        try {
            var entities = slots.stream().map(SlotJsonEntity::fromDomain).toList();
            this.slotsRaw = objectMapper.writeValueAsString(entities);
        } catch (IOException e) {
            this.slotsRaw = "[]";
        }
    }

    public List<Registration> getRegistrations() {
        if (registrations != null) {
            return registrations;
        }
        try {
            var entities = objectMapper.readValue(registrationsRaw, new TypeReference<List<RegistrationJsonEntity>>() {});
            registrations = entities.stream().map(RegistrationJsonEntity::toDomain).toList();
        } catch (IOException e) {
            registrations = Collections.emptyList();
        }
        return registrations;
    }

    public void setRegistrations(List<Registration> registrations) {
        try {
            var entities = registrations.stream().map(RegistrationJsonEntity::fromDomain).toList();
            this.registrationsRaw = objectMapper.writeValueAsString(entities);
        } catch (IOException e) {
            this.registrationsRaw = "[]";
        }
    }

    public Event toDomain() {
        return new Event(
            new EventKey(key),
            name,
            EventState.fromString(state).orElse(EventState.PLANNED),
            note != null ? note : "",
            description != null ? description : "",
            ZonedDateTime.parse(start),
            ZonedDateTime.parse(end),
            getLocations(),
            getSlots(),
            getRegistrations()
        );
    }
}
