# 接口文档摘要

Swagger 自动文档地址：`http://localhost:8080/swagger-ui.html`

统一返回格式：

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {},
  "timestamp": "2026-05-25T10:00:00"
}
```

除登录接口外，其余 `/api/**` 接口需要请求头：

```text
Authorization: Bearer <JWT>
```

## 认证

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录，返回 JWT |
| GET | `/api/auth/me` | 当前用户信息 |

## 首页驾驶舱

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/dashboard/stats` | 返回总量指标、作物分布、富硒等级分布、土壤风险分布、最近预测趋势、风险预警和推荐地块排行 |

## 通用分页与 CRUD

分页参数同时支持 `current` / `page` 与 `size`：

```text
GET /api/{resource}?current=1&size=10&keyword=...
```

通用操作：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/{resource}` | 分页查询 |
| GET | `/api/{resource}/{id}` | 详情 |
| POST | `/api/{resource}` | 新增 |
| PUT | `/api/{resource}/{id}` | 编辑 |
| DELETE | `/api/{resource}/{id}` | 删除 |

| 资源 | 路径 | 主要说明 |
| --- | --- | --- |
| 用户 | `/api/users` | 用户管理 |
| 地块 | `/api/plots` | 支持 `keyword`、`region`、`soilType`、`riskLevel` |
| 作物品种 | `/api/varieties` | 支持 `/api/varieties/all` |
| 种植档案 | `/api/crop-records` | 支持 `keyword`、`cropType`、`plotId`，返回关联地块硒含量、pH、风险等级 |
| 土壤样本 | `/api/soil-samples` | 支持 `/api/soil-samples/all` |
| 气象数据 | `/api/weather-records` | 气象记录维护 |
| 预测历史 | `/api/predictions` | 支持 `keyword`、`cropType`、`plotId`、`qualityLevel` |
| 土壤评估历史 | `/api/soil-assessments` | 支持 `keyword`、`plotId`、`riskLevel` |
| 决策建议 | `/api/decisions` | 动态建议记录 |
| 报告 | `/api/reports` | 支持 `keyword`、`reportType` |
| 数据来源 | `/api/data-sources` | 来源类型、网址、采集时间、是否模拟 |
| 操作日志 | `/api/operation-logs` | 操作审计 |

## 富硒品质预测

`POST /api/predictions/run`

请求示例：

```json
{
  "plotId": 1,
  "cropRecordId": 1,
  "soilSampleId": 1,
  "cropType": "棉花",
  "varietyName": "新陆早78号",
  "soilSelenium": 0.26,
  "phValue": 7.8,
  "organicMatter": 18.5,
  "salinity": 1.2,
  "electricalConductivity": 1.8,
  "avgTemperature": 23.5,
  "precipitation": 5.2,
  "irrigationMethod": "膜下滴灌",
  "fertilizerMethod": "有机肥+氮磷钾平衡追肥"
}
```

说明：

- `plotId` 必填。
- 其他字段可由地块、种植档案、土壤样本和近期气象数据自动补齐。
- 算法以土壤硒含量为主因子，同时计算 pH、有机质、盐分/电导率、气象适宜性、水肥管理和作物吸收系数。
- 返回并保存 `predictedSelenium`、`qualityLevel`、`confidence`、`riskLevel`、`factorContribution`、`factorExplanation`、`suggestion`。

富硒等级：

- 低硒
- 普通
- 富硒
- 高硒风险

## 土壤环境评估

`POST /api/soil-assessments/run`

请求示例：

```json
{
  "plotId": 1,
  "soilSampleId": 1
}
```

返回并保存：

- 综合评分：`assessmentScore`
- 风险等级：适宜、轻度预警、中度风险、高风险
- 单项等级：pH、有机质、土壤硒、盐分、电导率、重金属
- 单项评价 JSON：`itemEvaluation`
- 雷达图数据：`radarJson`
- 综合评价说明和改良建议

辅助接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/soil-assessments/summary` | 风险分布、多地块评分、最新评估 |

## 决策建议生成

`POST /api/decisions/generate`

请求示例：

```json
{
  "plotId": 1,
  "cropRecordId": 1
}
```

返回 5 类动态建议并保存：

- 富硒种植建议
- 土壤改良建议
- 灌溉施肥建议
- 风险预警建议
- 适宜推广区域建议

建议等级：推荐、谨慎、风险。

## 报告管理

`POST /api/reports/generate`

请求示例：

```json
{
  "reportName": "昌吉示范地块富硒种植适宜性报告",
  "reportType": "地块富硒种植适宜性报告",
  "relatedPlotId": 1
}
```

报告内容保存到 `report_record.content`，包含：

- 基本信息
- 地块数据
- 作物数据
- 土壤评估结果
- 富硒预测结果
- 图表摘要
- 决策建议
- 数据来源说明
- 生成时间

辅助接口：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/reports/{id}/preview` | 返回报告 HTML 内容，用于前端预览和浏览器打印 |
