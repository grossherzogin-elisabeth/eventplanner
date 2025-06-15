package org.eventplanner.events.domain.entities.users;

import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class User {
    private @NonNull UserKey key;
    private @NonNull String firstName;
    private @NonNull String lastName;
    private @Nullable String nickName;
}
