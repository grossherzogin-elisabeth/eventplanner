package org.eventplanner.testdata;

import java.util.Collections;

import org.eventplanner.events.domain.entities.qualifications.Qualification;
import org.eventplanner.events.domain.values.qualifications.QualificationKey;

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
