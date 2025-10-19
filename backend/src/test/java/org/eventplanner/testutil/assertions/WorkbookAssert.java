package org.eventplanner.testutil.assertions;

import org.apache.poi.ss.usermodel.Workbook;
import org.assertj.core.api.AbstractAssert;

public class WorkbookAssert extends AbstractAssert<WorkbookAssert, Workbook> {

    protected WorkbookAssert(final Workbook actual) {
        super(actual, WorkbookAssert.class);
    }

    public WorkbookAssert hasSheet(String sheetName) {
        if (actual.getSheet(sheetName) == null) {
            failWithMessage("Expected workbook to have sheet with name " + sheetName + " but has not");
        }
        return this;
    }
}
