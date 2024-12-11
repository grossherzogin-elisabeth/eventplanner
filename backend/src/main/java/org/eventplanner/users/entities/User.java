package org.eventplanner.users.entities;

import java.util.List;

import org.eventplanner.positions.values.PositionKey;
import org.eventplanner.users.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
public class User {
    private @NonNull UserKey key;
    private @NonNull String firstName;
    private @NonNull String lastName;
    private @Nullable String nickName;
    private @NonNull List<PositionKey> positions;
}
