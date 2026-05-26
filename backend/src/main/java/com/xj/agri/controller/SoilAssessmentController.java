package com.xj.agri.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xj.agri.common.ApiResponse;
import com.xj.agri.common.BaseCrudController;
import com.xj.agri.dto.AssessmentRequest;
import com.xj.agri.entity.FieldPlot;
import com.xj.agri.entity.SoilAssessment;
import com.xj.agri.entity.SoilSample;
import com.xj.agri.mapper.FieldPlotMapper;
import com.xj.agri.mapper.SoilAssessmentMapper;
import com.xj.agri.mapper.SoilSampleMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/soil-assessments")
@Tag(name = "土壤环境评估")
public class SoilAssessmentController extends BaseCrudController<SoilAssessment> {
    private final SoilAssessmentMapper assessmentMapper;
    private final FieldPlotMapper fieldPlotMapper;
    private final SoilSampleMapper soilSampleMapper;

    public SoilAssessmentController(SoilAssessmentMapper assessmentMapper,
                                    FieldPlotMapper fieldPlotMapper,
                                    SoilSampleMapper soilSampleMapper) {
        this.assessmentMapper = assessmentMapper;
        this.fieldPlotMapper = fieldPlotMapper;
        this.soilSampleMapper = soilSampleMapper;
    }

    @Override
    protected BaseMapper<SoilAssessment> mapper() {
        return assessmentMapper;
    }

    @Override
    protected QueryWrapper<SoilAssessment> buildQuery(String keyword) {
        QueryWrapper<SoilAssessment> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item.like("risk_level", keyword)
                    .or().like("fertility_level", keyword)
                    .or().like("salinity_level", keyword)
                    .or().like("selenium_level", keyword)
                    .or().like("overall_comment", keyword));
        }
        return wrapper.orderByDesc("created_at", "id");
    }

    @Override
    protected QueryWrapper<SoilAssessment> buildQuery(String keyword, Map<String, String> params) {
        QueryWrapper<SoilAssessment> wrapper = buildQuery(keyword);
        Long plotId = parseLong(params.get("plotId"));
        String riskLevel = params.get("riskLevel");
        wrapper.eq(plotId != null, "plot_id", plotId)
                .eq(StringUtils.hasText(riskLevel), "risk_level", riskLevel);
        return wrapper;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("riskDistribution", assessmentMapper.selectMaps(new QueryWrapper<SoilAssessment>()
                .select("risk_level as riskLevel, count(*) as count")
                .groupBy("risk_level")));
        result.put("plotScores", assessmentMapper.selectMaps(new QueryWrapper<SoilAssessment>()
                .select("plot_id as plotId, max(assessment_score) as score")
                .groupBy("plot_id")
                .orderByDesc("score")
                .last("limit 10")));
        result.put("latest", assessmentMapper.selectList(new QueryWrapper<SoilAssessment>()
                .orderByDesc("created_at")
                .last("limit 10")));
        return ApiResponse.ok(result);
    }

    @PostMapping("/run")
    public ApiResponse<SoilAssessment> run(@Valid @RequestBody AssessmentRequest request,
                                           HttpServletRequest servletRequest) {
        FieldPlot plot = require(fieldPlotMapper.selectById(request.plotId()), "地块不存在");
        SoilSample sample = request.soilSampleId() == null ? latestSample(plot.getId()) : soilSampleMapper.selectById(request.soilSampleId());

        double ph = value(plot.getPhValue(), sample == null ? null : sample.getPhValue(), 7.8);
        double organic = value(plot.getOrganicMatter(), sample == null ? null : sample.getOrganicMatter(), 18.0);
        double selenium = value(plot.getSeleniumContent(), sample == null ? null : sample.getSeleniumContent(), 0.25);
        double salinity = value(plot.getSalinity(), sample == null ? null : sample.getSalinity(), 1.0);
        double conductivity = value(plot.getElectricalConductivity(), sample == null ? null : sample.getElectricalConductivity(), 1.8);
        String heavyMetal = StringUtils.hasText(plot.getHeavyMetalRisk()) ? plot.getHeavyMetalRisk() : sample == null ? "低" : sample.getHeavyMetalRisk();
        if (!StringUtils.hasText(heavyMetal)) {
            heavyMetal = "低";
        }

        double phScore = clamp(100 - Math.abs(ph - 7.4) * 18, 35, 100);
        double organicScore = organic < 12 ? 58 + organic : organic <= 28 ? 92 : organic <= 36 ? 82 : 70;
        double seleniumScore = selenium < 0.12 ? 55 : selenium < 0.18 ? 70 : selenium <= 0.45 ? 92 : 65;
        double salinityScore = clamp(100 - Math.max(0, salinity - 0.8) * 22, 25, 100);
        double conductivityScore = clamp(100 - Math.max(0, conductivity - 1.4) * 18, 25, 100);
        double heavyScore = switch (heavyMetal) {
            case "高" -> 35;
            case "中" -> 62;
            default -> 90;
        };

        double score = phScore * 0.18 + organicScore * 0.18 + seleniumScore * 0.18
                + salinityScore * 0.17 + conductivityScore * 0.14 + heavyScore * 0.15;
        String riskLevel = soilRiskLevel(score, heavyMetal, salinity, conductivity);
        String phLevel = ph >= 6.8 && ph <= 8.2 ? "适宜" : ph < 6.8 ? "偏酸" : "偏碱";
        String fertilityLevel = organic >= 22 ? "高" : organic >= 16 ? "中" : "低";
        String seleniumLevel = selenium >= 0.35 ? "富硒背景" : selenium >= 0.18 ? "中等硒背景" : "低硒背景";
        String salinityLevel = salinity <= 1.3 ? "适宜" : salinity <= 1.8 ? "轻度偏高" : "偏高";
        String conductivityLevel = conductivity <= 2.0 ? "适宜" : conductivity <= 3.0 ? "轻度偏高" : "偏高";

        List<String> constraints = new ArrayList<>();
        if (!"适宜".equals(phLevel)) constraints.add("pH" + phLevel);
        if ("低".equals(fertilityLevel)) constraints.add("有机质不足");
        if ("低硒背景".equals(seleniumLevel)) constraints.add("硒背景偏低");
        if (!"适宜".equals(salinityLevel)) constraints.add("盐分" + salinityLevel);
        if (!"适宜".equals(conductivityLevel)) constraints.add("电导率" + conductivityLevel);
        if (!"低".equals(heavyMetal)) constraints.add("重金属风险" + heavyMetal);

        String itemEvaluation = "{"
                + "\"pH\":{\"value\":" + decimal(ph, 2) + ",\"score\":" + decimal(phScore, 2) + ",\"level\":\"" + phLevel + "\"},"
                + "\"有机质\":{\"value\":" + decimal(organic, 2) + ",\"score\":" + decimal(organicScore, 2) + ",\"level\":\"" + fertilityLevel + "\"},"
                + "\"土壤硒\":{\"value\":" + decimal(selenium, 3) + ",\"score\":" + decimal(seleniumScore, 2) + ",\"level\":\"" + seleniumLevel + "\"},"
                + "\"盐分\":{\"value\":" + decimal(salinity, 2) + ",\"score\":" + decimal(salinityScore, 2) + ",\"level\":\"" + salinityLevel + "\"},"
                + "\"电导率\":{\"value\":" + decimal(conductivity, 2) + ",\"score\":" + decimal(conductivityScore, 2) + ",\"level\":\"" + conductivityLevel + "\"},"
                + "\"重金属\":{\"value\":\"" + heavyMetal + "\",\"score\":" + decimal(heavyScore, 2) + ",\"level\":\"" + heavyMetal + "\"}"
                + "}";
        String radarJson = "[" + decimal(phScore, 2) + "," + decimal(organicScore, 2) + "," + decimal(seleniumScore, 2)
                + "," + decimal(salinityScore, 2) + "," + decimal(conductivityScore, 2) + "," + decimal(heavyScore, 2) + "]";

        SoilAssessment assessment = new SoilAssessment();
        assessment.setPlotId(plot.getId());
        assessment.setSoilSampleId(sample == null ? null : sample.getId());
        assessment.setAssessmentScore(decimal(score, 2));
        assessment.setRiskLevel(riskLevel);
        assessment.setPhLevel(phLevel);
        assessment.setFertilityLevel(fertilityLevel);
        assessment.setSeleniumLevel(seleniumLevel);
        assessment.setSalinityLevel(salinityLevel);
        assessment.setConductivityLevel(conductivityLevel);
        assessment.setHeavyMetalLevel(heavyMetal);
        assessment.setItemEvaluation(itemEvaluation);
        assessment.setRadarJson(radarJson);
        assessment.setConstraintFactor(constraints.isEmpty() ? "暂无显著限制因子" : String.join("；", constraints));
        assessment.setOverallComment(plot.getRegion() + plot.getPlotName() + "综合评分为" + decimal(score, 2)
                + "，土壤环境等级为" + riskLevel + "。");
        assessment.setImprovementAdvice(buildAdvice(riskLevel, phLevel, fertilityLevel, seleniumLevel, salinityLevel, conductivityLevel, heavyMetal));
        assessment.setCreatedBy(String.valueOf(servletRequest.getAttribute("username")));
        assessment.setCreatedAt(LocalDateTime.now());
        assessmentMapper.insert(assessment);
        return ApiResponse.ok("评估完成", assessment);
    }

    private SoilSample latestSample(Long plotId) {
        return soilSampleMapper.selectOne(new QueryWrapper<SoilSample>()
                .eq("plot_id", plotId)
                .orderByDesc("sample_date", "id")
                .last("limit 1"));
    }

    private String soilRiskLevel(double score, String heavyMetal, double salinity, double conductivity) {
        if ("高".equals(heavyMetal) || salinity >= 2.4 || conductivity >= 3.4 || score < 55) {
            return "高风险";
        }
        if (score < 70 || salinity >= 1.9 || conductivity >= 2.8) {
            return "中度风险";
        }
        if (score < 82 || salinity >= 1.4 || conductivity >= 2.2 || "中".equals(heavyMetal)) {
            return "轻度预警";
        }
        return "适宜";
    }

    private String buildAdvice(String riskLevel, String phLevel, String fertilityLevel, String seleniumLevel,
                               String salinityLevel, String conductivityLevel, String heavyMetal) {
        StringBuilder builder = new StringBuilder();
        if (!"适宜".equals(riskLevel)) {
            builder.append("建议分区复检并制定改良计划。");
        }
        if ("偏碱".equals(phLevel)) {
            builder.append("增施腐殖酸类有机肥，配合滴灌淋洗降低碱化影响。");
        }
        if ("低".equals(fertilityLevel)) {
            builder.append("补充有机肥和秸秆还田，提高土壤缓冲能力。");
        }
        if ("低硒背景".equals(seleniumLevel)) {
            builder.append("开展小区富硒调控试验，避免盲目大面积补硒。");
        }
        if (!"适宜".equals(salinityLevel) || !"适宜".equals(conductivityLevel)) {
            builder.append("优先采用膜下滴灌、冬春灌压盐和排水改良。");
        }
        if (!"低".equals(heavyMetal)) {
            builder.append("重金属风险需复检确认，风险未消除前不建议推广食用型富硒产品。");
        }
        if (builder.isEmpty()) {
            builder.append("保持现有水肥制度，按季度监测 pH、盐分和硒含量。");
        }
        return builder.toString();
    }

    private <T> T require(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private double value(BigDecimal first, BigDecimal second, double fallback) {
        if (first != null) {
            return first.doubleValue();
        }
        if (second != null) {
            return second.doubleValue();
        }
        return fallback;
    }

    private BigDecimal decimal(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private Long parseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
