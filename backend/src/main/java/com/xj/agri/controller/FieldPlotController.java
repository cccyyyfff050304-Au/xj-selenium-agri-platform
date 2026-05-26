package com.xj.agri.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xj.agri.common.BaseCrudController;
import com.xj.agri.entity.FieldPlot;
import com.xj.agri.mapper.FieldPlotMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/plots")
@Tag(name = "地块信息")
public class FieldPlotController extends BaseCrudController<FieldPlot> {
    private final FieldPlotMapper fieldPlotMapper;

    public FieldPlotController(FieldPlotMapper fieldPlotMapper) {
        this.fieldPlotMapper = fieldPlotMapper;
    }

    @Override
    protected BaseMapper<FieldPlot> mapper() {
        return fieldPlotMapper;
    }

    @Override
    protected QueryWrapper<FieldPlot> buildQuery(String keyword) {
        QueryWrapper<FieldPlot> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item.like("plot_code", keyword)
                    .or().like("plot_name", keyword)
                    .or().like("region", keyword)
                    .or().like("county", keyword));
        }
        return wrapper.orderByDesc("id");
    }

    @Override
    protected QueryWrapper<FieldPlot> buildQuery(String keyword, Map<String, String> params) {
        QueryWrapper<FieldPlot> wrapper = buildQuery(keyword);
        String region = params.get("region");
        String soilType = params.get("soilType");
        String riskLevel = params.get("riskLevel");
        wrapper.eq(StringUtils.hasText(region), "region", region)
                .eq(StringUtils.hasText(soilType), "soil_type", soilType)
                .eq(StringUtils.hasText(riskLevel), "risk_level", riskLevel);
        return wrapper;
    }
}
