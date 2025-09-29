package org.eventplanner.testutil.assertions;

import org.apache.poi.ss.usermodel.Row;
import org.assertj.core.api.AbstractAssert;

public class RowAssert extends AbstractAssert<RowAssert, Row> {

    protected RowAssert(final Row actual) {
        super(actual, RowAssert.class);
    }

    public CellAssert cell(int col) {
        return new CellAssert(actual.getCell(col));
    }

    public RowAssert isBlank() {
        for (var cell : actual) {
            new CellAssert(cell).isBlank();
        }
        return this;
    }
}
