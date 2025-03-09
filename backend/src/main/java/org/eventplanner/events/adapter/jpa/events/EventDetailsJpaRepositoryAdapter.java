package org.eventplanner.events.adapter.jpa.events;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eventplanner.events.application.ports.EventDetailsRepository;
import org.eventplanner.events.domain.entities.events.EventDetails;
import org.eventplanner.events.domain.values.EventKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventDetailsJpaRepositoryAdapter implements EventDetailsRepository {

    private final EventJpaRepository eventJpaRepository;
    private final RegistrationJpaRepository registrationJpaRepository;

    @Override
    public @NonNull Optional<EventDetails> findByKey(@NonNull EventKey key) {
        return this.eventJpaRepository.findById(key.value())
            .map(EventJpaEntity::toDomain);
    }

    @Override
    public @NonNull List<EventDetails> findAllByYear(int year) {
        return this.eventJpaRepository.findAllByYear(year).stream()
            .map(EventJpaEntity::toDomain)
            .toList();
    }

    @Override
    @Transactional
    public @NonNull EventDetails create(@NonNull EventDetails eventDetails) {
        var entity = EventJpaEntity.fromDomain(eventDetails);
        entity = this.eventJpaRepository.save(entity);
        return entity.toDomain();
    }

    @Override
    @Transactional
    public @NonNull EventDetails update(@NonNull EventDetails eventDetails) {
        if (!this.eventJpaRepository.existsById(eventDetails.getKey().value())) {
            throw new NoSuchElementException();
        }
        var entity = EventJpaEntity.fromDomain(eventDetails);
        entity = this.eventJpaRepository.save(entity);
        return entity.toDomain();
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
}
