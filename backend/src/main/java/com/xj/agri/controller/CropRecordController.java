package com.xj.agri.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xj.agri.common.ApiResponse;
import com.xj.agri.dto.CropRecordView;
import com.xj.agri.entity.CropRecord;
import com.xj.agri.entity.CropVariety;
import com.xj.agri.mapper.CropRecordMapper;
import com.xj.agri.mapper.CropVarietyMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crop-records")
@Tag(name = "作物种植档案")
public class CropRecordController {
    private final CropRecordMapper cropRecordMapper;
    private final CropVarietyMapper cropVarietyMapper;

    public CropRecordController(CropRecordMapper cropRecordMapper, CropVarietyMapper cropVarietyMapper) {
        this.cropRecordMapper = cropRecordMapper;
        this.cropVarietyMapper = cropVarietyMapper;
    }

    @GetMapping("/all")
    public ApiResponse<List<CropRecord>> all() {
        return ApiResponse.ok(cropRecordMapper.selectList(new QueryWrapper<CropRecord>().orderByDesc("season_year", "id")));
    }

    @PostMapping
    public ApiResponse<CropRecord> create(@Valid @RequestBody CropRecord body) {
        normalize(body);
        cropRecordMapper.insert(body);
        return ApiResponse.ok("创建成功", body);
    }

    @PutMapping("/{id}")
    public ApiResponse<CropRecord> update(@PathVariable Long id, @Valid @RequestBody CropRecord body) {
        body.setId(id);
        normalize(body);
        cropRecordMapper.updateById(body);
        return ApiResponse.ok("更新成功", body);
    }

    @GetMapping
    public ApiResponse<Page<CropRecordView>> page(@RequestParam(required = false) Long page,
                                                  @RequestParam(defaultValue = "1") long current,
                                                  @RequestParam(defaultValue = "10") long size,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String cropType,
                                                  @RequestParam(required = false) Long plotId) {
        if (page != null) {
            current = page;
        }
        return ApiResponse.ok(cropRecordMapper.selectViewPage(Page.of(current, size), keyword, cropType, plotId));
    }

    @GetMapping("/{id}")
    public ApiResponse<CropRecord> detail(@PathVariable Long id) {
        CropRecord record = cropRecordMapper.selectById(id);
        if (record == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        return ApiResponse.ok(record);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id) {
        cropRecordMapper.deleteById(id);
        return ApiResponse.ok("删除成功", true);
    }

    private void normalize(CropRecord body) {
        if (!StringUtils.hasText(body.getVarietyName()) && body.getVarietyId() != null) {
            CropVariety variety = cropVarietyMapper.selectById(body.getVarietyId());
            if (variety != null) {
                body.setVarietyName(variety.getVarietyName());
                body.setCropType(variety.getCropType());
            }
        }
        if (!StringUtils.hasText(body.getIrrigationMode())) {
            body.setIrrigationMode(body.getIrrigationMethod());
        }
        if (!StringUtils.hasText(body.getFertilizerPlan())) {
            body.setFertilizerPlan(body.getFertilizerMethod());
        }
        if (body.getExpectedHarvestDate() == null) {
            body.setExpectedHarvestDate(body.getHarvestDate());
        }
    }
}
