package org.eventplanner.events.domain.values;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.Size;

public record ErrorReport(
    @NonNull @Size(max = 500) String message,
    @NonNull @Size(max = 100) String url,
    @Nullable @Size(max = 50) String component,
    @Nullable @Size(max = 500) String stacktrace
) {
}
