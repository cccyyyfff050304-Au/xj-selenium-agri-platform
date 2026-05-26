package com.xj.agri.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("crop_variety")
public class CropVariety {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "不能为空")
    private String cropType;

    @NotBlank(message = "不能为空")
    private String varietyName;

    private Integer seleniumPotential;
    private String stressResistance;
    private String suitableRegion;
    private Integer growthPeriodDays;
    private String remark;
    private LocalDateTime createdAt;
}
