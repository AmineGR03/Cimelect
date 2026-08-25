package com.cimelect.dto.operation;

import jakarta.validation.constraints.NotBlank;

public record DeleteOperationRequest(
        @NotBlank String justification
) {
}
