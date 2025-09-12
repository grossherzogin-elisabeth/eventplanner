package org.eventplanner.events.rest;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.eventplanner.events.domain.exceptions.MissingPermissionException;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandlingController {

    @ExceptionHandler(Exception.class)
    public @NonNull ResponseEntity<ProblemDetail> handleException(
        @NonNull final Exception exception,
        @NonNull final HttpServletRequest request
    ) {
        if (isBrokenPipe(exception)) {
            log.info(
                "A broken pipe exception occurred on request {} {}",
                request.getMethod(),
                request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        log.error("Unhandled exception on request {} {}", request.getMethod(), request.getRequestURI(), exception);

        var body = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        body.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    private boolean isBrokenPipe(@Nullable final Throwable e) {
        return (e instanceof IOException && e.getMessage().contains("Broken pipe"))
            || (e != null && isBrokenPipe(e.getCause()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public @NonNull ResponseEntity<ProblemDetail> handleNoSuchElementException(
        @NonNull final NoSuchElementException exception,
        @NonNull final HttpServletRequest request
    ) {
        log.error(
            "Tried to access non existing element on request {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            exception.getMessage()
        );
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        body.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    @ExceptionHandler(MissingPermissionException.class)
    public @NonNull ResponseEntity<ProblemDetail> handleMissingPermissionException(
        @NonNull final MissingPermissionException exception,
        @NonNull final HttpServletRequest request
    ) {
        log.warn(
            "Tried to access resource at {} {} without proper permission",
            request.getMethod(),
            request.getRequestURI()
        );
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
        body.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public @NonNull ResponseEntity<ProblemDetail> handleUnauthorizedException(
        @NonNull final UnauthorizedException exception,
        @NonNull final HttpServletRequest request
    ) {
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        body.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public @NonNull ResponseEntity<ProblemDetail> handleIllegalArgumentException(
        @NonNull final IllegalArgumentException exception,
        @NonNull final HttpServletRequest request
    ) {
        log.warn(
            "Received invalid request parameters at {} {}",
            request.getMethod(),
            request.getRequestURI()
        );
        var body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        body.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public @NonNull ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
        @NonNull final MethodArgumentNotValidException exception,
        @NonNull final HttpServletRequest request
    ) {
        var errors = new HashMap<String, String>();
        exception.getAllErrors().stream()
            .filter(FieldError.class::isInstance)
            .map(FieldError.class::cast)
            .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        var body = exception.getBody();
        body.setInstance(URI.create(request.getRequestURI()));
        body.setProperty("errors", errors);
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public @NonNull ResponseEntity<ProblemDetail> handleMissingServletRequestParameterException(
        @NonNull final MethodArgumentNotValidException exception,
        @NonNull final HttpServletRequest request
    ) {
        var body = exception.getBody();
        body.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(body.getStatus()).body(body);
    }
}
