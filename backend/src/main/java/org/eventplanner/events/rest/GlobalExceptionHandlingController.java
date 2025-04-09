package org.eventplanner.events.rest;

import java.io.IOException;
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
        if (isBrokenPipe(e)) {
            log.info(
                "A broken pipe exception occurred on request {} {}",
                request.getMethod(),
                request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        log.error("Unhandled exception on request {} {}", request.getMethod(), request.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    private boolean isBrokenPipe(Throwable e) {
        return (e instanceof IOException && e.getMessage().contains("Broken pipe"))
            || isBrokenPipe(e.getCause());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Void> handleNoSuchElementException(NoSuchElementException e, HttpServletRequest request) {
        log.error(
            "Tried to access non existing element on request {} {}",
            request.getMethod(),
            request.getRequestURI()
        );
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
    public ResponseEntity<Void> handleIllegalArgumentException(HttpServletRequest request, Exception e) {
        log.warn(
            "Received invalid request parameters at {} {}",
            request.getMethod(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
