package org.eventplanner.events.application.ports;

import org.eventplanner.events.domain.entities.events.Event;
import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.events.EventKey;
import org.eventplanner.events.domain.values.events.RegistrationKey;
import org.jspecify.annotations.NonNull;

public interface RegistrationRepository {
    @NonNull Registration createRegistration(@NonNull Registration registration, @NonNull Event event);

    @NonNull Registration updateRegistration(@NonNull Registration registration, @NonNull Event event);

    void deleteRegistration(@NonNull RegistrationKey registrationKey, @NonNull EventKey eventKey);
}
