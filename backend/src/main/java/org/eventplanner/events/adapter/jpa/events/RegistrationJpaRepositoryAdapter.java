package org.eventplanner.events.adapter.jpa.events;

import static java.time.ZoneId.systemDefault;

import java.util.NoSuchElementException;

import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrationJpaRepositoryAdapter implements RegistrationRepository {

    private final RegistrationJpaRepository registrationJpaRepository;

    @Override
    @Transactional
    @Retryable(
        includes = PessimisticLockingFailureException.class,
        delayString = "${resilience.retry.delay:1000}",
        jitterString = "${resilience.retry.jitter:0}",
        multiplierString = "${resilience.retry.multiplier:1}",
        maxRetriesString = "${resilience.retry.max-retries:3}")
    public @NonNull Registration createRegistration(@NonNull Registration registration, @NonNull Event event) {
        if (registrationJpaRepository.existsById(registration.getKey().value())) {
            log.error("Failed to create new registration: key {} already exists", registration.getKey());
            throw new IllegalStateException("Registration with key " + registration.getKey() + " already exists");
        }
        var entity = registrationJpaRepository.save(RegistrationJpaEntity.fromDomain(registration, event));
        return entity.toDomain();
    }

    @Override
    @Transactional
    @Retryable(
        includes = PessimisticLockingFailureException.class,
        delayString = "${resilience.retry.delay:1000}",
        jitterString = "${resilience.retry.jitter:0}",
        multiplierString = "${resilience.retry.multiplier:1}",
        maxRetriesString = "${resilience.retry.max-retries:3}")
    public @NonNull Registration updateRegistration(@NonNull Registration registration, @NonNull Event event) {
        var entity = registrationJpaRepository.findByKeyAndEventKey(
                registration.getKey().value(),
                event.getKey().value()
            )
            .orElseThrow(() -> new NoSuchElementException(
                "Registration with key " + registration.getKey() + " does not exist on event " + event.getKey()));

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
        entity.setYear(event.getStart().atZone(systemDefault()).getYear());

        entity = registrationJpaRepository.save(entity);
        return entity.toDomain();
    }

    @Override
    @Transactional
    @Retryable(
        includes = PessimisticLockingFailureException.class,
        delayString = "${resilience.retry.delay:1000}",
        jitterString = "${resilience.retry.jitter:0}",
        multiplierString = "${resilience.retry.multiplier:1}",
        maxRetriesString = "${resilience.retry.max-retries:3}")
    public void deleteRegistration(@NonNull RegistrationKey registrationKey, @NonNull EventKey eventKey) {
        registrationJpaRepository.deleteByKeyAndEventKey(registrationKey.value(), eventKey.value());
    }
}
