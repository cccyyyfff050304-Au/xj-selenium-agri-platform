CREATE DATABASE IF NOT EXISTS xj_selenium_agri DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE xj_selenium_agri;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS data_source;
DROP TABLE IF EXISTS report_record;
DROP TABLE IF EXISTS decision_suggestion;
DROP TABLE IF EXISTS soil_assessment;
DROP TABLE IF EXISTS selenium_prediction;
DROP TABLE IF EXISTS weather_record;
DROP TABLE IF EXISTS soil_sample;
DROP TABLE IF EXISTS crop_record;
DROP TABLE IF EXISTS crop_variety;
DROP TABLE IF EXISTS field_plot;
DROP TABLE IF EXISTS sys_user;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  username VARCHAR(64) NOT NULL COMMENT '用户名',
  password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码摘要',
  real_name VARCHAR(64) NOT NULL COMMENT '姓名',
  role VARCHAR(32) NOT NULL DEFAULT 'ADMIN' COMMENT '角色',
  phone VARCHAR(32) NULL COMMENT '联系电话',
  enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_sys_user_username(username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE field_plot (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  plot_code VARCHAR(64) NOT NULL COMMENT '地块编号',
  plot_name VARCHAR(120) NOT NULL COMMENT '地块名称',
  region VARCHAR(64) NOT NULL COMMENT '所属地区',
  county VARCHAR(64) NULL COMMENT '县市区',
  town VARCHAR(64) NULL COMMENT '乡镇/团场',
  longitude DECIMAL(10,6) NOT NULL COMMENT '经度',
  latitude DECIMAL(10,6) NOT NULL COMMENT '纬度',
  area_mu DECIMAL(10,2) NOT NULL COMMENT '面积/亩',
  soil_type VARCHAR(64) NOT NULL COMMENT '土壤类型',
  ph_value DECIMAL(4,2) NOT NULL COMMENT '土壤 pH',
  organic_matter DECIMAL(6,2) NOT NULL COMMENT '有机质 g/kg',
  selenium_content DECIMAL(7,3) NOT NULL COMMENT '土壤硒含量 mg/kg',
  salinity DECIMAL(6,2) NOT NULL COMMENT '盐分 g/kg',
  electrical_conductivity DECIMAL(7,2) NOT NULL COMMENT '电导率 dS/m',
  heavy_metal_risk VARCHAR(32) NOT NULL DEFAULT '低' COMMENT '重金属风险',
  risk_level VARCHAR(32) NOT NULL DEFAULT '适宜' COMMENT '风险等级',
  irrigation_type VARCHAR(64) NULL COMMENT '默认灌溉方式',
  manager_name VARCHAR(64) NULL COMMENT '负责人',
  status VARCHAR(32) NOT NULL DEFAULT '正常' COMMENT '状态',
  remark VARCHAR(500) NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_field_plot_code(plot_code),
  INDEX idx_field_plot_region(region),
  INDEX idx_field_plot_soil_type(soil_type),
  INDEX idx_field_plot_risk(risk_level),
  INDEX idx_field_plot_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地块信息表';

CREATE TABLE crop_variety (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  crop_type VARCHAR(32) NOT NULL COMMENT '作物类型',
  variety_name VARCHAR(120) NOT NULL COMMENT '品种名称',
  selenium_potential INT NOT NULL DEFAULT 70 COMMENT '富硒潜力指数',
  stress_resistance VARCHAR(120) NULL COMMENT '抗逆特征',
  suitable_region VARCHAR(180) NULL COMMENT '适宜区域',
  growth_period_days INT NULL COMMENT '生育期天数',
  remark VARCHAR(255) NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_crop_variety_type(crop_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作物品种表';

CREATE TABLE crop_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  plot_id BIGINT NOT NULL COMMENT '地块ID',
  variety_id BIGINT NULL COMMENT '品种ID',
  crop_type VARCHAR(32) NOT NULL COMMENT '作物类型',
  variety_name VARCHAR(120) NOT NULL COMMENT '品种名称',
  season_year INT NOT NULL COMMENT '种植年份',
  sowing_date DATE NOT NULL COMMENT '播种日期',
  harvest_date DATE NULL COMMENT '采收日期',
  expected_harvest_date DATE NULL COMMENT '预计采收日期',
  planting_area_mu DECIMAL(10,2) NULL COMMENT '种植面积/亩',
  irrigation_method VARCHAR(64) NOT NULL COMMENT '灌溉方式',
  fertilizer_method VARCHAR(160) NOT NULL COMMENT '施肥方式',
  irrigation_mode VARCHAR(64) NULL COMMENT '兼容字段：灌溉模式',
  fertilizer_plan VARCHAR(255) NULL COMMENT '兼容字段：施肥方案',
  yield_kg_mu DECIMAL(10,2) NULL COMMENT '产量 kg/亩',
  quality_level VARCHAR(32) NULL COMMENT '品质等级',
  growth_status VARCHAR(64) NULL COMMENT '生长状态',
  remark VARCHAR(500) NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_crop_record_plot(plot_id),
  INDEX idx_crop_record_type(crop_type),
  INDEX idx_crop_record_year(season_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作物种植档案表';

CREATE TABLE soil_sample (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  plot_id BIGINT NOT NULL COMMENT '地块ID',
  sample_code VARCHAR(64) NOT NULL COMMENT '样本编码',
  sample_date DATE NOT NULL COMMENT '采样日期',
  ph_value DECIMAL(4,2) NOT NULL COMMENT 'pH',
  organic_matter DECIMAL(6,2) NULL COMMENT '有机质 g/kg',
  available_nitrogen DECIMAL(7,2) NULL COMMENT '碱解氮 mg/kg',
  available_phosphorus DECIMAL(7,2) NULL COMMENT '有效磷 mg/kg',
  available_potassium DECIMAL(7,2) NULL COMMENT '速效钾 mg/kg',
  selenium_content DECIMAL(7,3) NULL COMMENT '土壤硒 mg/kg',
  salinity DECIMAL(6,2) NULL COMMENT '盐分 g/kg',
  electrical_conductivity DECIMAL(7,2) NULL COMMENT '电导率 dS/m',
  heavy_metal_risk VARCHAR(32) NULL COMMENT '重金属风险',
  risk_level VARCHAR(32) NULL COMMENT '风险等级',
  soil_moisture DECIMAL(6,2) NULL COMMENT '土壤含水率 %',
  sampler VARCHAR(64) NULL COMMENT '采样人',
  remark VARCHAR(500) NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_soil_sample_code(sample_code),
  INDEX idx_soil_sample_plot(plot_id),
  INDEX idx_soil_sample_date(sample_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='土壤检测样本表';

CREATE TABLE weather_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  region VARCHAR(64) NOT NULL COMMENT '地区',
  record_date DATE NOT NULL COMMENT '记录日期',
  avg_temp DECIMAL(5,2) NULL COMMENT '平均气温 ℃',
  max_temp DECIMAL(5,2) NULL COMMENT '最高气温 ℃',
  min_temp DECIMAL(5,2) NULL COMMENT '最低气温 ℃',
  precipitation DECIMAL(7,2) NULL COMMENT '降水 mm',
  sunshine_hours DECIMAL(5,2) NULL COMMENT '日照时数 h',
  solar_radiation DECIMAL(7,2) NULL COMMENT '太阳辐射 MJ/m2',
  wind_speed DECIMAL(5,2) NULL COMMENT '风速 m/s',
  humidity DECIMAL(5,2) NULL COMMENT '相对湿度 %',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_weather_region_date(region, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='气象数据表';

CREATE TABLE selenium_prediction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  plot_id BIGINT NOT NULL COMMENT '地块ID',
  crop_record_id BIGINT NULL COMMENT '种植档案ID',
  soil_sample_id BIGINT NULL COMMENT '土壤样本ID',
  crop_type VARCHAR(32) NOT NULL COMMENT '作物类型',
  variety_name VARCHAR(120) NOT NULL COMMENT '品种名称',
  soil_selenium DECIMAL(7,3) NOT NULL COMMENT '输入土壤硒 mg/kg',
  ph_value DECIMAL(4,2) NOT NULL COMMENT '输入 pH',
  organic_matter DECIMAL(6,2) NOT NULL COMMENT '输入有机质 g/kg',
  salinity DECIMAL(6,2) NOT NULL COMMENT '输入盐分 g/kg',
  electrical_conductivity DECIMAL(7,2) NOT NULL COMMENT '输入电导率 dS/m',
  avg_temperature DECIMAL(5,2) NOT NULL COMMENT '平均温度 ℃',
  precipitation DECIMAL(7,2) NOT NULL COMMENT '降水量 mm',
  irrigation_method VARCHAR(64) NOT NULL COMMENT '灌溉方式',
  fertilizer_method VARCHAR(160) NOT NULL COMMENT '施肥方式',
  predicted_selenium DECIMAL(8,4) NOT NULL COMMENT '预测产品硒含量 mg/kg',
  quality_level VARCHAR(32) NOT NULL COMMENT '富硒等级',
  confidence DECIMAL(6,4) NOT NULL COMMENT '可信度',
  risk_level VARCHAR(32) NOT NULL COMMENT '风险等级',
  model_version VARCHAR(64) NOT NULL COMMENT '模型版本',
  factor_contribution TEXT NULL COMMENT '影响因子贡献 JSON',
  factor_explanation TEXT NULL COMMENT '影响因素解释',
  suggestion TEXT NULL COMMENT '种植建议',
  input_signature VARCHAR(128) NULL COMMENT '输入签名',
  created_by VARCHAR(64) NULL COMMENT '创建人',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_prediction_plot(plot_id),
  INDEX idx_prediction_crop(crop_type),
  INDEX idx_prediction_quality(quality_level),
  INDEX idx_prediction_created(created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='富硒品质预测记录表';

CREATE TABLE soil_assessment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  plot_id BIGINT NOT NULL COMMENT '地块ID',
  soil_sample_id BIGINT NULL COMMENT '土壤样本ID',
  assessment_score DECIMAL(6,2) NOT NULL COMMENT '综合评分',
  risk_level VARCHAR(32) NOT NULL COMMENT '风险等级',
  ph_level VARCHAR(32) NULL COMMENT '酸碱度评价',
  fertility_level VARCHAR(32) NULL COMMENT '有机质评价',
  selenium_level VARCHAR(32) NULL COMMENT '硒背景评价',
  salinity_level VARCHAR(32) NULL COMMENT '盐分评价',
  conductivity_level VARCHAR(32) NULL COMMENT '电导率评价',
  heavy_metal_level VARCHAR(32) NULL COMMENT '重金属评价',
  item_evaluation TEXT NULL COMMENT '单项指标评价 JSON',
  radar_json TEXT NULL COMMENT '雷达图数据 JSON',
  constraint_factor VARCHAR(500) NULL COMMENT '限制因子',
  overall_comment TEXT NULL COMMENT '综合评价说明',
  improvement_advice TEXT NULL COMMENT '改良建议',
  created_by VARCHAR(64) NULL COMMENT '创建人',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_assessment_plot(plot_id),
  INDEX idx_assessment_risk(risk_level),
  INDEX idx_assessment_created(created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='土壤环境评估记录表';

CREATE TABLE decision_suggestion (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  plot_id BIGINT NULL COMMENT '地块ID',
  crop_record_id BIGINT NULL COMMENT '种植档案ID',
  suggestion_type VARCHAR(64) NOT NULL COMMENT '建议类型',
  title VARCHAR(160) NOT NULL COMMENT '标题',
  content VARCHAR(1200) NOT NULL COMMENT '内容',
  priority VARCHAR(16) NOT NULL DEFAULT '推荐' COMMENT '建议等级：推荐/谨慎/风险',
  status VARCHAR(32) NOT NULL DEFAULT '待处理' COMMENT '状态',
  generated_by VARCHAR(64) NULL COMMENT '生成人',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_decision_plot(plot_id),
  INDEX idx_decision_status(status),
  INDEX idx_decision_priority(priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='决策建议表';

CREATE TABLE report_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  report_name VARCHAR(160) NOT NULL COMMENT '报告名称',
  report_type VARCHAR(64) NOT NULL COMMENT '报告类型',
  related_plot_id BIGINT NULL COMMENT '关联地块ID',
  file_path VARCHAR(255) NULL COMMENT '文件路径',
  summary VARCHAR(1000) NULL COMMENT '摘要',
  content MEDIUMTEXT NULL COMMENT '报告预览内容 HTML',
  source_summary VARCHAR(1000) NULL COMMENT '数据来源说明摘要',
  created_by VARCHAR(64) NULL COMMENT '创建人',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_report_type(report_type),
  INDEX idx_report_plot(related_plot_id),
  INDEX idx_report_created(created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报告记录表';

CREATE TABLE data_source (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  source_name VARCHAR(160) NOT NULL COMMENT '数据名称',
  source_type VARCHAR(64) NULL COMMENT '来源类型',
  source_url VARCHAR(255) NULL COMMENT '来源网址',
  data_scope VARCHAR(255) NULL COMMENT '数据范围/用途',
  collection_time DATETIME NULL COMMENT '采集时间',
  collection_method VARCHAR(120) NULL COMMENT '采集/生成方式',
  simulated TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否模拟数据',
  remark VARCHAR(500) NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_data_source_type(source_type),
  INDEX idx_data_source_simulated(simulated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据来源记录表';

CREATE TABLE operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NULL COMMENT '用户ID',
  username VARCHAR(64) NULL COMMENT '用户名',
  module_name VARCHAR(64) NULL COMMENT '模块名称',
  operation_type VARCHAR(64) NULL COMMENT '操作类型',
  request_uri VARCHAR(255) NULL COMMENT '请求地址',
  request_method VARCHAR(16) NULL COMMENT '请求方法',
  ip_address VARCHAR(64) NULL COMMENT 'IP地址',
  result_status VARCHAR(32) NULL COMMENT '结果',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_operation_user(username),
  INDEX idx_operation_created(created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
