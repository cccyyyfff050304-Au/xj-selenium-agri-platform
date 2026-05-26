package com.xj.agri.dto;

import jakarta.validation.constraints.NotNull;

public record AssessmentRequest(
        @NotNull(message = "地块不能为空") Long plotId,
        Long soilSampleId
) {
}
