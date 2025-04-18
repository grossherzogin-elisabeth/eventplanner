package org.eventplanner.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FileSystemJsonRepository<E> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final String directory;
    private final Class<E> entityClass;
    private final Map<String, List<E>> cache = new HashMap<>();

    public FileSystemJsonRepository(Class<E> e, File directory) {
        this.entityClass = e;
        this.directory = directory.getPath();
    }

    public @NonNull List<E> findAll() {
        var dir = new File(directory);
        return readAllFromDirectory(dir);
    }

    public @NonNull Optional<E> findByKey(@NonNull String key) {
        var file = new File(directory + "/" + key + ".json");
        return readFromFile(file);
    }

    public @NonNull E save(@NonNull String key, @NonNull E entity) {
        var file = new File(directory + "/" + key + ".json");
        writeToFile(file, entity);
        cache.remove(directory);
        return entity;
    }

    public void deleteByKey(@NonNull String key) {
        var file = new File(directory + "/" + key + ".json");
        if (!file.exists()) {
            return;
        }
        if (!file.delete()) {
            log.warn("Failed to delete file {}", file.getPath());
        }
        cache.remove(directory);
    }

    public void deleteAll() {
        var dir = new File(directory);
        deleteAllInDirectory(dir);
    }

    public void deleteAllInDirectory(@NonNull File dir) {
        if (!dir.exists()) {
            return;
        }
        var files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            if (!file.delete()) {
                log.warn("Failed to delete files in directory {}", file.getPath());
            }
        }
        cache.remove(dir.getAbsolutePath());
    }

    public @NonNull List<E> readAllFromDirectory(@NonNull File dir) {
        if (cache.containsKey(dir.getAbsolutePath())) {
            return cache.get(dir.getAbsolutePath());
        }
        if (!dir.exists() || !dir.isDirectory()) {
            return Collections.emptyList();
        }
        var files = dir.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        var entities = new ArrayList<E>();
        for (File file : files) {
            if (file.isDirectory()) {
                entities.addAll(readAllFromDirectory(file));
            } else if (file.getName().endsWith(".json")) {
                readFromFile(file).ifPresent(entities::add);
            }
        }

        cache.put(dir.getAbsolutePath(), entities);
        return entities;
    }

    public @NonNull Optional<E> readFromFile(@NonNull File file) {
        if (!file.exists()) {
            return Optional.empty();
        }
        try {
            var json = Files.readString(file.toPath());
            var entity = fromJson(json);
            return Optional.of(entity);
        } catch (Exception e) {
            log.error("Failed to read user from json file", e);
        }
        return Optional.empty();
    }

    public void writeToFile(@NonNull File file, @NonNull E entity) {
        new File(file.getParent()).mkdirs();
        var json = toJson(entity);
        try {
            Files.writeString(file.toPath(), json);
            cache.remove(file.getParentFile().getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write event to file");
        }
    }

    protected String toJson(E entity) {
        try {
            return new ObjectMapper().writeValueAsString(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to json", e);
        }
    }

    protected E fromJson(String json) {
        try {
            return new ObjectMapper().readValue(json, entityClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse json", e);
        }
    }
}
