package org.eventplanner.events.application.services;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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

    /**
     * Reads the provided xlsx file and executes freemarker templating on each cell. Cells are rendered starting in A1
     * and then going row by row. All <#assign> and <#function> directives in a cell will be used in all following
     * cells.
     * <br>
     * Cells containing {@code ..} will have the same content as the cell above them, so you can define a table by only
     * writing freemarker expressions in the first row and then using {@code ..} to fill the rest.
     * <br>
     * If there is a sheet named <#template>, all <#assign> and <#function> from this sheet will be used globally to
     * allow writing reusable helper functions.
     *
     * @param template the template file
     * @param model    the data model to use for templating
     * @return ByteArrayOutputStream of the rendered xlsx file
     */
    public @NonNull ByteArrayOutputStream exportToExcel(
        @NonNull final File template,
        @NonNull final Map<String, Object> model
    ) {
        model.put("currentDate", Instant.now());
        try (FileInputStream fileTemplate = new FileInputStream(template)) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileTemplate);
            workbook.setActiveSheet(0);
            var directives = extractGlobalDirectives(workbook);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                var sheet = workbook.getSheetAt(i);
                renderCopies(sheet);
                renderSheet(sheet, model, directives);
            }
            var bos = new ByteArrayOutputStream();
            try (bos) {
                workbook.write(bos);
                bos.flush();
            }
            return bos;
        } catch (TemplateException | IOException e) {
            // TODO throw other exception
            log.error("Failed to create excel export", e);
            throw new RuntimeException(e);
        }
    }

    private @NonNull String extractGlobalDirectives(@NonNull final XSSFWorkbook workbook) {
        for (var i = 0; i < workbook.getNumberOfSheets(); i++) {
            var sheet = workbook.getSheetAt(i);
            if (sheet.getSheetName().equals("<#template>")) {
                var content = new StringBuilder();
                for (var row : sheet) {
                    for (var cell : row) {
                        try {
                            var cellContent = cell.getStringCellValue();
                            var directives = findDirectivesInString(cellContent);
                            content.append(directives);
                        } catch (Exception e) {
                            log.debug("Failed to extract directives from cell content", e);
                        }
                    }
                }
                workbook.removeSheetAt(i);
                return content.toString();
            }
        }
        return "";
    }

    private void renderCopies(@NonNull final XSSFSheet sheet) {
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

    private void renderSheet(
        @NonNull final XSSFSheet sheet,
        @NonNull final Map<String, Object> model,
        @NonNull final String globalDirectives
    )
    throws TemplateException {
        var sheetDirectives = globalDirectives;
        for (var row : sheet) {
            for (var cell : row) {
                try {
                    sheetDirectives = renderCellValue(cell, model, sheetDirectives);
                } catch (Exception e) {
                    if (cell.getCellType().equals(CellType.STRING)) {
                        var expression = cell.getStringCellValue();
                        log.warn(
                            "Failed to render template expression `{}` on sheet {}",
                            expression,
                            sheet.getSheetName()
                        );
                    } else {
                        log.warn(
                            "Failed to render cell {}-{} on sheet {}",
                            cell.getColumnIndex(),
                            cell.getRowIndex(),
                            sheet.getSheetName()
                        );
                    }
                    // template exceptions are quite detailed and we don't want to spam the logs with debugging
                    // information
                    log.debug(e.getMessage(), e);
                }
            }
        }
    }

    private @NonNull String renderCellValue(
        @NonNull Cell cell,
        @NonNull Map<String, Object> model,
        @NonNull final String assigns
    ) throws TemplateException {
        if (!cell.getCellType().equals(CellType.STRING)) {
            return assigns;
        }
        var template = assigns + cell.getStringCellValue();
        if (!template.contains("${") && !template.contains("<#assign")) {
            return assigns;
        }
        model.put("row", cell.getRowIndex());
        var rendered = renderString(template, model);
        if (rendered.isEmpty()) {
            cell.setBlank();
        } else {
            try {
                var dbl = Double.parseDouble(rendered);
                cell.setCellValue(dbl);
            } catch (NumberFormatException e) {
                cell.setCellValue(rendered);
            }
        }
        return findDirectivesInString(template);
    }

    private @NonNull String findDirectivesInString(@NonNull final String template) {
        var directives = new StringBuilder();
        var patterns = List.of(
            Pattern.compile("<#assign [^>]*>"),
            Pattern.compile("<#function [\\S\\s]*<\\/#function>")
        );
        patterns.forEach(pattern -> {
            var matcher = pattern.matcher(template);
            while (matcher.find()) {
                directives.append(matcher.group());
            }
        });
        return directives.toString();
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
