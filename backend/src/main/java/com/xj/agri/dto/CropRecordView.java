package com.xj.agri.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CropRecordView {
    private Long id;
    private Long plotId;
    private String plotName;
    private String region;
    private BigDecimal plotSeleniumContent;
    private BigDecimal plotPhValue;
    private String plotRiskLevel;
    private Long varietyId;
    private String cropType;
    private String varietyName;
    private Integer seasonYear;
    private LocalDate sowingDate;
    private LocalDate harvestDate;
    private LocalDate expectedHarvestDate;
    private BigDecimal plantingAreaMu;
    private String irrigationMethod;
    private String fertilizerMethod;
    private BigDecimal yieldKgMu;
    private String qualityLevel;
    private String growthStatus;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
