package org.eventplanner.events.adapter.jpa.calendar;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eventplanner.events.domain.entities.calendar.IcsCalendarInfo;
import org.eventplanner.events.domain.values.users.UserKey;

/**
 * Represents a JPA entity for storing ICS calendar information.
 * This entity is mapped to the database table "ics_calendar".
 * It contains data such as a unique key, a token for the calendar,
 * and the user key to associate with a specific user.
 * <p>
 * The entity is used to interact with the database and map its records
 * to the corresponding domain object {@link IcsCalendarInfo}.
 */
@Entity
@Table(name = "ics_calendar")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class IcsCalendarJpaEntity {
    @Id
    @Column(name = "key", nullable = false, updatable = false)
    private String key;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "user_key", nullable = false)
    private String userKey;

    public IcsCalendarInfo toDomain() {
        return new IcsCalendarInfo(this.key, this.token, new UserKey(this.userKey));
    }
}
