package com.xj.agri.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String username;
    private String moduleName;
    private String operationType;
    private String requestUri;
    private String requestMethod;
    private String ipAddress;
    private String resultStatus;
    private LocalDateTime createdAt;
}
