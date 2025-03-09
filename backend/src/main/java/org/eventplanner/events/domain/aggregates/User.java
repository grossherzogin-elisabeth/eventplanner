package org.eventplanner.events.domain.aggregates;

import java.util.List;

import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.PositionKey;
import org.springframework.lang.NonNull;

public record User(
    @NonNull UserDetails details,
    @NonNull List<PositionKey> positions
) {
}
