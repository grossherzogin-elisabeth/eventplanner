package org.eventplanner.users.entities;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
public class EmergencyContact {
    private @NonNull String name;
    private @NonNull String phone;
}
