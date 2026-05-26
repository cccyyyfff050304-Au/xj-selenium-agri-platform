package com.xj.agri.dto;

import jakarta.validation.constraints.NotBlank;

public record ReportGenerateRequest(
        @NotBlank(message = "报告名称不能为空") String reportName,
        @NotBlank(message = "报告类型不能为空") String reportType,
        Long relatedPlotId
) {
}
