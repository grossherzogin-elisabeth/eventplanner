package org.eventplanner.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Timelog {

    private static final Map<UUID, Long> STARTS = new HashMap<>();
    private static final Map<UUID, Long> INTERMEDIATES = new HashMap<>();

    public static void start() {
        var traceid = getTraceId();
        var now = System.currentTimeMillis();
        STARTS.put(traceid, now);
        INTERMEDIATES.put(traceid, now);
        log.debug("Started timelogging for {}", traceid);
    }

    public static void logIntermediate() {
        var traceid = getTraceId();
        long diff = proceedIntermediate(traceid);
        log.debug("{}: +{}ms since last log", traceid, diff);
    }

    public static void logIntermediate(@NonNull String label) {
        var traceid = getTraceId();
        long diff = proceedIntermediate(traceid);
        log.debug("{} - {}: +{}ms since last log", traceid, label, diff);
    }

    private static long proceedIntermediate(@NonNull UUID traceid) {
        var now = System.currentTimeMillis();
        if (!STARTS.containsKey(traceid)) {
            STARTS.put(traceid, now);
        }
        long diff = now - INTERMEDIATES.getOrDefault(traceid, now);
        INTERMEDIATES.put(traceid, now);
        return diff;
    }

    public static void stop() {
        var traceid = getTraceId();
        var now = System.currentTimeMillis();
        long diff = now - STARTS.getOrDefault(traceid, now);
        log.debug("Stopped timelogging for {} after {}ms total", traceid, diff);
        STARTS.remove(traceid);
        INTERMEDIATES.remove(traceid);
    }

    private static @NonNull UUID getTraceId() {
        var traceid = MDC.get("trace_id");
        if (traceid != null) {
            return UUID.fromString(traceid);
        }
        return UUID.randomUUID();
    }
}
