package org.eventplanner.events.application.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;

import freemarker.template.TemplateException;

class ExcelEventMatrixExportServiceTest {

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
    void shouldFillExcelTemplateWithData() throws Exception {
        var model = new HashMap<String, Object>();
        model.put("modelvalue", "Value from model");
        var template = new File("src/test/resources/templates/excel/sample.xlsx");
        var outFile = new File("src/test/resources/templates/excel/sample.out.xlsx");
        var out = testee.exportToExcel(template, model);
        try (OutputStream outputStream = new FileOutputStream(outFile)) {
            out.writeTo(outputStream);
        } catch (Exception e) {
            throw e;
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook(outFile)) {
            // template sheet should be removed
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);

            var sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("static text should not be changed");
            assertThat(sheet.getRow(1)
                .getCell(0)
                .getStringCellValue()).isEqualTo("global template function is working");
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("global assigned value");
            assertThat(sheet.getRow(3).getCell(0).getStringCellValue()).isEmpty();
            assertThat(sheet.getRow(4).getCell(0).getStringCellValue()).isEqualTo("Value from model");
            assertThat(sheet.getRow(5).getCell(0).getNumericCellValue()).isEqualTo(42);
            assertThat(sheet.getRow(6).getCell(0).getNumericCellValue()).isEqualTo(43);
            assertThat(sheet.getRow(7).getCell(0).getNumericCellValue()).isEqualTo(44);
            assertThat(sheet.getRow(8).getCell(0).getStringCellValue()).isEqualTo("${invalid}");
            assertThat(sheet.getRow(9).getCell(0).getStringCellValue()).isEmpty();
        }

        FileUtils.delete(outFile);
    }
}
