package org.eventplanner.integration.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eventplanner.testdata.SignedInUserFactory.createSignedInUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
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
            assertThat(cellValue(sheet, 2, 0)).isEqualTo("Steve Rogers");
            assertThat(cellValue(sheet, 3, 0)).isEqualTo("Doctor Strange");
            assertThat(cellValue(sheet, 4, 0)).isEqualTo("Bruce Benner");
            assertThat(cellValue(sheet, 5, 0)).isEqualTo("Tony Stark");
            assertThat(cellValue(sheet, 6, 0)).isEqualTo("Natasha Romanoff");
        }
        FileUtils.delete(file);
    }

    @Test
    void shouldGenerateImoList() throws Exception {
        var file = generateExcelExport("imo-crew-list");

        try (XSSFWorkbook workbook = new XSSFWorkbook(file)) {
            var sheet = workbook.getSheetAt(0);
            // check all fields set correctly
            assertThat(cellValue(sheet, 8, 1)).isEqualTo("Rogers");
            assertThat(cellValue(sheet, 8, 2)).isEqualTo("Steven Grant");
            assertThat(cellValue(sheet, 8, 3)).isEqualTo("Master");
            assertThat(cellValue(sheet, 8, 4)).isEqualTo("US");
            assertThat(cellValue(sheet, 8, 6)).isEqualTo("1922-09-28");
            assertThat(cellValue(sheet, 8, 8)).isEqualTo("New York");
            assertThat(cellValue(sheet, 8, 9)).isEqualTo("US28091922");

            // check order of crew members
            assertThat(cellValue(sheet, 9, 1)).isEqualTo("Strange");
            assertThat(cellValue(sheet, 10, 1)).isEqualTo("Benner");
            assertThat(cellValue(sheet, 11, 1)).isEqualTo("Stark");
            assertThat(cellValue(sheet, 12, 1)).isEqualTo("Romanoff");

            // check guest crew
            assertThat(cellValue(sheet, 13, 1)).isEqualTo("Nebula");
            assertThat(cellValue(sheet, 13, 2)).isBlank();
            assertThat(cellValue(sheet, 13, 3)).isEqualTo("Deckshand");
            assertThat(cellValue(sheet, 13, 4)).isBlank();
            assertThat(cellValue(sheet, 13, 6)).isBlank();
            assertThat(cellValue(sheet, 13, 8)).isBlank();
            assertThat(cellValue(sheet, 13, 9)).isBlank();

            // check next line is completely blank
            for (int i = 0; i < 10; i++) {
                assertThat(cellValue(sheet, 14, i)).isBlank();
            }
        }
        FileUtils.delete(file);
    }

    private String cellValue(XSSFSheet sheet, int r, int c) {
        var cell = sheet.getRow(r).getCell(c);
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case BLANK -> "";
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> "";
        };
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
