package org.eventplanner.testutil.assertions;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Assertions extends org.assertj.core.api.Assertions {

    public static WorkbookAssert assertThat(XSSFWorkbook actual) {
        return new WorkbookAssert(actual);
    }

    public static SheetAssert assertThat(XSSFSheet actual) {
        return new SheetAssert(actual);
    }

    public static RowAssert assertThat(XSSFRow actual) {
        return new RowAssert(actual);
    }

    public static CellAssert assertThat(XSSFCell actual) {
        return new CellAssert(actual);
    }
}
