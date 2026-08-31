package org.eventplanner.events.rest.errors.dto;

import java.io.Serializable;

import org.eventplanner.events.domain.values.errors.ErrorReport;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record ErrorReportDto(
    @NonNull String message,
    @NonNull String url,
    @Nullable String component,
    @Nullable String stacktrace
) implements Serializable {
    public @NonNull ErrorReport toDomain() {
        return new ErrorReport(message, url, component, stacktrace);
    }
}