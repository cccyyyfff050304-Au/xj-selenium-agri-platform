package com.xj.agri.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("report_record")
public class ReportRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "不能为空")
    private String reportName;

    @NotBlank(message = "不能为空")
    private String reportType;

    private Long relatedPlotId;
    private String filePath;
    private String summary;
    private String content;
    private String sourceSummary;
    private String createdBy;
    private LocalDateTime createdAt;
}
