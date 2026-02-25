package com.project.inventory.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String errorr,
        String message,
        List<String> details
) {
}
