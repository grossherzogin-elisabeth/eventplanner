package org.eventplanner.importer.service;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class ExcelUtils {

    public static String[][] readExcelFile(@NonNull File file) throws IOException {
        return readExcelFile(file, null);
    }

    public static String[][] readExcelFile(@NonNull File file, @Nullable String password) throws IOException {
        if (!file.exists()) {
            return new String[][]{};
        }
        try (InputStream in = new FileInputStream(file)) {
            return readExcelFile(in, password);
        }
    }

    public static String[][] readExcelFile(@NonNull InputStream in, @Nullable String password) throws IOException {
        XSSFWorkbook workbook;
        if (password != null && !password.isBlank()) {
            workbook = (XSSFWorkbook) WorkbookFactory.create(in, password);
        } else {
            workbook = new XSSFWorkbook(in);
        }

        var sheet = workbook.getSheetAt(0);

        int colCount = 5;
        while (!getCellValueAsString(sheet, 0, colCount).isBlank()) {
            colCount++;
        }

        var rowCount = 3;
        while (!getCellValueAsString(sheet, rowCount, 0).isBlank()) {
            rowCount++;
        }

        String[][] cells = new String[colCount][rowCount];
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                cells[c][r] = getCellValueAsString(sheet, r, c);
            }
        }
        workbook.close();
        return cells;
    }

    private static String getCellValueAsString(XSSFSheet sheet, int r, int c) {
        var row = sheet.getRow(r);
        if (row == null) {
            return "";
        }
        var cell = row.getCell(c);
        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    public static Optional<ZonedDateTime> parseExcelDate(String value) {
        try {
            var daysSince1900 = (int) Double.parseDouble(value.trim());
            var date = DateUtil.getJavaDate(daysSince1900, false).toInstant().atZone(ZoneId.of("Europe/Berlin"));
            return Optional.of(date);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
