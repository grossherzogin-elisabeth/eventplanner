package org.eventplanner.events.application.ports;

import java.util.List;

import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.values.EventKey;
import org.springframework.lang.NonNull;

public interface RegistrationRepository {

    public @NonNull List<Registration> findAll(@NonNull EventKey eventkey);

    public @NonNull List<Registration> findAll(@NonNull List<EventKey> eventKeys);

    public @NonNull Registration createRegistration(@NonNull Registration registration);

    public @NonNull Registration updateRegistration(@NonNull Registration registration);

    public void deleteRegistration(@NonNull Registration registration);
}
