package com.xj.agri.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xj.agri.common.ApiResponse;
import com.xj.agri.common.BaseCrudController;
import com.xj.agri.entity.CropVariety;
import com.xj.agri.mapper.CropVarietyMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/varieties")
@Tag(name = "作物品种")
public class CropVarietyController extends BaseCrudController<CropVariety> {
    private final CropVarietyMapper cropVarietyMapper;

    public CropVarietyController(CropVarietyMapper cropVarietyMapper) {
        this.cropVarietyMapper = cropVarietyMapper;
    }

    @Override
    protected BaseMapper<CropVariety> mapper() {
        return cropVarietyMapper;
    }

    @Override
    protected QueryWrapper<CropVariety> buildQuery(String keyword) {
        QueryWrapper<CropVariety> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item.like("crop_type", keyword)
                    .or().like("variety_name", keyword)
                    .or().like("suitable_region", keyword));
        }
        return wrapper.orderByDesc("id");
    }

    @GetMapping("/all")
    public ApiResponse<List<CropVariety>> all() {
        return ApiResponse.ok(cropVarietyMapper.selectList(new QueryWrapper<CropVariety>().orderByAsc("crop_type", "id")));
    }
}
