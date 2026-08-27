package org.eventplanner.events.application.usecases.errors;

import org.eventplanner.events.domain.values.ErrorReport;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.event.Level;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReportErrorsUseCase {

    @PreAuthorize("hasAuthority('errors:report')")
    public void report(@NonNull final Level level, @NonNull @Valid final ErrorReport error) {
        log.atLevel(level)
            .addKeyValue("component", sanitizeInput(error.component()))
            .addKeyValue("stacktrace", sanitizeInput(error.stacktrace()))
            .log(sanitizeInput(error.message()));
    }

    protected @Nullable String sanitizeInput(@Nullable String raw) {
        if (raw != null) {
            return raw
                .replaceAll("[\\r\\n\\t]", " ")// remove log-breaking control chars
                .replaceAll("[^\\p{L}\\p{N} .,:!?@()'/_\\-#]", "*"); // keep letters, numbers, and common punctuation
        }
        return null;
    }
}
