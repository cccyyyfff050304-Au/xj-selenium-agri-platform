package com.xj.agri.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("soil_assessment")
public class SoilAssessment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long plotId;
    private Long soilSampleId;
    private BigDecimal assessmentScore;
    private String riskLevel;
    private String phLevel;
    private String fertilityLevel;
    private String salinityLevel;
    private String seleniumLevel;
    private String conductivityLevel;
    private String heavyMetalLevel;
    private String itemEvaluation;
    private String radarJson;
    private String constraintFactor;
    private String overallComment;
    private String improvementAdvice;
    private String createdBy;
    private LocalDateTime createdAt;
}
