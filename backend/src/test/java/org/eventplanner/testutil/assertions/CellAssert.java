package org.eventplanner.testutil.assertions;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.assertj.core.api.AbstractAssert;

public class CellAssert extends AbstractAssert<CellAssert, Cell> {

    protected CellAssert(final Cell actual) {
        super(actual, CellAssert.class);
    }

    public CellAssert isBlank() {
        if (actual == null) {
            // null counts as blank
            return this;
        }
        String stringValue = switch (actual.getCellType()) {
            case CellType.STRING -> actual.getStringCellValue();
            case CellType.NUMERIC -> String.valueOf(actual.getNumericCellValue());
            case CellType.BOOLEAN -> String.valueOf(actual.getBooleanCellValue());
            case CellType.BLANK -> "";
            case CellType.FORMULA -> "";
            default -> "unmapped cell type " + actual.getCellType();
        };
        if (!stringValue.isBlank()) {
            failWithMessage("Expecting actual to be blank but was " + stringValue);
        }
        return this;
    }

    public CellAssert isNotBlank() {
        isNotNull();
        String stringValue = switch (actual.getCellType()) {
            case CellType.STRING -> actual.getStringCellValue();
            case CellType.NUMERIC -> String.valueOf(actual.getNumericCellValue());
            case CellType.BOOLEAN -> String.valueOf(actual.getBooleanCellValue());
            default -> "";
        };
        if (stringValue.isBlank()) {
            failWithMessage("Expecting actual not to be not blank");
        }
        return this;
    }

    public CellAssert hasCellType(CellType expected) {
        isNotNull();
        var is = actual.getCellType();
        if (!expected.equals(is)) {
            failWithMessage(" Expecting actual to have cell type " + expected + " but was " + is);
        }
        return this;
    }

    public CellAssert hasValue(String expected) {
        hasCellType(CellType.STRING);
        try {
            var is = actual.getStringCellValue();
            if (!is.equals(expected)) {
                failWithMessage("Expecting actual to have value " + expected + " but was " + is);
            }
        } catch (Exception e) {
            failWithMessage("Expecting actual to have value " + expected + " but has no string value");
        }
        return this;
    }

    public CellAssert hasValue(int expected) {
        return hasValue((double) expected);
    }

    public CellAssert hasValue(double expected) {
        hasCellType(CellType.NUMERIC);
        try {
            var is = actual.getNumericCellValue();
            if (is != expected) {
                failWithMessage("Expecting actual to have value " + expected + " but was " + is);
            }
        } catch (Exception e) {
            failWithMessage("Expecting actual to have value " + expected + " but has no numeric value");
        }
        return this;
    }

    public CellAssert hasFillBackgroundColor(String rgbHex) {
        isNotNull();
        var expected = rgbHex.replace("#", "ff").toUpperCase();
        var is = actual.getCellStyle().getFillBackgroundColorColor();
        if (is instanceof XSSFColor xssfColor && !xssfColor.getARGBHex().equals(expected)) {
            failWithMessage("Expecting actual to have color " + expected + " but was " + xssfColor.getARGBHex());
        }
        return this;
    }

    @Override
    protected void failWithMessage(String errorMessage, Object... arguments) {
        if (actual != null) {
            errorMessage = errorMessage.replace(
                "Expecting actual",
                "Expecting cell (" + actual.getRowIndex() + ", " + actual.getColumnIndex() + ")"
            );
        }
        super.failWithMessage(errorMessage, arguments);
    }
}
