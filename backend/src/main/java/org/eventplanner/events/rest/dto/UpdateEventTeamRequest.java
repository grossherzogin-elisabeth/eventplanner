package org.eventplanner.events.rest.dto;

import java.io.Serializable;
import java.util.List;

import org.eventplanner.events.entities.Registration;
import org.springframework.lang.NonNull;

public record UpdateEventTeamRequest(
    @NonNull List<RegistrationRepresentation> registrations
) implements Serializable {
    public List<Registration> toDomain() {
        return registrations.stream().map(RegistrationRepresentation::toDomain).toList();
    }
}
