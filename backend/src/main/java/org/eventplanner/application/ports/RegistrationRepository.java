package org.eventplanner.application.ports;

import org.eventplanner.domain.entities.Registration;
import org.eventplanner.domain.values.EventKey;
import org.springframework.lang.NonNull;

public interface RegistrationRepository {
    @NonNull
    Registration createRegistration(@NonNull Registration registration, @NonNull EventKey eventKey);

    @NonNull
    Registration updateRegistration(@NonNull Registration registration, @NonNull EventKey eventKey);

    void deleteRegistration(@NonNull Registration registration, @NonNull EventKey eventKey);
}
