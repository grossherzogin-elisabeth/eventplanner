package org.eventplanner.events.adapter.jpa;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.entities.Slot;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.events.values.EventState;
import org.eventplanner.events.values.Location;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class EventJpaEntity {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @Column(name = "key", nullable = false)
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

    public List<Location> getLocations() {
        try {
            return objectMapper.readValue(locationsRaw, new TypeReference<>() {});
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public void setLocations(List<Location> locations) {
        try {
            this.locationsRaw = objectMapper.writeValueAsString(locations);
        } catch (IOException e) {
            this.locationsRaw = "[]";
        }
    }

    public List<Slot> getSlots() {
        try {
            return objectMapper.readValue(slotsRaw, new TypeReference<>() {});
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public void setSlots(List<Slot> slots) {
        try {
            this.slotsRaw = objectMapper.writeValueAsString(slots);
        } catch (IOException e) {
            this.slotsRaw = "[]";
        }
    }

    public List<Registration> getRegistrations() {
        try {
            return objectMapper.readValue(registrationsRaw, new TypeReference<>() {});
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public void setRegistrations(List<Registration> registrations) {
        try {
            this.registrationsRaw = objectMapper.writeValueAsString(registrations);
        } catch (IOException e) {
            this.registrationsRaw = "[]";
        }
    }

    public static @NonNull EventJpaEntity fromDomain(@NonNull Event domain) {
        var eventJpaEntity = new EventJpaEntity();
        eventJpaEntity.setKey(domain.getKey().value());
        eventJpaEntity.setYear(domain.getStart().getYear());
        eventJpaEntity.setName(domain.getName());
        eventJpaEntity.setState(domain.getState().value());
        eventJpaEntity.setNote(domain.getNote());
        eventJpaEntity.setDescription(domain.getDescription());
        eventJpaEntity.setStart(domain.getStart().format(DateTimeFormatter.ISO_DATE_TIME));
        eventJpaEntity.setEnd(domain.getStart().format(DateTimeFormatter.ISO_DATE_TIME));
        eventJpaEntity.setLocations(domain.getLocations());
        eventJpaEntity.setSlots(domain.getSlots());
        eventJpaEntity.setRegistrations(domain.getRegistrations());
        return eventJpaEntity;
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
