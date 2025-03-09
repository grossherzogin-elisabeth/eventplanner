package org.eventplanner.events.domain.entities.users;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public class UserEmergencyContact {
    private @NonNull String name;
    private @NonNull String phone;
}
