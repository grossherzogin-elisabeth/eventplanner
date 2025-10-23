package org.eventplanner.events.adapter.jpa.calendar;

import org.eventplanner.events.application.ports.IcsCalendarRepository;
import org.eventplanner.events.domain.entities.calendar.IcsCalendarInfo;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementation of the {@link IcsCalendarRepository} interface, using
 * a JPA repository to handle persistence operations for ICS calendar information.
 * This class acts as a bridge between the domain-level {@link IcsCalendarRepository}
 * interface and the underlying JPA repository {@link IcsCalendarJpaRepository}.
 * <p>
 * It provides methods to:
 * - Retrieve an ICS calendar record by key and token.
 * - Retrieve an ICS calendar record by user key.
 * - Create a new ICS calendar record for a given user.
 * - Delete an ICS calendar record by key or user key.
 * <p>
 * Utilizes the {@link IcsCalendarJpaRepository} for database interactions and
 * converts the JPA entity {@link IcsCalendarJpaEntity} to the domain model
 * {@link IcsCalendarInfo}.
 */
@Component
public class IcsCalendarJpaRepositoryAdapter implements IcsCalendarRepository {
    private final IcsCalendarJpaRepository repository;

    public IcsCalendarJpaRepositoryAdapter(IcsCalendarJpaRepository repository) {
        this.repository = repository;
    }


    @Override
    public @NonNull Optional<IcsCalendarInfo> findByKeyAndToken(@NonNull String key, @NonNull String token) {
        return repository.findFirstByKeyAndToken(key, token).map(IcsCalendarJpaEntity::toDomain);
    }

    @Override
    public @NonNull Optional<IcsCalendarInfo> findByUser(@NonNull UserKey userKey) {
        return repository.findAllByUserKey(userKey.value()).map(IcsCalendarJpaEntity::toDomain);
    }

    @Override
    public @NonNull IcsCalendarInfo create(@NonNull UserKey user) {
        IcsCalendarJpaEntity entity = new IcsCalendarJpaEntity();
        entity.setKey(UUID.randomUUID().toString());
        entity.setToken(UUID.randomUUID().toString());
        entity.setUserKey(user.toString());
        return repository.save(entity).toDomain();
    }


    @Override
    public void deleteByKey(@NonNull String key) {
        repository.deleteById(key);
    }

    @Override
    public void deleteByUser(@NonNull String userKey) {
        repository.deleteByUserKey(userKey);
    }
}
