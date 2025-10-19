package org.eventplanner.events.domain.exceptions;

import org.springframework.lang.NonNull;

public class ExcelExportFailedException extends HandledException {
    public ExcelExportFailedException(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
    }
}
