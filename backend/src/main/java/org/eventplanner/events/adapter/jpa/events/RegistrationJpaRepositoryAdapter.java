package org.eventplanner.events.adapter.jpa.events;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.eventplanner.events.application.ports.RegistrationRepository;
import org.eventplanner.events.domain.entities.Registration;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
            throw new NoSuchElementException("Registration with key " + registration.getKey()
                .value() + " does not exist");
        }
        var entity = registrationJpaRepository.save(RegistrationJpaEntity.fromDomain(registration, eventKey));
        return entity.toDomain();
    }

    @Override
    public void deleteRegistration(@NonNull RegistrationKey registrationKey, @NonNull EventKey eventKey) {
        registrationJpaRepository.deleteByKeyAndEventKey(registrationKey.value(), eventKey.value());
    }

    @PostConstruct
    public @NonNull void generateMissingAccessKeys() {
        var registrations = registrationJpaRepository.findAllByAccessKeyNull();
        registrations.forEach(registration -> {
            log.info("Generating missing access key for registration {}", registration.getKey());
            registration.setAccessKey(UUID.randomUUID().toString());
            registrationJpaRepository.save(registration);
        });
    }
}
