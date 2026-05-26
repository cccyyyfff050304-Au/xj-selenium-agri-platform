package com.xj.agri.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("field_plot")
public class FieldPlot {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "不能为空")
    private String plotCode;

    @NotBlank(message = "不能为空")
    private String plotName;

    @NotBlank(message = "不能为空")
    private String region;

    private String county;
    private String town;

    @NotNull(message = "不能为空")
    private BigDecimal longitude;

    @NotNull(message = "不能为空")
    private BigDecimal latitude;

    @NotNull(message = "不能为空")
    private BigDecimal areaMu;

    @NotBlank(message = "不能为空")
    private String soilType;

    @NotNull(message = "不能为空")
    @DecimalMin(value = "4.0", message = "不能小于4.0")
    @DecimalMax(value = "10.0", message = "不能大于10.0")
    private BigDecimal phValue;

    @NotNull(message = "不能为空")
    @DecimalMin(value = "0.0", message = "不能小于0")
    private BigDecimal organicMatter;

    @NotNull(message = "不能为空")
    @DecimalMin(value = "0.0", message = "不能小于0")
    private BigDecimal seleniumContent;

    @NotNull(message = "不能为空")
    @DecimalMin(value = "0.0", message = "不能小于0")
    private BigDecimal salinity;

    @NotNull(message = "不能为空")
    @DecimalMin(value = "0.0", message = "不能小于0")
    private BigDecimal electricalConductivity;

    @NotBlank(message = "不能为空")
    private String heavyMetalRisk;

    @NotBlank(message = "不能为空")
    private String riskLevel;

    private String irrigationType;
    private String managerName;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
