package org.eventplanner.events.application.services;

import static org.eventplanner.testutil.assertions.Assertions.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;

import freemarker.template.TemplateException;

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
    void shouldFillExcelTemplateWithData() throws Exception {
        var model = new HashMap<String, Object>();
        model.put("modelvalue", "Value from model");
        var template = new File("src/test/resources/templates/excel/sample.xlsx");
        var outFile = new File("src/test/resources/templates/excel/sample.out.xlsx");
        var out = testee.exportToExcel(template, model);
        try (OutputStream outputStream = new FileOutputStream(outFile)) {
            out.writeTo(outputStream);
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook(outFile)) {
            // template sheet should be removed
            Assertions.assertThat(workbook.getNumberOfSheets()).isEqualTo(1);

            var sheet = workbook.getSheetAt(0);
            assertThat(sheet).cell(0, 0).hasValue("static text should not be changed");
            assertThat(sheet).cell(1, 0).hasValue("global template function is working");
            assertThat(sheet).cell(2, 0).hasValue("global assigned value");
            assertThat(sheet).cell(3, 0).isBlank();
            assertThat(sheet).cell(4, 0).hasValue("Value from model");
            assertThat(sheet).cell(5, 0).hasValue(42);
            assertThat(sheet).cell(6, 0).hasValue(43);
            assertThat(sheet).cell(7, 0).hasValue(44);
            assertThat(sheet).cell(8, 0).hasValue("${invalid}");
            assertThat(sheet).cell(9, 0).isBlank();
        }

        FileUtils.delete(outFile);
    }
}
