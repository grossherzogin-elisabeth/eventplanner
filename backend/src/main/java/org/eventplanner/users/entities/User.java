package org.eventplanner.users.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.UserKey;
import org.springframework.lang.NonNull;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
public class User {
    private @NonNull UserKey key;
    private @NonNull String firstName;
    private @NonNull String lastName;
    private @NonNull List<PositionKey> positions;
}
