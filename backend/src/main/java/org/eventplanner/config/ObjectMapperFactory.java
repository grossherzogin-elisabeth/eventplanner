package org.eventplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.util.StdDateFormat;

public class ObjectMapperFactory {

    private ObjectMapper objectMapper;

    @Bean
    public @NonNull ObjectMapper objectMapper() {
        if (objectMapper == null) {
            objectMapper = defaultObjectMapper();
        }
        return objectMapper;
    }

    public static @NonNull ObjectMapper defaultObjectMapper() {
        return JsonMapper.builder()
            .findAndAddModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            // .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .defaultDateFormat(new StdDateFormat())
            .build();

        // ObjectMapper objectMapper = new ObjectMapper();
        // objectMapper.findAndRegisterModules();
        // objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // // Use ISO 8601 date format when serializing date types
        // objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // objectMapper.setDateFormat(new StdDateFormat());
        // return objectMapper;
    }
}
