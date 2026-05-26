package com.xj.agri.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xj.agri.common.BaseCrudController;
import com.xj.agri.entity.DataSourceRecord;
import com.xj.agri.mapper.DataSourceRecordMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-sources")
@Tag(name = "数据来源")
public class DataSourceController extends BaseCrudController<DataSourceRecord> {
    private final DataSourceRecordMapper dataSourceRecordMapper;

    public DataSourceController(DataSourceRecordMapper dataSourceRecordMapper) {
        this.dataSourceRecordMapper = dataSourceRecordMapper;
    }

    @Override
    protected BaseMapper<DataSourceRecord> mapper() {
        return dataSourceRecordMapper;
    }

    @Override
    protected QueryWrapper<DataSourceRecord> buildQuery(String keyword) {
        QueryWrapper<DataSourceRecord> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item.like("source_name", keyword)
                    .or().like("source_type", keyword)
                    .or().like("data_scope", keyword));
        }
        return wrapper.orderByDesc("id");
    }
}
