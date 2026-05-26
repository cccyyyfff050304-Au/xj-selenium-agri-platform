package com.xj.agri.dto;

import com.xj.agri.entity.SeleniumPrediction;

import java.util.List;
import java.util.Map;

public record DashboardStats(
        Long plotCount,
        Long cropRecordCount,
        Long soilSampleCount,
        Long weatherRecordCount,
        Long predictionCount,
        Long suggestionCount,
        Long seleniumSuitablePlotCount,
        Long riskWarningPlotCount,
        List<Map<String, Object>> plotRegionStats,
        List<Map<String, Object>> cropTypeStats,
        List<Map<String, Object>> seleniumLevelStats,
        List<Map<String, Object>> soilRiskStats,
        List<Map<String, Object>> recentPredictionTrend,
        List<Map<String, Object>> latestRiskWarnings,
        List<Map<String, Object>> recommendedPlotRank,
        List<SeleniumPrediction> recentPredictions
) {
}
