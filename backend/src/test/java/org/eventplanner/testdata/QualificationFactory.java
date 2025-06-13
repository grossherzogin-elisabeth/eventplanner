package org.eventplanner.testdata;

import java.util.Collections;

import org.eventplanner.events.domain.entities.Qualification;
import org.eventplanner.events.domain.values.QualificationKey;

public class QualificationFactory {
    public static Qualification createQualification() {
        return new Qualification(
            new QualificationKey(),
            "Lorem ipsum",
            "icon",
            "Lorem ipsum dolor sit amet",
            true,
            Collections.emptyList()
        );
    }
}
