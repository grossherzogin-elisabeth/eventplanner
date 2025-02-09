package org.eventplanner.events.adapter;

import java.util.List;

import org.eventplanner.events.entities.Registration;
import org.eventplanner.events.values.EventKey;
import org.springframework.lang.NonNull;

public interface RegistrationRepository {
    @NonNull
    Registration createRegistration(@NonNull Registration registration, @NonNull EventKey eventKey);

    @NonNull
    Registration updateRegistration(@NonNull Registration registration, @NonNull EventKey eventKey);

    void deleteRegistration(@NonNull Registration registration, @NonNull EventKey eventKey);
}
