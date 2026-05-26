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
@TableName("weather_record")
public class WeatherRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "不能为空")
    private String region;

    @NotNull(message = "不能为空")
    private LocalDate recordDate;

    private BigDecimal avgTemp;
    private BigDecimal maxTemp;
    private BigDecimal minTemp;
    private BigDecimal precipitation;
    private BigDecimal sunshineHours;
    private BigDecimal solarRadiation;
    private BigDecimal windSpeed;
    private BigDecimal humidity;
    private LocalDateTime createdAt;
}
