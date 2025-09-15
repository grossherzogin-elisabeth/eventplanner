package org.eventplanner.events.application.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private final Configuration freemarkerConfig;

    public @NonNull ByteArrayOutputStream exportToExcel(
        @NonNull final File template,
        @NonNull final Map<String, Object> model
    ) {
        model.put("currentDate", Instant.now());
        try (FileInputStream fileTemplate = new FileInputStream(template)) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                var sheet = workbook.getSheetAt(i);
                resolveCopies(sheet);
                fillSheet(sheet, model);
            }
            var bos = new ByteArrayOutputStream();
            try (bos) {
                workbook.write(bos);
                bos.flush();
            } catch (IOException e) {
                throw e;
            }
            return bos;
        } catch (TemplateException | IOException e) {
            // TODO throw other exception
            log.error("Failed to create excel export", e);
            throw new RuntimeException(e);
        }
    }

    private void resolveCopies(@NonNull final XSSFSheet sheet) {
        for (var row : sheet) {
            for (var cell : row) {
                if (!cell.getCellType().equals(CellType.STRING)) {
                    continue;
                }
                if (cell.getStringCellValue().equals("..")) {
                    var copyFrom = sheet.getRow(row.getRowNum() - 1).getCell(cell.getColumnIndex());
                    if (copyFrom.getCellType().equals(CellType.STRING)) {
                        cell.setCellValue(copyFrom.getStringCellValue());
                    }
                }
            }
        }
    }

    private void fillSheet(@NonNull final XSSFSheet sheet, @NonNull final Map<String, Object> model)
    throws TemplateException {
        for (var row : sheet) {
            for (var cell : row) {
                renderCellValue(cell, model);
            }
        }
    }

    private void renderCellValue(@NonNull Cell cell, Map<String, Object> model) throws TemplateException {
        if (!cell.getCellType().equals(CellType.STRING)) {
            return;
        }
        var template = cell.getStringCellValue();
        if (template == null || !template.contains("${")) {
            return;
        }
        model.put("row", cell.getRowIndex());
        template = template.replace("#row", String.valueOf(cell.getRowIndex()));
        var rendered = renderString(template, model);
        cell.setCellValue(rendered);
    }

    protected @NonNull String renderString(@NonNull final String template, @NonNull Map<String, Object> model)
    throws TemplateException {
        try {
            var renderer = new Template(template, new StringReader(template), freemarkerConfig);
            var writer = new StringWriter();
            renderer.process(model, writer);
            return writer.toString();
        } catch (IOException e) {
            log.error("Failed to initialize template", e);
            return template;
        }
    }
}
