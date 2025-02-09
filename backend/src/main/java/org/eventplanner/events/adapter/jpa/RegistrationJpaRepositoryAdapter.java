package org.eventplanner.events.adapter.jpa;

import java.util.List;
import java.util.NoSuchElementException;

import org.eventplanner.events.adapter.RegistrationRepository;
import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.values.EventKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RegistrationJpaRepositoryAdapter implements RegistrationRepository {

    private final RegistrationJpaRepository registrationJpaRepository;

    @Override
    public @NonNull Registration createRegistration(@NonNull Registration registration, @NonNull EventKey eventKey) {
        var entity = registrationJpaRepository.save(RegistrationJpaEntity.fromDomain(registration, eventKey));
        return entity.toDomain();
    }

    @Override
    public @NonNull Registration updateRegistration(@NonNull Registration registration, @NonNull EventKey eventKey) {
        if (!registrationJpaRepository.existsById(registration.getKey().value())) {
            throw new NoSuchElementException("Registration with key " + registration.getKey().value() + " does not exist");
        }
        var entity = registrationJpaRepository.save(RegistrationJpaEntity.fromDomain(registration, eventKey));
        return entity.toDomain();
    }

    @Override
    public void deleteRegistration(@NonNull Registration registration, @NonNull EventKey eventKey) {
        registrationJpaRepository.deleteById(registration.getKey().value());
    }
}
