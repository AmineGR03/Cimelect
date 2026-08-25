package com.cimelect.dto.operation;

import com.cimelect.enums.OperationStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull OperationStatus status
) {
}
