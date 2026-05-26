package com.xj.agri.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xj.agri.common.ApiResponse;
import com.xj.agri.common.BaseCrudController;
import com.xj.agri.dto.PredictionRequest;
import com.xj.agri.entity.CropRecord;
import com.xj.agri.entity.CropVariety;
import com.xj.agri.entity.FieldPlot;
import com.xj.agri.entity.SeleniumPrediction;
import com.xj.agri.entity.SoilSample;
import com.xj.agri.entity.WeatherRecord;
import com.xj.agri.mapper.CropRecordMapper;
import com.xj.agri.mapper.CropVarietyMapper;
import com.xj.agri.mapper.FieldPlotMapper;
import com.xj.agri.mapper.SeleniumPredictionMapper;
import com.xj.agri.mapper.SoilSampleMapper;
import com.xj.agri.mapper.WeatherRecordMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
@Tag(name = "富硒品质预测")
public class SeleniumPredictionController extends BaseCrudController<SeleniumPrediction> {
    private static final String MODEL_VERSION = "XJ-Se-Explainable-0.2";

    private final SeleniumPredictionMapper predictionMapper;
    private final FieldPlotMapper fieldPlotMapper;
    private final CropRecordMapper cropRecordMapper;
    private final CropVarietyMapper cropVarietyMapper;
    private final SoilSampleMapper soilSampleMapper;
    private final WeatherRecordMapper weatherRecordMapper;

    public SeleniumPredictionController(SeleniumPredictionMapper predictionMapper,
                                        FieldPlotMapper fieldPlotMapper,
                                        CropRecordMapper cropRecordMapper,
                                        CropVarietyMapper cropVarietyMapper,
                                        SoilSampleMapper soilSampleMapper,
                                        WeatherRecordMapper weatherRecordMapper) {
        this.predictionMapper = predictionMapper;
        this.fieldPlotMapper = fieldPlotMapper;
        this.cropRecordMapper = cropRecordMapper;
        this.cropVarietyMapper = cropVarietyMapper;
        this.soilSampleMapper = soilSampleMapper;
        this.weatherRecordMapper = weatherRecordMapper;
    }

    @Override
    protected BaseMapper<SeleniumPrediction> mapper() {
        return predictionMapper;
    }

    @Override
    protected QueryWrapper<SeleniumPrediction> buildQuery(String keyword) {
        QueryWrapper<SeleniumPrediction> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item.like("crop_type", keyword)
                    .or().like("variety_name", keyword)
                    .or().like("quality_level", keyword)
                    .or().like("risk_level", keyword)
                    .or().like("created_by", keyword));
        }
        return wrapper.orderByDesc("created_at", "id");
    }

    @Override
    protected QueryWrapper<SeleniumPrediction> buildQuery(String keyword, Map<String, String> params) {
        QueryWrapper<SeleniumPrediction> wrapper = buildQuery(keyword);
        String cropType = params.get("cropType");
        String qualityLevel = params.get("qualityLevel");
        Long plotId = parseLong(params.get("plotId"));
        wrapper.eq(StringUtils.hasText(cropType), "crop_type", cropType)
                .eq(plotId != null, "plot_id", plotId)
                .eq(StringUtils.hasText(qualityLevel), "quality_level", qualityLevel);
        return wrapper;
    }

    @PostMapping("/run")
    public ApiResponse<SeleniumPrediction> run(@Valid @RequestBody PredictionRequest request,
                                               HttpServletRequest servletRequest) {
        FieldPlot plot = require(fieldPlotMapper.selectById(request.plotId()), "地块不存在");
        CropRecord record = request.cropRecordId() == null ? null : cropRecordMapper.selectById(request.cropRecordId());
        SoilSample sample = request.soilSampleId() == null ? latestSample(plot.getId()) : soilSampleMapper.selectById(request.soilSampleId());
        CropVariety variety = record == null || record.getVarietyId() == null ? null : cropVarietyMapper.selectById(record.getVarietyId());

        String cropType = firstText(request.cropType(), record == null ? null : record.getCropType(), "棉花");
        String varietyName = firstText(request.varietyName(),
                record == null ? null : record.getVarietyName(),
                variety == null ? null : variety.getVarietyName(),
                "示范品种");
        BigDecimal soilSelenium = firstNumber(request.soilSelenium(), plot.getSeleniumContent(), sample == null ? null : sample.getSeleniumContent(), BigDecimal.valueOf(0.22));
        BigDecimal phValue = firstNumber(request.phValue(), plot.getPhValue(), sample == null ? null : sample.getPhValue(), BigDecimal.valueOf(7.8));
        BigDecimal organicMatter = firstNumber(request.organicMatter(), plot.getOrganicMatter(), sample == null ? null : sample.getOrganicMatter(), BigDecimal.valueOf(18.0));
        BigDecimal salinity = firstNumber(request.salinity(), plot.getSalinity(), sample == null ? null : sample.getSalinity(), BigDecimal.valueOf(1.1));
        BigDecimal conductivity = firstNumber(request.electricalConductivity(), plot.getElectricalConductivity(), sample == null ? null : sample.getElectricalConductivity(), BigDecimal.valueOf(1.8));
        WeatherSummary weather = weatherSummary(plot.getRegion());
        BigDecimal avgTemperature = firstNumber(request.avgTemperature(), weather.avgTemperature(), BigDecimal.valueOf(22.0));
        BigDecimal precipitation = firstNumber(request.precipitation(), weather.precipitation(), BigDecimal.valueOf(5.0));
        String irrigationMethod = firstText(request.irrigationMethod(), record == null ? null : record.getIrrigationMethod(), plot.getIrrigationType(), "滴灌");
        String fertilizerMethod = firstText(request.fertilizerMethod(), record == null ? null : record.getFertilizerMethod(), "有机肥+氮磷钾平衡追肥");

        double cropCoefficient = cropCoefficient(cropType);
        double seFactor = soilSelenium.doubleValue();
        double phFactor = clamp(1.08 - Math.abs(phValue.doubleValue() - 7.2) * 0.08, 0.65, 1.08);
        double organicFactor = organicFactor(organicMatter.doubleValue());
        double saltFactor = clamp(1.0 - Math.max(0, salinity.doubleValue() - 1.2) * 0.08
                - Math.max(0, conductivity.doubleValue() - 1.8) * 0.05, 0.55, 1.0);
        double climateFactor = climateFactor(cropType, avgTemperature.doubleValue(), precipitation.doubleValue());
        double managementFactor = managementFactor(irrigationMethod, fertilizerMethod);

        double predicted = seFactor * 0.22 * cropCoefficient * phFactor * organicFactor * saltFactor * climateFactor * managementFactor;
        predicted = clamp(predicted, 0.015, 0.135);
        String level = qualityLevel(predicted);
        String risk = riskLevel(level, salinity.doubleValue(), conductivity.doubleValue(), phValue.doubleValue());
        double confidence = clamp(0.70 + completenessScore(request) + (1 - Math.abs(phFactor - 1)) * 0.06
                + saltFactor * 0.06 + climateFactor * 0.05, 0.70, 0.96);

        String contribution = factorContributionJson(soilSelenium.doubleValue(), phFactor, organicFactor, saltFactor, climateFactor, managementFactor);
        String explanation = "土壤硒含量是主导因子；pH 越接近 6.8-8.2，吸收效率越高；有机质处于 16-28 g/kg 时促进根系吸收；盐分和电导率升高会抑制硒吸收；气温与降水共同影响生长适宜性。";
        String suggestion = buildSuggestion(plot, cropType, level, risk, salinity.doubleValue(), conductivity.doubleValue(), phValue.doubleValue(), soilSelenium.doubleValue(), irrigationMethod, fertilizerMethod);

        SeleniumPrediction prediction = new SeleniumPrediction();
        prediction.setPlotId(plot.getId());
        prediction.setCropRecordId(record == null ? null : record.getId());
        prediction.setSoilSampleId(sample == null ? null : sample.getId());
        prediction.setCropType(cropType);
        prediction.setVarietyName(varietyName);
        prediction.setSoilSelenium(scale(soilSelenium, 3));
        prediction.setPhValue(scale(phValue, 2));
        prediction.setOrganicMatter(scale(organicMatter, 2));
        prediction.setSalinity(scale(salinity, 2));
        prediction.setElectricalConductivity(scale(conductivity, 2));
        prediction.setAvgTemperature(scale(avgTemperature, 2));
        prediction.setPrecipitation(scale(precipitation, 2));
        prediction.setIrrigationMethod(irrigationMethod);
        prediction.setFertilizerMethod(fertilizerMethod);
        prediction.setPredictedSelenium(decimal(predicted, 4));
        prediction.setQualityLevel(level);
        prediction.setConfidence(decimal(confidence, 4));
        prediction.setRiskLevel(risk);
        prediction.setModelVersion(MODEL_VERSION);
        prediction.setFactorContribution(contribution);
        prediction.setFactorExplanation(explanation);
        prediction.setSuggestion(suggestion);
        prediction.setInputSignature(signature(plot.getId(), cropType, varietyName, soilSelenium, phValue, organicMatter, salinity, conductivity, avgTemperature, precipitation, irrigationMethod, fertilizerMethod));
        prediction.setCreatedBy(String.valueOf(servletRequest.getAttribute("username")));
        prediction.setCreatedAt(LocalDateTime.now());
        predictionMapper.insert(prediction);
        return ApiResponse.ok("预测完成", prediction);
    }

    private SoilSample latestSample(Long plotId) {
        return soilSampleMapper.selectOne(new QueryWrapper<SoilSample>()
                .eq("plot_id", plotId)
                .orderByDesc("sample_date", "id")
                .last("limit 1"));
    }

    private WeatherSummary weatherSummary(String region) {
        List<WeatherRecord> records = weatherRecordMapper.selectList(new QueryWrapper<WeatherRecord>()
                .eq("region", region)
                .orderByDesc("record_date")
                .last("limit 7"));
        if (records.isEmpty()) {
            return new WeatherSummary(BigDecimal.valueOf(22.0), BigDecimal.valueOf(5.0));
        }
        double avgTemp = records.stream().map(WeatherRecord::getAvgTemp).filter(v -> v != null).mapToDouble(BigDecimal::doubleValue).average().orElse(22.0);
        double precipitation = records.stream().map(WeatherRecord::getPrecipitation).filter(v -> v != null).mapToDouble(BigDecimal::doubleValue).average().orElse(5.0);
        return new WeatherSummary(decimal(avgTemp, 2), decimal(precipitation, 2));
    }

    private double cropCoefficient(String cropType) {
        return switch (cropType) {
            case "棉花" -> 0.55;
            case "小麦" -> 0.90;
            case "玉米" -> 0.82;
            case "葡萄" -> 1.15;
            case "枸杞" -> 1.20;
            case "番茄" -> 1.05;
            default -> 0.85;
        };
    }

    private double organicFactor(double organicMatter) {
        if (organicMatter < 12) {
            return 0.88;
        }
        if (organicMatter <= 28) {
            return 1.08;
        }
        if (organicMatter <= 36) {
            return 1.02;
        }
        return 0.96;
    }

    private double climateFactor(String cropType, double temperature, double precipitation) {
        double idealTemperature = switch (cropType) {
            case "小麦" -> 20.0;
            case "葡萄", "番茄" -> 25.0;
            case "枸杞" -> 24.0;
            default -> 23.0;
        };
        double tempFactor = clamp(1.0 - Math.abs(temperature - idealTemperature) * 0.025, 0.70, 1.05);
        double rainFactor = precipitation < 1 ? 0.92 : precipitation > 18 ? 0.90 : 1.02;
        return clamp(tempFactor * rainFactor, 0.65, 1.06);
    }

    private double managementFactor(String irrigationMethod, String fertilizerMethod) {
        double factor = 1.0;
        if (irrigationMethod != null && irrigationMethod.contains("滴灌")) {
            factor += 0.04;
        }
        if (irrigationMethod != null && irrigationMethod.contains("水肥一体化")) {
            factor += 0.04;
        }
        if (fertilizerMethod != null && fertilizerMethod.contains("有机")) {
            factor += 0.03;
        }
        if (fertilizerMethod != null && fertilizerMethod.contains("富硒")) {
            factor += 0.08;
        }
        return clamp(factor, 0.92, 1.16);
    }

    private String qualityLevel(double predicted) {
        if (predicted < 0.035) {
            return "低硒";
        }
        if (predicted < 0.060) {
            return "普通";
        }
        if (predicted < 0.100) {
            return "富硒";
        }
        return "高硒风险";
    }

    private String riskLevel(String level, double salinity, double conductivity, double ph) {
        if ("高硒风险".equals(level) || salinity >= 2.4 || conductivity >= 3.4 || ph >= 8.8 || ph <= 5.8) {
            return "风险";
        }
        if (salinity >= 1.7 || conductivity >= 2.6 || ph >= 8.4 || "低硒".equals(level)) {
            return "谨慎";
        }
        return "推荐";
    }

    private String factorContributionJson(double soilSelenium, double phFactor, double organicFactor, double saltFactor, double climateFactor, double managementFactor) {
        int selenium = (int) Math.round(clamp(38 + soilSelenium * 18, 38, 52));
        int ph = (int) Math.round(phFactor * 15);
        int organic = (int) Math.round(organicFactor * 13);
        int salt = (int) Math.round(saltFactor * 14);
        int climate = (int) Math.round(climateFactor * 12);
        int management = (int) Math.round(managementFactor * 9);
        int total = selenium + ph + organic + salt + climate + management;
        return "["
                + jsonFactor("土壤硒含量", selenium, total) + ","
                + jsonFactor("pH", ph, total) + ","
                + jsonFactor("有机质", organic, total) + ","
                + jsonFactor("盐分/电导率", salt, total) + ","
                + jsonFactor("气象适宜性", climate, total) + ","
                + jsonFactor("水肥管理", management, total)
                + "]";
    }

    private String jsonFactor(String factor, int value, int total) {
        return "{\"factor\":\"" + factor + "\",\"contribution\":" + Math.round(value * 100.0 / total) + "}";
    }

    private String buildSuggestion(FieldPlot plot, String cropType, String level, String risk, double salinity, double conductivity,
                                   double ph, double soilSelenium, String irrigationMethod, String fertilizerMethod) {
        StringBuilder builder = new StringBuilder();
        builder.append(plot.getRegion()).append(plot.getPlotName()).append("种植").append(cropType)
                .append("预测等级为").append(level).append("，管理等级为").append(risk).append("。");
        if (soilSelenium < 0.18) {
            builder.append("土壤硒背景偏低，建议优先建设小区试验并采用叶面补硒。");
        } else if ("高硒风险".equals(level)) {
            builder.append("预测硒含量偏高，建议降低外源补硒强度并加强采收前检测。");
        } else {
            builder.append("土壤硒背景可支撑富硒品质形成，建议保持关键生育期监测。");
        }
        if (salinity > 1.7 || conductivity > 2.6) {
            builder.append("盐分或电导率偏高，应先通过").append(irrigationMethod).append("压盐控盐，再执行").append(fertilizerMethod).append("。");
        }
        if (ph > 8.4) {
            builder.append("pH 偏碱，建议增施腐殖酸类有机肥并避免一次性高浓度追肥。");
        }
        return builder.toString();
    }

    private double completenessScore(PredictionRequest request) {
        int present = 0;
        present += request.soilSelenium() != null ? 1 : 0;
        present += request.phValue() != null ? 1 : 0;
        present += request.organicMatter() != null ? 1 : 0;
        present += request.salinity() != null ? 1 : 0;
        present += request.electricalConductivity() != null ? 1 : 0;
        present += request.avgTemperature() != null ? 1 : 0;
        present += request.precipitation() != null ? 1 : 0;
        return present * 0.008;
    }

    private String signature(Object... values) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder builder = new StringBuilder();
            for (Object value : values) {
                builder.append(value).append('|');
            }
            byte[] hash = digest.digest(builder.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (byte item : hash) {
                out.append(String.format("%02x", item));
            }
            return out.toString();
        } catch (Exception ex) {
            return String.valueOf(System.currentTimeMillis());
        }
    }

    private <T> T require(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    @SafeVarargs
    private final <T> T firstNumber(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstText(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private BigDecimal scale(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.HALF_UP);
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

    private record WeatherSummary(BigDecimal avgTemperature, BigDecimal precipitation) {
    }
}
