package org.eventplanner.events.rest.calendar;

import lombok.RequiredArgsConstructor;
import org.eventplanner.events.application.usecases.IcsCalendarUseCase;
import org.eventplanner.events.application.usecases.UserUseCase;
import org.eventplanner.events.domain.entities.calendar.IcsCalendarInfo;
import org.eventplanner.events.rest.calendar.dto.CalendarRepresentation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Rest controller for managing iCalendar (.ics) functionalities.
 * Provides endpoints for creating and retrieving iCalendar resources.
 */
@RestController
@RequestMapping("/api/v1/calendar/")
@RequiredArgsConstructor
public class IcsCalendarController {

    private final UserUseCase userUseCase;
    private final IcsCalendarUseCase icsCalendarUseCase;


    /**
     * Creates a new calendar for the currently signed-in user.
     * Retrieves the signed-in user's information, generates a calendar specific to the user,
     * and returns a representation of the newly created calendar.
     *
     * @return a ResponseEntity containing the CalendarRepresentation object
     * of the created calendar, which includes the calendar's unique key,
     * security token, and associated user key.
     */
    @PutMapping("")
    public ResponseEntity<CalendarRepresentation> createCalendar() {
        var signedInUser = userUseCase.getSignedInUser(SecurityContextHolder.getContext().getAuthentication());
        IcsCalendarInfo calendarInfo = icsCalendarUseCase.createCalendar(signedInUser.key());

        CalendarRepresentation calendarRepresentation = new CalendarRepresentation(
                calendarInfo.getKey(),
                calendarInfo.getToken(),
                calendarInfo.getUser().value()
        );

        return ResponseEntity.ok().body(calendarRepresentation);
    }


    /**
     * Retrieves the iCalendar (.ics) content for the specified calendar key if the provided
     * token is valid. Returns an unauthorized response if the token is invalid or the calendar
     * does not exist.
     *
     * @param key   the unique identifier of the calendar to retrieve
     * @param token the security token associated with the calendar for authorization
     * @return a ResponseEntity containing the .ics calendar content as a string if successful,
     * or an unauthorized status if the token is invalid or the calendar is not found
     */
    @GetMapping(value = "/{key}.ics", produces = "text/calendar")
    public ResponseEntity<String> getCalendar(
            @PathVariable String key,
            @RequestParam String token
    ) {

        Optional<IcsCalendarInfo> calendar = icsCalendarUseCase.getCalendarDetails(key, token);

        if (calendar.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String ics = icsCalendarUseCase.getIcsCalendarValueString(calendar.get());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=calendar.ics")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(ics);
    }


}
