package com.xj.agri.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xj.agri.common.ApiResponse;
import com.xj.agri.common.BaseCrudController;
import com.xj.agri.dto.ReportGenerateRequest;
import com.xj.agri.entity.CropRecord;
import com.xj.agri.entity.DataSourceRecord;
import com.xj.agri.entity.FieldPlot;
import com.xj.agri.entity.ReportRecord;
import com.xj.agri.entity.SeleniumPrediction;
import com.xj.agri.entity.SoilAssessment;
import com.xj.agri.mapper.CropRecordMapper;
import com.xj.agri.mapper.DataSourceRecordMapper;
import com.xj.agri.mapper.FieldPlotMapper;
import com.xj.agri.mapper.ReportRecordMapper;
import com.xj.agri.mapper.SeleniumPredictionMapper;
import com.xj.agri.mapper.SoilAssessmentMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "报告")
public class ReportRecordController extends BaseCrudController<ReportRecord> {
    private final ReportRecordMapper reportRecordMapper;
    private final FieldPlotMapper fieldPlotMapper;
    private final CropRecordMapper cropRecordMapper;
    private final SeleniumPredictionMapper predictionMapper;
    private final SoilAssessmentMapper assessmentMapper;
    private final DataSourceRecordMapper dataSourceMapper;

    public ReportRecordController(ReportRecordMapper reportRecordMapper,
                                  FieldPlotMapper fieldPlotMapper,
                                  CropRecordMapper cropRecordMapper,
                                  SeleniumPredictionMapper predictionMapper,
                                  SoilAssessmentMapper assessmentMapper,
                                  DataSourceRecordMapper dataSourceMapper) {
        this.reportRecordMapper = reportRecordMapper;
        this.fieldPlotMapper = fieldPlotMapper;
        this.cropRecordMapper = cropRecordMapper;
        this.predictionMapper = predictionMapper;
        this.assessmentMapper = assessmentMapper;
        this.dataSourceMapper = dataSourceMapper;
    }

    @Override
    protected BaseMapper<ReportRecord> mapper() {
        return reportRecordMapper;
    }

    @Override
    protected QueryWrapper<ReportRecord> buildQuery(String keyword) {
        QueryWrapper<ReportRecord> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item.like("report_name", keyword).or().like("report_type", keyword));
        }
        return wrapper.orderByDesc("created_at", "id");
    }

    @Override
    protected QueryWrapper<ReportRecord> buildQuery(String keyword, Map<String, String> params) {
        QueryWrapper<ReportRecord> wrapper = buildQuery(keyword);
        String reportType = params.get("reportType");
        wrapper.eq(StringUtils.hasText(reportType), "report_type", reportType);
        return wrapper;
    }

    @PostMapping("/generate")
    public ApiResponse<ReportRecord> generate(@Valid @RequestBody ReportGenerateRequest request,
                                              HttpServletRequest servletRequest) {
        FieldPlot plot = request.relatedPlotId() == null ? null : fieldPlotMapper.selectById(request.relatedPlotId());
        CropRecord crop = plot == null ? null : cropRecordMapper.selectOne(new QueryWrapper<CropRecord>()
                .eq("plot_id", plot.getId())
                .orderByDesc("season_year", "id")
                .last("limit 1"));
        SeleniumPrediction prediction = plot == null ? null : predictionMapper.selectOne(new QueryWrapper<SeleniumPrediction>()
                .eq("plot_id", plot.getId())
                .orderByDesc("created_at")
                .last("limit 1"));
        SoilAssessment assessment = plot == null ? null : assessmentMapper.selectOne(new QueryWrapper<SoilAssessment>()
                .eq("plot_id", plot.getId())
                .orderByDesc("created_at")
                .last("limit 1"));
        ReportRecord report = new ReportRecord();
        report.setReportName(request.reportName());
        report.setReportType(request.reportType());
        report.setRelatedPlotId(request.relatedPlotId());
        report.setFilePath("/reports/" + System.currentTimeMillis() + "-" + request.reportType() + ".html");
        report.setSummary((plot == null ? "全域" : plot.getRegion() + plot.getPlotName())
                + "报告已生成，包含地块、作物、土壤评估、富硒预测、图表摘要、决策建议和数据来源说明。");
        report.setSourceSummary(sourceSummary());
        report.setContent(buildReportContent(request.reportType(), plot, crop, prediction, assessment, report.getSourceSummary()));
        report.setCreatedBy(String.valueOf(servletRequest.getAttribute("username")));
        report.setCreatedAt(LocalDateTime.now());
        reportRecordMapper.insert(report);
        return ApiResponse.ok("报告已生成", report);
    }

    @GetMapping("/{id}/preview")
    public ApiResponse<ReportRecord> preview(@PathVariable Long id) {
        ReportRecord report = reportRecordMapper.selectById(id);
        if (report == null) {
            throw new IllegalArgumentException("报告不存在");
        }
        return ApiResponse.ok(report);
    }

    private String sourceSummary() {
        long simulated = dataSourceMapper.selectCount(new QueryWrapper<DataSourceRecord>().eq("simulated", true));
        long total = dataSourceMapper.selectCount(null);
        return "当前系统登记数据来源 " + total + " 类，其中初始化业务数据 " + simulated + " 类标注为模拟数据。公开来源用于口径参考，非直接抓取。";
    }

    private String buildReportContent(String type, FieldPlot plot, CropRecord crop, SeleniumPrediction prediction,
                                      SoilAssessment assessment, String sourceSummary) {
        StringBuilder html = new StringBuilder();
        html.append("<article class='report-print'>")
                .append("<h1>").append(type).append("</h1>")
                .append("<p>生成时间：").append(LocalDateTime.now()).append("</p>");
        if (plot != null) {
            html.append("<h2>一、基本信息与地块数据</h2>")
                    .append("<p>地块：").append(plot.getRegion()).append(plot.getPlotName())
                    .append("，面积 ").append(plot.getAreaMu()).append(" 亩，土壤类型 ").append(plot.getSoilType())
                    .append("，风险等级 ").append(plot.getRiskLevel()).append("。</p>")
                    .append("<p>pH=").append(plot.getPhValue()).append("，有机质=").append(plot.getOrganicMatter())
                    .append(" g/kg，土壤硒=").append(plot.getSeleniumContent()).append(" mg/kg，盐分=")
                    .append(plot.getSalinity()).append(" g/kg，电导率=").append(plot.getElectricalConductivity())
                    .append(" dS/m，重金属风险=").append(plot.getHeavyMetalRisk()).append("。</p>");
        } else {
            html.append("<h2>一、基本信息</h2><p>本报告为全域汇总报告，覆盖系统内所有示范地块。</p>");
        }
        if (crop != null) {
            html.append("<h2>二、作物数据</h2>")
                    .append("<p>作物：").append(crop.getCropType()).append("，品种：").append(crop.getVarietyName())
                    .append("，播种日期：").append(crop.getSowingDate()).append("，采收日期：").append(crop.getHarvestDate())
                    .append("，灌溉方式：").append(crop.getIrrigationMethod()).append("，施肥方式：").append(crop.getFertilizerMethod())
                    .append("，产量：").append(crop.getYieldKgMu()).append(" kg/亩，品质等级：").append(crop.getQualityLevel()).append("。</p>");
        }
        if (assessment != null) {
            html.append("<h2>三、土壤评估结果</h2>")
                    .append("<p>综合评分：").append(assessment.getAssessmentScore()).append("，风险等级：")
                    .append(assessment.getRiskLevel()).append("。").append(assessment.getOverallComment()).append("</p>")
                    .append("<p>改良建议：").append(assessment.getImprovementAdvice()).append("</p>");
        }
        if (prediction != null) {
            html.append("<h2>四、富硒预测结果</h2>")
                    .append("<p>预测产品硒含量：").append(prediction.getPredictedSelenium()).append(" mg/kg，等级：")
                    .append(prediction.getQualityLevel()).append("，可信度：").append(prediction.getConfidence()).append("。</p>")
                    .append("<p>影响因素：").append(prediction.getFactorExplanation()).append("</p>")
                    .append("<p>建议：").append(prediction.getSuggestion()).append("</p>");
        }
        html.append("<h2>五、图表摘要</h2><p>系统前端提供影响因子贡献柱状图、土壤雷达图、风险等级分布和预测趋势图，报告预览可直接使用浏览器打印或导出 PDF。</p>")
                .append("<h2>六、数据来源说明</h2><p>").append(sourceSummary).append("</p>")
                .append("</article>");
        return html.toString();
    }
}
