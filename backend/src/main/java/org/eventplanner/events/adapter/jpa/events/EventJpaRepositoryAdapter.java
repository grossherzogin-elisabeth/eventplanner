package org.eventplanner.events.adapter.jpa.events;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eventplanner.events.application.ports.EventRepository;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.events.EventKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventJpaRepositoryAdapter implements EventRepository {

    private final RegistrationJpaRepository registrationJpaRepository;
    private final EventJpaRepository eventJpaRepository;

    @Override
    public @NonNull Optional<Event> findByKey(@NonNull EventKey key) {
        var registrations = getRegistrations(key);
        return this.eventJpaRepository.findById(key.value())
            .map(entity -> entity.toDomain(registrations));
    }

    @Override
    public @NonNull List<Event> findAllByYear(int year) {
        var eventEntities = this.eventJpaRepository.findAllByYear(year);
        var eventKeys = eventEntities.stream().map(EventJpaEntity::getKey).toList();
        var allRegistrationEntities = this.registrationJpaRepository.findAllByEventKeyIn(eventKeys);
        return eventEntities.stream()
            .map(entity -> entity.toDomain(allRegistrationEntities
                .stream()
                .filter(it -> it.eventKey.equals(entity.getKey()))
                .map(RegistrationJpaEntity::toDomain)
                .toList()
            ))
            .toList();
    }

    @Override
    @Transactional
    public @NonNull Event create(@NonNull Event event) {
        var entity = EventJpaEntity.fromDomain(event);
        entity = this.eventJpaRepository.save(entity);
        return entity.toDomain(event.getRegistrations());
    }

    @Override
    @Transactional
    public @NonNull Event update(@NonNull Event event) {
        if (!this.eventJpaRepository.existsById(event.getKey().value())) {
            throw new NoSuchElementException();
        }
        var entity = EventJpaEntity.fromDomain(event);
        entity = this.eventJpaRepository.save(entity);
        return entity.toDomain(event.getRegistrations());
    }

    @Override
    @Transactional
    public void deleteByKey(@NonNull EventKey key) {
        this.eventJpaRepository.deleteById(key.value());
        this.registrationJpaRepository.deleteAllByEventKey(key.value());
    }

    @Override
    @Transactional
    public void deleteAllByYear(int year) {
        this.eventJpaRepository.deleteAllByYear(year);
    }

    private @NonNull List<Registration> getRegistrations(@NonNull EventKey eventKey) {
        return registrationJpaRepository.findAllByEventKey(eventKey.value())
            .stream()
            .map(RegistrationJpaEntity::toDomain)
            .toList();
    }
}
