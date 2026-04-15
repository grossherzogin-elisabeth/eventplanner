package org.eventplanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;

@Configuration
public class JsonMapperFactory {

    private JsonMapper jsonMapper;

    @Bean
    public @NonNull JsonMapper jsonMapper() {
        if (jsonMapper == null) {
            jsonMapper = defaultJsonMapper();
        }
        return jsonMapper;
    }

    public static @NonNull JsonMapper defaultJsonMapper() {
        return JsonMapper.builder()
            .findAndAddModules()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // Use ISO 8601 date format when serializing date types
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .defaultDateFormat(new StdDateFormat())
            .build();
    }

    // public static @NonNull ObjectMapper defaultObjectMapper() {
    //     ObjectMapper objectMapper = new ObjectMapper();
    //     objectMapper.registerModule(new JavaTimeModule());
    //     objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    //     // Use ISO 8601 date format when serializing date types
    //     objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    //     objectMapper.setDateFormat(new StdDateFormat());
    //     return objectMapper;
    // }
}
