package com.xj.agri.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("data_source")
public class DataSourceRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "不能为空")
    private String sourceName;

    private String sourceType;
    private String sourceUrl;
    private String dataScope;
    private LocalDateTime collectionTime;
    private String collectionMethod;
    private Boolean simulated;
    private String remark;
    private LocalDateTime createdAt;
}
