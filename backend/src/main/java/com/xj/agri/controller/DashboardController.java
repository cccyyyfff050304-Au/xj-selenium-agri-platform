package com.xj.agri.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xj.agri.common.ApiResponse;
import com.xj.agri.dto.DashboardStats;
import com.xj.agri.entity.CropRecord;
import com.xj.agri.entity.FieldPlot;
import com.xj.agri.entity.SeleniumPrediction;
import com.xj.agri.entity.SoilAssessment;
import com.xj.agri.mapper.CropRecordMapper;
import com.xj.agri.mapper.DecisionSuggestionMapper;
import com.xj.agri.mapper.FieldPlotMapper;
import com.xj.agri.mapper.SeleniumPredictionMapper;
import com.xj.agri.mapper.SoilAssessmentMapper;
import com.xj.agri.mapper.SoilSampleMapper;
import com.xj.agri.mapper.WeatherRecordMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "首页驾驶舱")
public class DashboardController {
    private final FieldPlotMapper fieldPlotMapper;
    private final CropRecordMapper cropRecordMapper;
    private final SoilSampleMapper soilSampleMapper;
    private final WeatherRecordMapper weatherRecordMapper;
    private final SeleniumPredictionMapper predictionMapper;
    private final SoilAssessmentMapper assessmentMapper;
    private final DecisionSuggestionMapper suggestionMapper;

    public DashboardController(FieldPlotMapper fieldPlotMapper,
                               CropRecordMapper cropRecordMapper,
                               SoilSampleMapper soilSampleMapper,
                               WeatherRecordMapper weatherRecordMapper,
                               SeleniumPredictionMapper predictionMapper,
                               SoilAssessmentMapper assessmentMapper,
                               DecisionSuggestionMapper suggestionMapper) {
        this.fieldPlotMapper = fieldPlotMapper;
        this.cropRecordMapper = cropRecordMapper;
        this.soilSampleMapper = soilSampleMapper;
        this.weatherRecordMapper = weatherRecordMapper;
        this.predictionMapper = predictionMapper;
        this.assessmentMapper = assessmentMapper;
        this.suggestionMapper = suggestionMapper;
    }

    @GetMapping("/stats")
    public ApiResponse<DashboardStats> stats() {
        DashboardStats stats = new DashboardStats(
                fieldPlotMapper.selectCount(null),
                cropRecordMapper.selectCount(null),
                soilSampleMapper.selectCount(null),
                weatherRecordMapper.selectCount(null),
                predictionMapper.selectCount(null),
                suggestionMapper.selectCount(null),
                fieldPlotMapper.selectCount(new QueryWrapper<FieldPlot>()
                        .between("selenium_content", 0.18, 0.45)
                        .notIn("risk_level", "高风险")),
                fieldPlotMapper.selectCount(new QueryWrapper<FieldPlot>()
                        .in("risk_level", "轻度预警", "中度风险", "高风险")),
                fieldPlotMapper.selectMaps(new QueryWrapper<FieldPlot>()
                        .select("region, count(*) as count")
                        .groupBy("region")),
                cropRecordMapper.selectMaps(new QueryWrapper<CropRecord>()
                        .select("crop_type as cropType, count(*) as count")
                        .groupBy("crop_type")),
                predictionMapper.selectMaps(new QueryWrapper<SeleniumPrediction>()
                        .select("quality_level as qualityLevel, count(*) as count")
                        .groupBy("quality_level")),
                assessmentMapper.selectMaps(new QueryWrapper<SoilAssessment>()
                        .select("risk_level as riskLevel, count(*) as count")
                        .groupBy("risk_level")),
                predictionMapper.selectMaps(new QueryWrapper<SeleniumPrediction>()
                        .select("id, crop_type as cropType, predicted_selenium as value, quality_level as qualityLevel, created_at as createdAt")
                        .orderByDesc("created_at")
                        .last("limit 7")),
                fieldPlotMapper.selectMaps(new QueryWrapper<FieldPlot>()
                        .select("id, plot_name as plotName, region, risk_level as riskLevel, salinity, electrical_conductivity as electricalConductivity")
                        .in("risk_level", "轻度预警", "中度风险", "高风险")
                        .orderByDesc("updated_at")
                        .last("limit 6")),
                fieldPlotMapper.selectMaps(new QueryWrapper<FieldPlot>()
                        .select("id, plot_name as plotName, region, selenium_content as seleniumContent, ph_value as phValue, risk_level as riskLevel")
                        .between("selenium_content", 0.18, 0.45)
                        .notIn("risk_level", "高风险")
                        .orderByDesc("selenium_content")
                        .last("limit 8")),
                predictionMapper.selectList(new QueryWrapper<SeleniumPrediction>()
                        .orderByDesc("created_at")
                        .last("limit 7"))
        );
        return ApiResponse.ok(stats);
    }
}
