package org.eventplanner.events.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.EventFactory.createEvent;
import static org.eventplanner.testdata.PositionKeys.createPosition;
import static org.eventplanner.testdata.QualificationFactory.createQualification;
import static org.eventplanner.testdata.RegistrationFactory.createRegistration;
import static org.eventplanner.testdata.UserFactory.createUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eventplanner.events.domain.entities.events.Registration;
import org.eventplanner.events.domain.entities.positions.Position;
import org.eventplanner.events.domain.entities.users.UserDetails;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    @ParameterizedTest
    @ValueSource(strings = { "captain-list.xlsx" })
    void shouldExportToExcel(String file) {
        var template = new File("src/test/resources/templates/excel/" + file);
        var out = testee.exportToExcel(template, generateTestData());
        try (OutputStream outputStream = new FileOutputStream(
            "src/test/resources/templates/excel/out/" + file)) {
            out.writeTo(outputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testRenderString() throws TemplateException {
        var template = """
            ${(
            crew[0].user.qualifications
            ?filter(it -> it.qualificationKey == 'medical-fitness')
            ?filter(it -> it.expiresAt??)
            ?map(it -> it.expiresAt)
            ?first
            )!'k.A.'}
            """.replaceAll(" ", "").replaceAll("\n", "");
        var template2 = "${(crew[0].user.nickName)!(crew[0].user.nickName)!}";
        var result = testee.renderString(template2, generateTestData());
        assertThat(result).isNotEqualTo(template);
    }

    private static Map<String, Object> generateTestData() {
        var crew = new LinkedList<>();
        for (int i = 0; i < 50; i++) {
            var user = createUser().withNickName(null);
            user.addQualification(
                createQualification().withKey(new QualificationKey("medical-fitness-2")),
                Instant.now()
            );
            user.addQualification(createQualification().withKey(new QualificationKey("medical-fitness")));
            user.addQualification(createQualification().withKey(new QualificationKey("funk-src")));
            user.addQualification(createQualification().withKey(new QualificationKey("lissi-deckshand")));
            crew.add(new ExportRow(i, user, createPosition(), createRegistration()));
        }
        var model = new HashMap<String, Object>();
        model.put("event", createEvent());
        model.put("crew", crew);
        return model;
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
