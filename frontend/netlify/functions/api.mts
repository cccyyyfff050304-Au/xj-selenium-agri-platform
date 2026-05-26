import type { Config, Context } from "@netlify/functions";

type RecordItem = Record<string, unknown>;

function json(data: unknown, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
    },
  });
}

function ok(data: unknown) {
  return json({ code: 0, message: "success", data });
}

function fail(message: string, status = 400) {
  return json({ code: 1, message, data: null }, status);
}

async function readJson(req: Request) {
  try {
    return await req.json();
  } catch {
    return {};
  }
}

function today(offset = 0) {
  const date = new Date();
  date.setDate(date.getDate() + offset);
  return date.toISOString().slice(0, 10);
}

function dateTime(offset = 0) {
  return `${today(offset)}T09:30:00`;
}

function makePage(records: RecordItem[], params: URLSearchParams) {
  const current = Math.max(Number(params.get("current") || 1), 1);
  const size = Math.max(Number(params.get("size") || 10), 1);
  const filtered = filterRecords(records, params);
  const start = (current - 1) * size;

  return {
    records: filtered.slice(start, start + size),
    total: filtered.length,
    size,
    current,
    pages: Math.max(Math.ceil(filtered.length / size), 1),
  };
}

function filterRecords(records: RecordItem[], params: URLSearchParams) {
  const ignored = new Set(["current", "size"]);
  let rows = [...records];

  params.forEach((rawValue, key) => {
    const value = rawValue.trim();
    if (!value || ignored.has(key)) return;

    if (key === "keyword") {
      const needle = value.toLowerCase();
      rows = rows.filter((row) => JSON.stringify(row).toLowerCase().includes(needle));
      return;
    }

    rows = rows.filter((row) => {
      const actual = row[key];
      if (actual === undefined || actual === null) return true;
      return String(actual) === value || String(actual).includes(value);
    });
  });

  return rows;
}

function countBy(records: RecordItem[], key: string, outputKey: string) {
  const counts = new Map<string, number>();
  for (const row of records) {
    const value = String(row[key] || "未分类");
    counts.set(value, (counts.get(value) || 0) + 1);
  }
  return [...counts.entries()].map(([name, count]) => ({ [outputKey]: name, count }));
}

function findById(records: RecordItem[], id: number) {
  return records.find((row) => Number(row.id) === id);
}

function makeReportContent(report: RecordItem) {
  return `
    <h1>${report.reportName}</h1>
    <p><strong>报告类型：</strong>${report.reportType}</p>
    <p><strong>关联地块：</strong>${report.relatedPlotId || "综合"}</p>
    <h2>综合结论</h2>
    <p>当前示范数据表明，多数地块具备富硒作物种植基础，建议继续结合土壤 pH、盐分、电导率和作物品种进行分区管理。</p>
    <h2>管理建议</h2>
    <p>优先推广滴灌与水肥一体化，维持适宜土壤水分，针对中高风险地块开展盐分监测和土壤改良。</p>
  `;
}

function makeDemoData() {
  const plots = [
    {
      id: 1,
      plotCode: "XJ-UR-001",
      plotName: "乌鲁木齐达坂城示范田",
      region: "乌鲁木齐",
      county: "达坂城",
      town: "示范园",
      longitude: 88.31,
      latitude: 43.36,
      areaMu: 126,
      soilType: "灰漠土",
      phValue: 7.8,
      organicMatter: 18.6,
      seleniumContent: 0.32,
      salinity: 1.1,
      electricalConductivity: 1.7,
      heavyMetalRisk: "低",
      riskLevel: "适宜",
      irrigationType: "膜下滴灌",
      managerName: "张天山",
      status: "在用",
      remark: "富硒示范地块",
    },
    {
      id: 2,
      plotCode: "XJ-CJ-002",
      plotName: "昌吉呼图壁棉花田",
      region: "昌吉",
      county: "呼图壁",
      town: "良种场",
      longitude: 86.87,
      latitude: 44.18,
      areaMu: 220,
      soilType: "灌淤土",
      phValue: 7.5,
      organicMatter: 20.1,
      seleniumContent: 0.27,
      salinity: 1.4,
      electricalConductivity: 2.1,
      heavyMetalRisk: "低",
      riskLevel: "适宜",
      irrigationType: "水肥一体化滴灌",
      managerName: "李棉农",
      status: "在用",
      remark: "棉花连片示范区",
    },
    {
      id: 3,
      plotCode: "XJ-AK-003",
      plotName: "阿克苏苹果园",
      region: "阿克苏",
      county: "温宿",
      town: "特色林果园",
      longitude: 80.24,
      latitude: 41.29,
      areaMu: 96,
      soilType: "棕漠土",
      phValue: 8.1,
      organicMatter: 16.8,
      seleniumContent: 0.21,
      salinity: 1.8,
      electricalConductivity: 2.5,
      heavyMetalRisk: "低",
      riskLevel: "轻度预警",
      irrigationType: "滴灌",
      managerName: "王林果",
      status: "在用",
      remark: "盐分需持续监测",
    },
    {
      id: 4,
      plotCode: "XJ-KS-004",
      plotName: "喀什番茄基地",
      region: "喀什",
      county: "疏勒",
      town: "设施农业区",
      longitude: 76.05,
      latitude: 39.41,
      areaMu: 180,
      soilType: "灌耕土",
      phValue: 7.6,
      organicMatter: 19.4,
      seleniumContent: 0.24,
      salinity: 1.6,
      electricalConductivity: 2.2,
      heavyMetalRisk: "低",
      riskLevel: "适宜",
      irrigationType: "滴灌",
      managerName: "买买提",
      status: "在用",
      remark: "番茄加工基地",
    },
    {
      id: 5,
      plotCode: "XJ-HM-005",
      plotName: "哈密葡萄园",
      region: "哈密",
      county: "伊州",
      town: "葡萄产业带",
      longitude: 93.52,
      latitude: 42.82,
      areaMu: 150,
      soilType: "风沙土",
      phValue: 8.3,
      organicMatter: 14.5,
      seleniumContent: 0.18,
      salinity: 2.3,
      electricalConductivity: 3.1,
      heavyMetalRisk: "中",
      riskLevel: "中度风险",
      irrigationType: "滴灌",
      managerName: "陈葡萄",
      status: "观测",
      remark: "盐分偏高",
    },
  ];

  const varieties = [
    { id: 1, cropType: "棉花", varietyName: "新陆早78号", seleniumPotential: 0.78, growthPeriodDays: 125 },
    { id: 2, cropType: "小麦", varietyName: "新冬53号", seleniumPotential: 0.68, growthPeriodDays: 240 },
    { id: 3, cropType: "玉米", varietyName: "新玉66号", seleniumPotential: 0.62, growthPeriodDays: 115 },
    { id: 4, cropType: "葡萄", varietyName: "无核白", seleniumPotential: 0.54, growthPeriodDays: 150 },
    { id: 5, cropType: "枸杞", varietyName: "宁杞7号", seleniumPotential: 0.72, growthPeriodDays: 150 },
    { id: 6, cropType: "番茄", varietyName: "加工番茄220", seleniumPotential: 0.64, growthPeriodDays: 110 },
  ];

  const cropRecords = plots.map((plot, index) => {
    const variety = varieties[index % varieties.length];
    return {
      id: index + 1,
      plotId: plot.id,
      varietyId: variety.id,
      cropType: variety.cropType,
      varietyName: variety.varietyName,
      seasonYear: 2026,
      sowingDate: today(-45 - index * 4),
      harvestDate: "",
      expectedHarvestDate: today(70 + index * 5),
      plantingAreaMu: Number(plot.areaMu) * 0.82,
      irrigationMethod: plot.irrigationType,
      fertilizerMethod: "有机肥+氮磷钾平衡追肥",
      irrigationMode: plot.irrigationType,
      fertilizerPlan: "基肥深施，关键生育期追肥",
      yieldKgMu: 420 + index * 35,
      qualityLevel: index % 3 === 0 ? "优" : "良",
      growthStatus: "营养生长期",
      remark: "Netlify 演示数据",
      plotName: plot.plotName,
      region: plot.region,
      plotSeleniumContent: plot.seleniumContent,
      plotPhValue: plot.phValue,
      plotRiskLevel: plot.riskLevel,
    };
  });

  const soilSamples = plots.map((plot, index) => ({
    id: index + 1,
    plotId: plot.id,
    sampleCode: `SOIL-2026-${String(index + 1).padStart(3, "0")}`,
    sampleDate: today(-12 - index),
    phValue: plot.phValue,
    organicMatter: plot.organicMatter,
    availableNitrogen: 75 + index * 8,
    availablePhosphorus: 18 + index * 2,
    availablePotassium: 165 + index * 14,
    seleniumContent: plot.seleniumContent,
    salinity: plot.salinity,
    electricalConductivity: plot.electricalConductivity,
    heavyMetalRisk: plot.heavyMetalRisk,
    riskLevel: plot.riskLevel,
    soilMoisture: 17 + index,
    sampler: "系统演示员",
    remark: "线上演示样本",
  }));

  const weatherRecords = plots.flatMap((plot, plotIndex) =>
    [0, 1, 2].map((day) => ({
      id: plotIndex * 3 + day + 1,
      region: plot.region,
      recordDate: today(-day),
      avgTemp: 22 + plotIndex,
      maxTemp: 30 + plotIndex,
      minTemp: 15 + plotIndex,
      precipitation: plotIndex === 2 ? 4.2 : 1.1 + day,
      sunshineHours: 8.5 - day * 0.4,
      solarRadiation: 18 + plotIndex,
      windSpeed: 2.5 + day * 0.3,
      humidity: 42 + plotIndex * 3,
    }))
  );

  const predictions = cropRecords.map((record, index) => {
    const plot = plots.find((item) => item.id === record.plotId) || plots[0];
    const predicted = Number((Number(plot.seleniumContent) * 0.76 + Number(varieties[index].seleniumPotential) * 0.12).toFixed(3));
    return {
      id: index + 1,
      plotId: record.plotId,
      cropRecordId: record.id,
      soilSampleId: index + 1,
      cropType: record.cropType,
      varietyName: record.varietyName,
      soilSelenium: plot.seleniumContent,
      phValue: plot.phValue,
      organicMatter: plot.organicMatter,
      salinity: plot.salinity,
      electricalConductivity: plot.electricalConductivity,
      avgTemperature: 23 + index,
      precipitation: 3 + index,
      irrigationMethod: record.irrigationMethod,
      fertilizerMethod: record.fertilizerMethod,
      predictedSelenium: predicted,
      qualityLevel: predicted >= 0.28 ? "富硒" : predicted >= 0.2 ? "普通" : "低硒",
      confidence: 0.86 - index * 0.02,
      riskLevel: plot.riskLevel === "中度风险" ? "谨慎" : "推荐",
      modelVersion: "netlify-demo-v1",
      factorContribution: JSON.stringify([
        { factor: "土壤硒", contribution: 42 },
        { factor: "pH", contribution: 18 },
        { factor: "有机质", contribution: 16 },
        { factor: "盐分", contribution: 14 },
        { factor: "气象", contribution: 10 },
      ]),
      factorExplanation: "土壤硒含量、pH 和盐分是当前预测的主要影响因素。",
      suggestion: plot.riskLevel === "中度风险" ? "建议先进行盐分改良后再扩大种植。" : "建议保持滴灌和有机肥补充。",
      inputSignature: `demo-${record.id}`,
      createdBy: "admin",
      createdAt: dateTime(-index),
    };
  });

  const assessments = plots.map((plot, index) => {
    const score = Math.max(62, Math.round(92 - Number(plot.salinity) * 8 - index * 2));
    return {
      id: index + 1,
      plotId: plot.id,
      soilSampleId: index + 1,
      assessmentScore: score,
      riskLevel: plot.riskLevel,
      phLevel: Number(plot.phValue) > 8.2 ? "偏碱" : "适宜",
      fertilityLevel: Number(plot.organicMatter) > 18 ? "较高" : "中等",
      salinityLevel: Number(plot.salinity) > 2 ? "偏高" : "适宜",
      seleniumLevel: Number(plot.seleniumContent) > 0.25 ? "较高" : "中等",
      conductivityLevel: Number(plot.electricalConductivity) > 3 ? "偏高" : "适宜",
      heavyMetalLevel: plot.heavyMetalRisk,
      itemEvaluation: "pH、硒含量和有机质整体可支撑示范种植。",
      radarJson: JSON.stringify([82, 78, 86, Math.max(45, 90 - Number(plot.salinity) * 14), Math.max(45, 90 - Number(plot.electricalConductivity) * 10), 88]),
      constraintFactor: plot.riskLevel === "中度风险" ? "盐分和电导率偏高" : "无明显限制",
      overallComment: "土壤环境具备演示评价基础。",
      improvementAdvice: plot.riskLevel === "中度风险" ? "建议深翻压盐、优化灌排并增加有机质。" : "建议维持当前水肥管理。",
      createdAt: dateTime(-index),
    };
  });

  const decisions = [
    {
      id: 1,
      plotId: 1,
      cropRecordId: 1,
      suggestionType: "富硒种植",
      title: "维持膜下滴灌和有机肥补充",
      content: "该地块硒含量和 pH 处于较适宜区间，建议维持稳定滴灌并在关键生育期补充有机肥。",
      priority: "推荐",
      status: "待执行",
      generatedBy: "admin",
      createdAt: dateTime(-1),
    },
    {
      id: 2,
      plotId: 5,
      cropRecordId: 5,
      suggestionType: "土壤改良",
      title: "先控盐再扩大种植",
      content: "哈密葡萄园盐分和电导率偏高，建议完成盐分改良和排水优化后再扩大示范面积。",
      priority: "谨慎",
      status: "待执行",
      generatedBy: "admin",
      createdAt: dateTime(-2),
    },
  ];

  const reports = [
    {
      id: 1,
      reportName: "新疆特色作物富硒示范综合报告",
      reportType: "智慧农业综合决策报告",
      relatedPlotId: 1,
      sourceSummary: "Netlify 演示数据，来源于项目初始化样例。",
      summary: "示范区域具备富硒作物种植基础，需关注盐分偏高地块。",
      createdBy: "admin",
      createdAt: dateTime(-1),
    },
  ];

  const dataSources = [
    {
      id: 1,
      sourceName: "新疆农业统计公开资料",
      sourceType: "公开资料",
      sourceUrl: "https://example.com/agri",
      dataScope: "作物、区域、种植结构",
      collectionTime: dateTime(-10),
      collectionMethod: "人工整理",
      simulated: false,
      remark: "演示记录",
    },
    {
      id: 2,
      sourceName: "系统初始化模拟数据",
      sourceType: "模拟数据",
      sourceUrl: "",
      dataScope: "地块、土壤、气象、预测结果",
      collectionTime: dateTime(-8),
      collectionMethod: "规则生成",
      simulated: true,
      remark: "用于线上演示。",
    },
  ];

  const logs = [
    { id: 1, username: "admin", moduleName: "登录", operationType: "LOGIN", requestUri: "/api/auth/login", resultStatus: "成功", createdAt: dateTime(0) },
    { id: 2, username: "admin", moduleName: "看板", operationType: "QUERY", requestUri: "/api/dashboard/stats", resultStatus: "成功", createdAt: dateTime(0) },
    { id: 3, username: "admin", moduleName: "部署", operationType: "DEMO", requestUri: "/api/*", resultStatus: "成功", createdAt: dateTime(-1) },
  ];

  return {
    plots,
    varieties,
    cropRecords,
    soilSamples,
    weatherRecords,
    predictions,
    assessments,
    decisions,
    reports,
    dataSources,
    logs,
  };
}

function dashboardStats(data: ReturnType<typeof makeDemoData>) {
  return {
    plotCount: data.plots.length,
    cropRecordCount: data.cropRecords.length,
    soilSampleCount: data.soilSamples.length,
    predictionCount: data.predictions.length,
    seleniumSuitablePlotCount: data.plots.filter((plot) => Number(plot.seleniumContent) >= 0.2 && plot.riskLevel !== "高风险").length,
    riskWarningPlotCount: data.plots.filter((plot) => plot.riskLevel !== "适宜").length,
    cropTypeStats: countBy(data.cropRecords, "cropType", "cropType"),
    seleniumLevelStats: countBy(data.predictions, "qualityLevel", "qualityLevel"),
    soilRiskStats: countBy(data.plots, "riskLevel", "riskLevel"),
    recentPredictionTrend: data.predictions.map((item) => ({ id: item.id, cropType: item.cropType, value: item.predictedSelenium })),
    latestRiskWarnings: data.plots.filter((plot) => plot.riskLevel !== "适宜"),
    recommendedPlotRank: [...data.plots].sort((a, b) => Number(b.seleniumContent) - Number(a.seleniumContent)).slice(0, 5),
    recentPredictions: data.predictions,
  };
}

function runPrediction(body: RecordItem, data: ReturnType<typeof makeDemoData>) {
  const plot = data.plots.find((item) => item.id === Number(body.plotId)) || data.plots[0];
  const predicted = Number((Number(body.soilSelenium || plot.seleniumContent) * 0.82).toFixed(3));
  return {
    id: Date.now(),
    ...body,
    plotId: Number(body.plotId || plot.id),
    predictedSelenium: predicted,
    qualityLevel: predicted >= 0.28 ? "富硒" : predicted >= 0.2 ? "普通" : "低硒",
    confidence: 0.88,
    riskLevel: Number(body.salinity || 0) > 2 ? "谨慎" : "推荐",
    modelVersion: "netlify-demo-v1",
    factorContribution: JSON.stringify([
      { factor: "土壤硒", contribution: 45 },
      { factor: "pH", contribution: 17 },
      { factor: "有机质", contribution: 15 },
      { factor: "盐分", contribution: 13 },
      { factor: "气象", contribution: 10 },
    ]),
    factorExplanation: "本次结果由 Netlify 演示函数按规则计算，仅用于线上预览。",
    suggestion: "建议结合实际后端数据库复核后用于生产决策。",
    inputSignature: `demo-${Date.now()}`,
    createdBy: "admin",
    createdAt: dateTime(0),
  };
}

function runAssessment(body: RecordItem, data: ReturnType<typeof makeDemoData>) {
  const plot = data.plots.find((item) => item.id === Number(body.plotId)) || data.plots[0];
  const score = Math.max(55, Math.round(94 - Number(plot.salinity) * 9 - Number(plot.electricalConductivity) * 2));
  return {
    id: Date.now(),
    plotId: plot.id,
    soilSampleId: Number(body.soilSampleId || 1),
    assessmentScore: score,
    riskLevel: plot.riskLevel,
    phLevel: Number(plot.phValue) > 8.2 ? "偏碱" : "适宜",
    fertilityLevel: Number(plot.organicMatter) > 18 ? "较高" : "中等",
    salinityLevel: Number(plot.salinity) > 2 ? "偏高" : "适宜",
    seleniumLevel: Number(plot.seleniumContent) > 0.25 ? "较高" : "中等",
    conductivityLevel: Number(plot.electricalConductivity) > 3 ? "偏高" : "适宜",
    heavyMetalLevel: plot.heavyMetalRisk,
    itemEvaluation: "Netlify 演示函数生成的土壤环境评价。",
    radarJson: JSON.stringify([82, 78, 84, Math.max(45, 90 - Number(plot.salinity) * 14), Math.max(45, 90 - Number(plot.electricalConductivity) * 10), 88]),
    constraintFactor: plot.riskLevel === "中度风险" ? "盐分和电导率偏高" : "无明显限制",
    overallComment: "该地块可作为线上演示评价样例。",
    improvementAdvice: plot.riskLevel === "中度风险" ? "建议控盐、改良土壤并监测电导率。" : "建议维持现有水肥管理。",
    createdAt: dateTime(0),
  };
}

function generateDecisions(body: RecordItem) {
  const plotId = Number(body.plotId || 1);
  const cropRecordId = Number(body.cropRecordId || 1);
  return [
    {
      id: Date.now(),
      plotId,
      cropRecordId,
      suggestionType: "富硒种植",
      title: "保持稳定滴灌与有机肥补充",
      content: "当前地块具备演示级富硒种植条件，建议保持水肥均衡并持续监测土壤硒含量。",
      priority: "推荐",
      status: "待执行",
      generatedBy: "admin",
      createdAt: dateTime(0),
    },
    {
      id: Date.now() + 1,
      plotId,
      cropRecordId,
      suggestionType: "风险管理",
      title: "建立盐分和电导率巡检",
      content: "建议每 10-15 天记录盐分、电导率和土壤水分，提前识别轻度预警趋势。",
      priority: "谨慎",
      status: "待执行",
      generatedBy: "admin",
      createdAt: dateTime(0),
    },
  ];
}

function generateReport(body: RecordItem) {
  const report = {
    id: Date.now(),
    reportName: String(body.reportName || "线上演示报告"),
    reportType: String(body.reportType || "智慧农业综合决策报告"),
    relatedPlotId: body.relatedPlotId ? Number(body.relatedPlotId) : 1,
    sourceSummary: "Netlify Function 演示数据。",
    summary: "当前为无外部数据库的线上演示报告。",
    createdBy: "admin",
    createdAt: dateTime(0),
  };

  return { ...report, content: makeReportContent(report) };
}

function collectionMap(data: ReturnType<typeof makeDemoData>) {
  return {
    plots: data.plots,
    "crop-records": data.cropRecords,
    "soil-samples": data.soilSamples,
    "weather-records": data.weatherRecords,
    predictions: data.predictions,
    "soil-assessments": data.assessments,
    decisions: data.decisions,
    reports: data.reports,
    "data-sources": data.dataSources,
    "operation-logs": data.logs,
  } satisfies Record<string, RecordItem[]>;
}

export default async (req: Request, _context: Context) => {
  const url = new URL(req.url);
  const segments = url.pathname.replace(/^\/api\/?/, "").split("/").filter(Boolean);
  const [resource, action] = segments;
  const data = makeDemoData();

  if (resource === "auth" && action === "login" && req.method === "POST") {
    const body = await readJson(req);
    if (body.username === "admin" && body.password === "123456") {
      return ok({
        token: "netlify-demo-token",
        username: "admin",
        realName: "系统管理员",
        role: "ADMIN",
      });
    }
    return fail("账号或密码错误", 401);
  }

  if (resource === "auth" && action === "me" && req.method === "GET") {
    return ok({ id: 1, username: "admin", realName: "系统管理员", role: "ADMIN" });
  }

  if (resource === "dashboard" && action === "stats" && req.method === "GET") {
    return ok(dashboardStats(data));
  }

  if (resource === "varieties" && action === "all" && req.method === "GET") {
    return ok(data.varieties);
  }

  if (resource === "predictions" && action === "run" && req.method === "POST") {
    return ok(runPrediction(await readJson(req), data));
  }

  if (resource === "soil-assessments" && action === "summary" && req.method === "GET") {
    return ok({
      riskDistribution: countBy(data.assessments, "riskLevel", "riskLevel"),
      plotScores: data.assessments.map((item) => ({ plotId: item.plotId, score: item.assessmentScore })),
      latest: data.assessments.slice(0, 5),
    });
  }

  if (resource === "soil-assessments" && action === "run" && req.method === "POST") {
    return ok(runAssessment(await readJson(req), data));
  }

  if (resource === "decisions" && action === "generate" && req.method === "POST") {
    return ok(generateDecisions(await readJson(req)));
  }

  if (resource === "reports" && action === "generate" && req.method === "POST") {
    return ok(generateReport(await readJson(req)));
  }

  if (resource === "reports" && segments[2] === "preview" && req.method === "GET") {
    const report = findById(data.reports, Number(action)) || data.reports[0];
    return ok({ ...report, content: makeReportContent(report) });
  }

  const collections = collectionMap(data);
  const records = collections[resource || ""];

  if (!records) {
    return fail(`API route not found: ${url.pathname}`, 404);
  }

  if (req.method === "GET") {
    if (action && /^\d+$/.test(action)) {
      return ok(findById(records, Number(action)) || null);
    }
    return ok(makePage(records, url.searchParams));
  }

  if (req.method === "POST") {
    return ok({ id: Date.now(), ...(await readJson(req)) });
  }

  if (req.method === "PUT" || req.method === "PATCH") {
    return ok({ ...(findById(records, Number(action)) || {}), ...(await readJson(req)), id: Number(action) });
  }

  if (req.method === "DELETE") {
    return ok(true);
  }

  return fail("Method not allowed", 405);
};

export const config: Config = {
  path: "/api/*",
};
