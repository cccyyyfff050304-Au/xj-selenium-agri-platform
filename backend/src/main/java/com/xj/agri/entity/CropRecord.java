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
@TableName("crop_record")
public class CropRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotNull(message = "不能为空")
    private Long plotId;

    @NotNull(message = "不能为空")
    private Long varietyId;

    @NotBlank(message = "不能为空")
    private String cropType;

    @NotBlank(message = "不能为空")
    private String varietyName;

    @NotNull(message = "不能为空")
    private Integer seasonYear;

    @NotNull(message = "不能为空")
    private LocalDate sowingDate;

    private LocalDate harvestDate;
    private LocalDate expectedHarvestDate;
    private BigDecimal plantingAreaMu;

    @NotBlank(message = "不能为空")
    private String irrigationMethod;

    @NotBlank(message = "不能为空")
    private String fertilizerMethod;

    private String irrigationMode;
    private String fertilizerPlan;
    private BigDecimal yieldKgMu;
    private String qualityLevel;
    private String growthStatus;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
