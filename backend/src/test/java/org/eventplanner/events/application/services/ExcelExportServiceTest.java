package org.eventplanner.events.application.services;

import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.PositionKeys.createPosition;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;
import static org.eventplanner.testdata.UserFactory.createUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;

import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.entities.positions.Position;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;

import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

class ExcelExportServiceTest {

    private ExcelExportService testee;

    @BeforeEach
    void setUp() throws TemplateException, IOException {
        var freeMarkerConfig = new FreeMarkerConfigurationFactory().createConfiguration();
        freeMarkerConfig.setDirectoryForTemplateLoading(new File("src/main/resources/templates"));
        testee = new ExcelExportService(
            freeMarkerConfig
        );
    }

    @Test
    void shouldExportToExcel() {
        var template = new File("src/test/resources/templates/excel/imo-list-template.xlsx");
        var crew = new LinkedList<Object>();
        for (int i = 0; i < 50; i++) {
            crew.add(
                new ExportRow(i, createUser(), createPosition(), createRegistration())
            );
        }
        var model = new HashMap<String, Object>();
        model.put("event", createEvent());
        model.put("crew", crew);
        var out = testee.exportToExcel(template, model);
        try (OutputStream outputStream = new FileOutputStream(
            "src/test/resources/templates/excel/imo-list-template.out.xlsx")) {
            out.writeTo(outputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ExportRow {
        private int number;
        private UserDetails user;
        private Position position;
        private Registration registration;
    }
}
