package org.eventplanner.events.adapter.jpa.events;

import java.util.List;
import java.util.NoSuchElementException;

import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.EventKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RegistrationJpaRepositoryAdapter implements RegistrationRepository {

    private final RegistrationJpaRepository registrationJpaRepository;

    @Override
    public @NonNull List<Registration> findAll(@NonNull final List<EventKey> eventKeys) {
        var keys = eventKeys.stream().map(EventKey::toString).toList();
        return registrationJpaRepository.findAllByEventKeyIn(keys)
            .stream()
            .map(RegistrationJpaEntity::toDomain)
            .toList();
    }

    @Override
    public @NonNull List<Registration> findAll(@NonNull final EventKey eventkey) {
        return findAll(List.of(eventkey));
    }

    @Override
    public @NonNull Registration createRegistration(@NonNull Registration registration) {
        var entity = registrationJpaRepository.save(RegistrationJpaEntity.fromDomain(registration));
        return entity.toDomain();
    }

    @Override
    public @NonNull Registration updateRegistration(@NonNull Registration registration) {
        if (!registrationJpaRepository.existsById(registration.getKey().value())) {
            throw new NoSuchElementException("Registration with key " + registration.getKey()
                .value() + " does not exist");
        }
        var entity = registrationJpaRepository.save(RegistrationJpaEntity.fromDomain(registration));
        return entity.toDomain();
    }

    @Override
    public void deleteRegistration(@NonNull Registration registration) {
        registrationJpaRepository.deleteById(registration.getKey().value());
    }
}
