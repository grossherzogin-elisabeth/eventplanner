package org.eventplanner.events.adapter.filesystem;

import org.eventplanner.events.adapter.EventRepository;
import org.eventplanner.events.entities.Event;
import org.eventplanner.utils.FileSystemJsonRepository;
import org.eventplanner.events.adapter.filesystem.entities.EventJsonEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.List;
import java.util.UUID;

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
    public @NonNull List<Event> findAllByYear(int year) {
        var dir = new File(directory.getPath() + "/" + year);
        return fs.readAllFromDirectory(dir).stream()
            .map(EventJsonEntity::toDomain)
            .sorted(comparing(Event::start))
            .toList();
    }

    @Override
    public @NonNull Event create(@NonNull Event event) {
        var year = event.start().getYear();
        var key = UUID.randomUUID().toString();
        var file = new File(directory.getPath() + "/" + year + "/" + key + ".json");
        fs.writeToFile(file, EventJsonEntity.fromDomain(event));
        return event;
    }

    @Override
    public @NonNull Event update(@NonNull Event event) {
        var year = event.start().getYear();
        var key = UUID.randomUUID().toString();
        var file = new File(directory.getPath() + "/" + year + "/" + key + ".json");
        fs.writeToFile(file, EventJsonEntity.fromDomain(event));
        return event;
    }

    @Override
    public void deleteAllByYear(int year) {
        var dir = new File(directory.getPath() + "/" + year);
        fs.deleteAllInDirectory(dir);
    }
}
