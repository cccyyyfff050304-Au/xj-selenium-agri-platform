package com.xj.agri.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xj.agri.common.BaseCrudController;
import com.xj.agri.entity.OperationLog;
import com.xj.agri.mapper.OperationLogMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operation-logs")
@Tag(name = "操作日志")
public class OperationLogController extends BaseCrudController<OperationLog> {
    private final OperationLogMapper operationLogMapper;

    public OperationLogController(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    protected BaseMapper<OperationLog> mapper() {
        return operationLogMapper;
    }

    @Override
    protected QueryWrapper<OperationLog> buildQuery(String keyword) {
        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item.like("username", keyword)
                    .or().like("module_name", keyword)
                    .or().like("operation_type", keyword));
        }
        return wrapper.orderByDesc("created_at", "id");
    }
}
