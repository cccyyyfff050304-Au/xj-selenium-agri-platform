package com.xj.agri.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("selenium_prediction")
public class SeleniumPrediction {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long plotId;
    private Long cropRecordId;
    private Long soilSampleId;
    private String cropType;
    private String varietyName;
    private BigDecimal soilSelenium;
    private BigDecimal phValue;
    private BigDecimal organicMatter;
    private BigDecimal salinity;
    private BigDecimal electricalConductivity;
    private BigDecimal avgTemperature;
    private BigDecimal precipitation;
    private String irrigationMethod;
    private String fertilizerMethod;
    private BigDecimal predictedSelenium;
    private String qualityLevel;
    private BigDecimal confidence;
    private String riskLevel;
    private String modelVersion;
    private String factorContribution;
    private String factorExplanation;
    private String suggestion;
    private String inputSignature;
    private String createdBy;
    private LocalDateTime createdAt;
}
