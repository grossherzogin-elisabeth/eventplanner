package org.eventplanner.config;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.resilience.retry.MethodRetryEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableResilientMethods(proxyTargetClass = true)
public class RetryConfig {

    @EventListener
    private void logRetryEvents(@NonNull final MethodRetryEvent event) {
        if (event.isRetryAborted()) {
            log.warn(
                "Retry attempts for method: {}::{} have been exhausted without a successful retry",
                event.getMethod().getDeclaringClass().getSimpleName(),
                event.getMethod().getName(),
                event.getFailure()
            );
        } else {
            log.info(
                "Retrying method: {}::{} after {}",
                event.getMethod().getDeclaringClass().getSimpleName(),
                event.getMethod().getName(),
                event.getFailure().getClass().getSimpleName()
            );
        }
    }
}
