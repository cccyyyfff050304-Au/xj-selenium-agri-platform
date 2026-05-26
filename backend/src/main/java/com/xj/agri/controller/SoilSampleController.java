package com.xj.agri.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xj.agri.common.ApiResponse;
import com.xj.agri.common.BaseCrudController;
import com.xj.agri.entity.SoilSample;
import com.xj.agri.mapper.SoilSampleMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/soil-samples")
@Tag(name = "土壤样本")
public class SoilSampleController extends BaseCrudController<SoilSample> {
    private final SoilSampleMapper soilSampleMapper;

    public SoilSampleController(SoilSampleMapper soilSampleMapper) {
        this.soilSampleMapper = soilSampleMapper;
    }

    @Override
    protected BaseMapper<SoilSample> mapper() {
        return soilSampleMapper;
    }

    @Override
    protected QueryWrapper<SoilSample> buildQuery(String keyword) {
        QueryWrapper<SoilSample> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item.like("sample_code", keyword)
                    .or().like("sampler", keyword));
        }
        return wrapper.orderByDesc("sample_date", "id");
    }

    @GetMapping("/all")
    public ApiResponse<List<SoilSample>> all() {
        return ApiResponse.ok(soilSampleMapper.selectList(new QueryWrapper<SoilSample>().orderByDesc("sample_date", "id")));
    }
}
