package org.eventplanner.events.adapter.jpa.events;

import java.util.NoSuchElementException;

import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationJpaRepositoryAdapter implements RegistrationRepository {

    private final RegistrationJpaRepository registrationJpaRepository;

    @Override
    public @NonNull Registration createRegistration(@NonNull Registration registration, @NonNull Event event) {
        if (registrationJpaRepository.existsById(registration.getKey().value())) {
            log.error("Failed to create new registration: key {} already exists", registration.getKey());
            throw new IllegalStateException("Registration with key " + registration.getKey() + " already exists");
        }
        var entity = registrationJpaRepository.save(RegistrationJpaEntity.fromDomain(registration, event));
        return entity.toDomain();
    }

    @Override
    public @NonNull Registration updateRegistration(@NonNull Registration registration, @NonNull EventKey eventKey) {
        var entity = registrationJpaRepository.findByKeyAndEventKey(
                registration.getKey().value(),
                eventKey.value()
            )
            .orElseThrow(() -> new NoSuchElementException(
                "Registration with key " + registration.getKey() + " does not exist on event " + eventKey));

        entity.setPositionKey(registration.getPosition().value());
        entity.setName(registration.getName());
        entity.setNote(registration.getNote());
        entity.setArrival(registration.getArrival() != null
            ? registration.getArrival().toString()
            : null);
        entity.setConfirmedAt(registration.getConfirmedAt() != null
            ? registration.getConfirmedAt().toString()
            : null);
        entity.setOvernightStay(registration.getOvernightStay());

        entity = registrationJpaRepository.save(entity);
        return entity.toDomain();
    }

    @Override
    public void deleteRegistration(@NonNull RegistrationKey registrationKey, @NonNull EventKey eventKey) {
        registrationJpaRepository.deleteByKeyAndEventKey(registrationKey.value(), eventKey.value());
    }
}
