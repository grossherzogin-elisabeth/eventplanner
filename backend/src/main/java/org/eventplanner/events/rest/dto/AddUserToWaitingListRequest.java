package org.eventplanner.events.rest.dto;

import java.io.Serializable;

import org.springframework.lang.NonNull;

public record AddUserToWaitingListRequest(
    @NonNull String userKey,
    @NonNull String positionKey
) implements Serializable {
}
