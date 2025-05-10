package org.eventplanner.events.application.ports;

import org.eventplanner.events.domain.entities.Registration;
import org.eventplanner.events.domain.values.EventKey;
import org.eventplanner.events.domain.values.RegistrationKey;
import org.springframework.lang.NonNull;

public interface RegistrationRepository {
    @NonNull
    Registration createRegistration(@NonNull Registration registration, @NonNull EventKey eventKey);

    @NonNull
    Registration updateRegistration(@NonNull Registration registration, @NonNull EventKey eventKey);

    void deleteRegistration(@NonNull RegistrationKey registrationKey, @NonNull EventKey eventKey);
}
