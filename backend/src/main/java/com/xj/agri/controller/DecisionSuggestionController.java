package com.xj.agri.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xj.agri.common.ApiResponse;
import com.xj.agri.common.BaseCrudController;
import com.xj.agri.dto.DecisionGenerateRequest;
import com.xj.agri.entity.CropRecord;
import com.xj.agri.entity.DecisionSuggestion;
import com.xj.agri.entity.FieldPlot;
import com.xj.agri.entity.SeleniumPrediction;
import com.xj.agri.entity.SoilAssessment;
import com.xj.agri.entity.SoilSample;
import com.xj.agri.mapper.CropRecordMapper;
import com.xj.agri.mapper.DecisionSuggestionMapper;
import com.xj.agri.mapper.FieldPlotMapper;
import com.xj.agri.mapper.SeleniumPredictionMapper;
import com.xj.agri.mapper.SoilAssessmentMapper;
import com.xj.agri.mapper.SoilSampleMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/decisions")
@Tag(name = "决策建议")
public class DecisionSuggestionController extends BaseCrudController<DecisionSuggestion> {
    private final DecisionSuggestionMapper suggestionMapper;
    private final FieldPlotMapper fieldPlotMapper;
    private final CropRecordMapper cropRecordMapper;
    private final SoilSampleMapper soilSampleMapper;
    private final SeleniumPredictionMapper predictionMapper;
    private final SoilAssessmentMapper assessmentMapper;

    public DecisionSuggestionController(DecisionSuggestionMapper suggestionMapper,
                                        FieldPlotMapper fieldPlotMapper,
                                        CropRecordMapper cropRecordMapper,
                                        SoilSampleMapper soilSampleMapper,
                                        SeleniumPredictionMapper predictionMapper,
                                        SoilAssessmentMapper assessmentMapper) {
        this.suggestionMapper = suggestionMapper;
        this.fieldPlotMapper = fieldPlotMapper;
        this.cropRecordMapper = cropRecordMapper;
        this.soilSampleMapper = soilSampleMapper;
        this.predictionMapper = predictionMapper;
        this.assessmentMapper = assessmentMapper;
    }

    @Override
    protected BaseMapper<DecisionSuggestion> mapper() {
        return suggestionMapper;
    }

    @Override
    protected QueryWrapper<DecisionSuggestion> buildQuery(String keyword) {
        QueryWrapper<DecisionSuggestion> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item.like("suggestion_type", keyword)
                    .or().like("title", keyword)
                    .or().like("priority", keyword));
        }
        return wrapper.orderByDesc("created_at", "id");
    }

    @PostMapping("/generate")
    public ApiResponse<List<DecisionSuggestion>> generate(@Valid @RequestBody DecisionGenerateRequest request,
                                                          HttpServletRequest servletRequest) {
        FieldPlot plot = require(fieldPlotMapper.selectById(request.plotId()), "地块不存在");
        CropRecord record = require(cropRecordMapper.selectById(request.cropRecordId()), "种植档案不存在");
        SoilSample sample = soilSampleMapper.selectOne(new QueryWrapper<SoilSample>()
                .eq("plot_id", plot.getId())
                .orderByDesc("sample_date", "id")
                .last("limit 1"));
        SeleniumPrediction prediction = predictionMapper.selectOne(new QueryWrapper<SeleniumPrediction>()
                .eq("plot_id", plot.getId())
                .eq("crop_type", record.getCropType())
                .orderByDesc("created_at")
                .last("limit 1"));
        SoilAssessment assessment = assessmentMapper.selectOne(new QueryWrapper<SoilAssessment>()
                .eq("plot_id", plot.getId())
                .orderByDesc("created_at")
                .last("limit 1"));

        List<DecisionSuggestion> suggestions = new ArrayList<>();
        suggestions.add(buildSuggestion(plot, record, "富硒种植建议",
                plot.getSeleniumContent().doubleValue() < 0.18 ? "谨慎" : "推荐",
                "土壤硒含量为 " + plot.getSeleniumContent() + " mg/kg，"
                        + (prediction == null ? "尚无最新预测，建议先运行富硒品质预测。" : "最新预测等级为" + prediction.getQualityLevel() + "。")
                        + "建议在关键生育期留样检测，避免外源补硒过量。"));

        String soilPriority = plot.getRiskLevel().contains("风险") ? "风险" : plot.getRiskLevel().contains("预警") ? "谨慎" : "推荐";
        suggestions.add(buildSuggestion(plot, record, "土壤改良建议", soilPriority,
                "地块风险等级为" + plot.getRiskLevel() + "，pH=" + plot.getPhValue()
                        + "，盐分=" + plot.getSalinity() + " g/kg，电导率=" + plot.getElectricalConductivity()
                        + " dS/m。" + (assessment == null ? "建议先完成土壤综合评估。" : assessment.getImprovementAdvice())));

        String waterPriority = plot.getSalinity().doubleValue() > 1.7 || plot.getElectricalConductivity().doubleValue() > 2.6 ? "谨慎" : "推荐";
        suggestions.add(buildSuggestion(plot, record, "灌溉施肥建议", waterPriority,
                "当前方案为" + record.getIrrigationMethod() + "、" + record.getFertilizerMethod()
                        + "。若盐分升高，应提高滴灌频次、降低单次肥液浓度，并采用有机肥改善缓冲能力。"));

        String warningPriority = "高".equals(plot.getHeavyMetalRisk()) || "高风险".equals(plot.getRiskLevel()) ? "风险" : "谨慎";
        suggestions.add(buildSuggestion(plot, record, "风险预警建议", warningPriority,
                "重金属风险为" + plot.getHeavyMetalRisk() + "，土壤样本"
                        + (sample == null ? "不足" : "最近采样日期为" + sample.getSampleDate())
                        + "。风险未解除前不建议扩大食用型富硒产品推广。"));

        String promotePriority = "适宜".equals(plot.getRiskLevel()) && plot.getSeleniumContent().doubleValue() >= 0.18 ? "推荐" : "谨慎";
        suggestions.add(buildSuggestion(plot, record, "适宜推广区域建议", promotePriority,
                plot.getRegion() + "同类" + plot.getSoilType() + "地块可作为" + record.getCropType()
                        + "富硒示范候选区，但需满足 pH、盐分和重金属风险同步达标。"));

        String username = String.valueOf(servletRequest.getAttribute("username"));
        for (DecisionSuggestion suggestion : suggestions) {
            suggestion.setGeneratedBy(username);
            suggestion.setCreatedAt(LocalDateTime.now());
            suggestion.setUpdatedAt(LocalDateTime.now());
            suggestionMapper.insert(suggestion);
        }
        return ApiResponse.ok("建议已生成", suggestions);
    }

    private DecisionSuggestion buildSuggestion(FieldPlot plot, CropRecord record, String type, String priority, String content) {
        DecisionSuggestion suggestion = new DecisionSuggestion();
        suggestion.setPlotId(plot.getId());
        suggestion.setCropRecordId(record.getId());
        suggestion.setSuggestionType(type);
        suggestion.setTitle(plot.getRegion() + record.getCropType() + type);
        suggestion.setContent(content);
        suggestion.setPriority(priority);
        suggestion.setStatus("待处理");
        return suggestion;
    }

    private <T> T require(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
