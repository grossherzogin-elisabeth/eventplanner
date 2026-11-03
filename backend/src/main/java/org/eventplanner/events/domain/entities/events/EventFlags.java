package org.eventplanner.events.domain.entities.events;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

@Getter
@Setter
@With
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventFlags {
    boolean enableDirectSignup = false;
    boolean enableRegistrationConfirmation = false;
    boolean enableOvernightStaySelection = false;
    boolean enableArrivalDaySelection = false;
}
