package com.xj.agri.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("soil_sample")
public class SoilSample {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "不能为空")
    private Long plotId;

    @NotBlank(message = "不能为空")
    private String sampleCode;

    @NotNull(message = "不能为空")
    private LocalDate sampleDate;

    @NotNull(message = "不能为空")
    private BigDecimal phValue;

    private BigDecimal organicMatter;
    private BigDecimal availableNitrogen;
    private BigDecimal availablePhosphorus;
    private BigDecimal availablePotassium;
    private BigDecimal seleniumContent;
    private BigDecimal salinity;
    private BigDecimal electricalConductivity;
    private String heavyMetalRisk;
    private String riskLevel;
    private BigDecimal soilMoisture;
    private String sampler;
    private String remark;
    private LocalDateTime createdAt;
}
