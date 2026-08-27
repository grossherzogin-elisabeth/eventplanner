package org.eventplanner.events.rest.errors;

import org.eventplanner.events.application.usecases.errors.ReportErrorsUseCase;
import org.eventplanner.events.rest.errors.dto.ErrorReportDto;
import org.jspecify.annotations.NonNull;
import org.slf4j.event.Level;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ErrorReportingController {

    private final ReportErrorsUseCase reportErrorsUseCase;

    @PostMapping("/error")
    public @NonNull ResponseEntity<Void> reportError(@RequestBody @NonNull ErrorReportDto log) {
        reportErrorsUseCase.report(Level.ERROR, log.toDomain());
        return ResponseEntity.noContent().build();
    }
}
