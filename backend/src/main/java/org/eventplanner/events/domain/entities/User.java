package org.eventplanner.events.domain.entities;

import java.util.List;

import org.eventplanner.events.domain.values.PositionKey;
import org.eventplanner.events.domain.values.UserKey;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public class User {
    private @NonNull UserKey key;
    private @NonNull String firstName;
    private @NonNull String lastName;
    private @Nullable String nickName;
    private @NonNull List<PositionKey> positions;
}
