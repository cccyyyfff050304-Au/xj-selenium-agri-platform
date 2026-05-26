package com.xj.agri.dto;

import jakarta.validation.constraints.NotNull;

public record DecisionGenerateRequest(
        @NotNull(message = "地块不能为空") Long plotId,
        @NotNull(message = "种植档案不能为空") Long cropRecordId
) {
}
