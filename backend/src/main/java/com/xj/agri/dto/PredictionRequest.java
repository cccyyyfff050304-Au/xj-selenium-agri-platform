package com.xj.agri.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PredictionRequest(
        @NotNull(message = "地块不能为空") Long plotId,
        Long cropRecordId,
        Long soilSampleId,
        String cropType,
        String varietyName,
        BigDecimal soilSelenium,
        BigDecimal phValue,
        BigDecimal organicMatter,
        BigDecimal salinity,
        BigDecimal electricalConductivity,
        BigDecimal avgTemperature,
        BigDecimal precipitation,
        String irrigationMethod,
        String fertilizerMethod
) {
}
