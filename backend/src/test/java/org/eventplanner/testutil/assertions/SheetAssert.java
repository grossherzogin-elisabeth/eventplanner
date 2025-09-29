package org.eventplanner.testutil.assertions;

import org.apache.poi.ss.usermodel.Sheet;
import org.assertj.core.api.AbstractAssert;

public class SheetAssert extends AbstractAssert<SheetAssert, Sheet> {

    protected SheetAssert(final Sheet actual) {
        super(actual, SheetAssert.class);
    }

    public CellAssert cell(int row, int col) {
        return new CellAssert(actual.getRow(row).getCell(col));
    }

    public RowAssert row(int row) {
        return new RowAssert(actual.getRow(row));
    }
}
