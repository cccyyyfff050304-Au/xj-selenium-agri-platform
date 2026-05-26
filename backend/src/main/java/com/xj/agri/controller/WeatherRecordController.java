package com.xj.agri.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xj.agri.common.ApiResponse;
import com.xj.agri.common.BaseCrudController;
import com.xj.agri.entity.WeatherRecord;
import com.xj.agri.mapper.WeatherRecordMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weather-records")
@Tag(name = "气象数据")
public class WeatherRecordController extends BaseCrudController<WeatherRecord> {
    private final WeatherRecordMapper weatherRecordMapper;

    public WeatherRecordController(WeatherRecordMapper weatherRecordMapper) {
        this.weatherRecordMapper = weatherRecordMapper;
    }

    @Override
    protected BaseMapper<WeatherRecord> mapper() {
        return weatherRecordMapper;
    }

    @Override
    protected QueryWrapper<WeatherRecord> buildQuery(String keyword) {
        QueryWrapper<WeatherRecord> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like("region", keyword);
        }
        return wrapper.orderByDesc("record_date", "id");
    }

    @GetMapping("/regions")
    public ApiResponse<List<Map<String, Object>>> regions() {
        return ApiResponse.ok(weatherRecordMapper.selectMaps(new QueryWrapper<WeatherRecord>()
                .select("region")
                .groupBy("region")
                .orderByAsc("region")));
    }
}
