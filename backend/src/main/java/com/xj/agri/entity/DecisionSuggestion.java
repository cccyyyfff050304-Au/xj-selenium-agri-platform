package com.xj.agri.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("decision_suggestion")
public class DecisionSuggestion {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long plotId;
    private Long cropRecordId;

    @NotBlank(message = "不能为空")
    private String suggestionType;

    @NotBlank(message = "不能为空")
    private String title;

    @NotBlank(message = "不能为空")
    private String content;

    private String priority;
    private String status;
    private String generatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
