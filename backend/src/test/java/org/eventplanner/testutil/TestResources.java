package org.eventplanner.testutil;

import java.io.IOException;

import org.eventplanner.config.ObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestResources {
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.defaultObjectMapper();

    public static String getString(String path) {
        return new String(getBytes(path));
    }

    public static byte[] getBytes(String path) {
        var normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        try (var resource = TestResources.class.getClassLoader().getResourceAsStream(normalizedPath)) {
            if (resource != null) {
                return resource.readAllBytes();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Could not read file " + normalizedPath);
    }
}
