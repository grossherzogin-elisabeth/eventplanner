package org.eventplanner.events.rest;

import java.util.NoSuchElementException;

import org.eventplanner.events.domain.exceptions.MissingPermissionException;
import org.eventplanner.events.domain.exceptions.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandlingController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleException(Exception e, HttpServletRequest request) {
        if (request != null) {
            log.error("Unhandled exception on request {} {}", request.getMethod(), request.getRequestURI(), e);
        } else {
            log.error("An unexpected exception occurred", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Void> handleNoSuchElementException() {
        log.warn("Tried to access non-existent element");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(MissingPermissionException.class)
    public ResponseEntity<Void> handleMissingPermissionException(HttpServletRequest request) {
        log.warn(
            "Tried to access resource at {} {} without proper permission",
            request.getMethod(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Void> handleUnauthorizedException() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgumentException(HttpServletRequest request) {
        log.warn(
            "Received invalid request parameters at {} {}",
            request.getMethod(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
