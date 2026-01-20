package org.eventplanner.events.domain.entities.calendar;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.eventplanner.events.domain.values.users.UserKey;
import org.springframework.lang.NonNull;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@ToString
public class IcsCalendarInfo {
    private @NonNull String key;
    private @NonNull String token;
    private @NonNull UserKey user;
}
