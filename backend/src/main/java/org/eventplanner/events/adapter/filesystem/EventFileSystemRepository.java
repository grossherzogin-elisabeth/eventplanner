package org.eventplanner.events.adapter.filesystem;

import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.adapter.filesystem.entities.EventJsonEntity;
import org.eventplanner.events.entities.Event;
import org.eventplanner.events.values.EventKey;
import org.eventplanner.utils.FileSystemJsonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;

@Repository
public class EventFileSystemRepository implements EventRepository {

    private final File directory;
    private final FileSystemJsonRepository<EventJsonEntity> fs;

    public EventFileSystemRepository(@Value("${custom.data-directory}") String dataDirectory) {
        directory = new File(dataDirectory + "/events");
        this.fs = new FileSystemJsonRepository<>(EventJsonEntity.class, directory);
    }

    @Override
    public @NonNull Optional<Event> findByKey(@NonNull EventKey key) {
        for (File year : Optional.ofNullable(directory.listFiles()).orElse(new File[0])) {
            var file = new File(year.getPath() + "/" + key.value() + ".json");
            if (file.exists()) {
                return fs.readFromFile(file).map(EventJsonEntity::toDomain);
            }
        }
        return Optional.empty();
    }

    @Override
    public @NonNull List<Event> findAllByYear(int year) {
        var dir = new File(directory.getPath() + "/" + year);
        return fs.readAllFromDirectory(dir).stream()
            .map(EventJsonEntity::toDomain)
            .sorted(comparing(Event::getStart))
            .toList();
    }

    @Override
    public @NonNull Event create(@NonNull Event event) {
        var year = event.getStart().getYear();
        var key = event.getKey().value();
        var file = new File(directory.getPath() + "/" + year + "/" + key + ".json");
        fs.writeToFile(file, EventJsonEntity.fromDomain(event));
        return event;
    }

    @Override
    public @NonNull Event update(@NonNull Event event) {
        var year = event.getStart().getYear();
        var key = event.getKey().value();
        var file = new File(directory.getPath() + "/" + year + "/" + key + ".json");
        fs.writeToFile(file, EventJsonEntity.fromDomain(event));
        return event;
    }

    @Override
    public void deleteAllByYear(int year) {
        var dir = new File(directory.getPath() + "/" + year);
        fs.deleteAllInDirectory(dir);
    }

    @Override
    public @NonNull void deleteByKey(@NonNull EventKey key) {
        for (File year : Optional.ofNullable(directory.listFiles()).orElse(new File[0])) {
            var file = new File(year.getPath() + "/" + key.value() + ".json");
            if (file.exists()) {
                fs.deleteByKey(year.getName() + "/" + key.value());
            }
        }
    }
}
