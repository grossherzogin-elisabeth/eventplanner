package org.eventplanner.events.adapter.jpa;

import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.values.EventKey;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EventJpaRepositoryAdapter implements EventRepository {

    private final EventJpaRepository eventJpaRepository;

    public EventJpaRepositoryAdapter(EventJpaRepository eventJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
    }

    @Override
    public Optional<Event> findByKey(EventKey key) {
        return this.eventJpaRepository.findById(key.value())
                .map(EventJpaEntity::toDomain);
    }

    @Override
    public List<Event> findAllByYear(int year) {
        return this.eventJpaRepository.findAllByYear(year)
                .map(EventJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Event create(Event event) {
        var entity = EventJpaEntity.fromDomain(event);
        entity = this.eventJpaRepository.save(entity);
        return entity.toDomain();
    }

    @Override
    public Event update(Event event) {
        var entity = EventJpaEntity.fromDomain(event);
        entity = this.eventJpaRepository.save(entity);
        return entity.toDomain();
    }

    @Override
    public void deleteByKey(EventKey key) {
        this.eventJpaRepository.deleteById(key.value());
    }

    @Override
    public void deleteAllByYear(int year) {
        this.eventJpaRepository.deleteAllByYear(year);
    }
}
