package org.eventplanner.testdata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestDb {
    public static void setup() throws IOException {
        log.info("Setting up TestDb");
        var original = new File("src/test/resources/eventplanner-test.db");
        if (!original.exists()) {
            throw new FileNotFoundException("eventplanner-test.db does not exist");
        }
        var copy = new File("data/eventplanner-test.db");
        FileUtils.copyFile(original, copy);
    }
}
