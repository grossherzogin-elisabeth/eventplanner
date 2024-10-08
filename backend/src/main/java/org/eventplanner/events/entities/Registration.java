package org.eventplanner.events.entities;

import lombok.*;
import org.eventplanner.events.values.RegistrationKey;
import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
public class Registration {
    private @NonNull RegistrationKey key;
    private @NonNull PositionKey position;
    private @Nullable UserKey user;
    private @Nullable String name;
    private @Nullable String note;

    public static Registration ofUser(@NonNull UserKey user, @NonNull PositionKey position) {
        return new Registration(new RegistrationKey(), position, user, null, null);
    }

    public static Registration ofPerson(@NonNull String name, @NonNull PositionKey position) {
        return new Registration(new RegistrationKey(), position, null, name, null);
    }
}
