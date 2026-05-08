package org.eventplanner.events.rest;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.eventplanner.events.domain.exceptions.MissingPermissionException;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
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
        if (isClientAbort(exception)) {
            return ResponseEntity.noContent().build();
        }
        log.error("Unhandled exception on request {} {}", request.getMethod(), request.getRequestURI(), exception);
        var body = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Unexpected error, see server logs for details"
        );
        body.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Void> handleIOException(IOException ex, HttpServletRequest request) {
        if (isClientAbort(ex)) {
            return ResponseEntity.noContent().build();
        }
        log.error("I/O error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    private boolean isClientAbort(@NonNull Throwable t) {
        if (t.getClass().getName().contains("ClientAbortException")) {
            return true;
        }
        var msg = t.getMessage();
        if (msg != null && (msg.contains("Broken pipe") || msg.contains("Connection reset by peer"))) {
            return true;
        }
        if (t.getCause() != null) {
            return isClientAbort(t.getCause());
        }
        return false;
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

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public @NonNull ResponseEntity<ProblemDetail> handleRequestMethodNotSupportedException(
        @NonNull final HttpRequestMethodNotSupportedException exception,
        @NonNull final HttpServletRequest request
    ) {
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
        @NonNull final MissingServletRequestParameterException exception,
        @NonNull final HttpServletRequest request
    ) {
        var body = exception.getBody();
        body.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(body.getStatus()).body(body);
    }
}
