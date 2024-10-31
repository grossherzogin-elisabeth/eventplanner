package org.eventplanner.events.services;

import org.eventplanner.events.entities.Event;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class ImoListService {
    public @NonNull File generateImoList(@NonNull Event event) throws IOException {
        var file = File.createTempFile(event.getKey() + "-imo-list", "txt");
        Files.writeString(file.toPath(), "Hello world");
        file.deleteOnExit();
        // TODO generate the excel
        return file;
    }
}
