package org.eventplanner.integration.export;

import static org.eventplanner.testdata.SignedInUserFactory.createSignedInUser;
import static org.eventplanner.testutil.assertions.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eventplanner.events.application.usecases.EventExportUseCase;
import org.eventplanner.events.domain.values.auth.Role;
import org.eventplanner.events.domain.values.events.EventKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test" })
@AutoConfigureMockMvc
@Transactional // resets db changes after each test
class EventExportIntegrationTest {

    @Autowired
    private EventExportUseCase testee;

    @Test
    void shouldGenerateConsumptionList() throws Exception {
        var file = generateExcelExport("consumption-list");

        try (XSSFWorkbook workbook = new XSSFWorkbook(file)) {
            var sheet = workbook.getSheetAt(0);
            assertThat(sheet).cell(2, 0).hasValue("Steve Rogers");
            assertThat(sheet).cell(3, 0).hasValue("Doctor Strange");
            assertThat(sheet).cell(4, 0).hasValue("Bruce Benner");
            assertThat(sheet).cell(5, 0).hasValue("Tony Stark");
            assertThat(sheet).cell(6, 0).hasValue("Natasha Romanoff");
        }
        FileUtils.delete(file);
    }

    @Test
    void shouldGenerateImoList() throws Exception {
        var file = generateExcelExport("imo-crew-list");

        try (XSSFWorkbook workbook = new XSSFWorkbook(file)) {
            var sheet = workbook.getSheetAt(0);
            // check all fields set correctly
            assertThat(sheet).cell(8, 1).hasValue("Rogers");
            assertThat(sheet).cell(8, 2).hasValue("Steven Grant");
            assertThat(sheet).cell(8, 3).hasValue("Master");
            assertThat(sheet).cell(8, 4).hasValue("US");
            assertThat(sheet).cell(8, 6).hasValue("1922-09-28");
            assertThat(sheet).cell(8, 8).hasValue("New York");
            assertThat(sheet).cell(8, 9).hasValue("US28091922");

            // check order of crew members
            assertThat(sheet).cell(9, 1).hasValue("Strange");
            assertThat(sheet).cell(10, 1).hasValue("Benner");
            assertThat(sheet).cell(11, 1).hasValue("Stark");
            assertThat(sheet).cell(12, 1).hasValue("Romanoff");

            // check guest crew
            assertThat(sheet).cell(13, 1).hasValue("Nebula");
            assertThat(sheet).cell(13, 2).isBlank();
            assertThat(sheet).cell(13, 3).hasValue("Deckshand");
            assertThat(sheet).cell(13, 4).isBlank();
            assertThat(sheet).cell(13, 6).isBlank();
            assertThat(sheet).cell(13, 8).isBlank();
            assertThat(sheet).cell(13, 9).isBlank();

            // check next line is completely blank
            for (int i = 14; i < 30; i++) {
                assertThat(sheet).row(i).isBlank();
            }
        }
        FileUtils.delete(file);
    }

    private File generateExcelExport(String template) {
        var out = testee.exportEvent(
            createSignedInUser().withRole(Role.ADMIN),
            new EventKey("7fa48570-963a-4e95-b72f-acaf70c70a24"),
            template
        );
        var target = new File(template + ".out.xlsx");
        try (OutputStream outputStream = new FileOutputStream(target)) {
            out.writeTo(outputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return target;
    }
}
