USE xj_selenium_agri;

INSERT INTO sys_user (username, password_hash, real_name, role, phone, enabled)
VALUES ('admin', '$2a$10$YMdIae7.cbkTNn3XZNlAIe6RWJg6WAG6AU8hxVL8xkDtedbXrwOxK', '系统管理员', 'ADMIN', '0991-1234567', 1);

INSERT INTO crop_variety (crop_type, variety_name, selenium_potential, stress_resistance, suitable_region, growth_period_days, remark) VALUES
('棉花', '新陆早78号', 62, '耐盐碱、早熟', '昌吉、石河子、阿克苏', 128, '适合膜下滴灌棉区'),
('棉花', '中棉所96号', 66, '抗枯萎、耐旱', '阿克苏、喀什、库尔勒', 132, '高产稳产品种'),
('棉花', '新陆中82号', 64, '抗逆性强、机采适配', '石河子、昌吉', 135, '适合机采棉示范区'),
('小麦', '新冬41号', 70, '抗寒、抗倒伏', '伊犁、昌吉、乌鲁木齐', 245, '冬小麦主栽模拟品种'),
('小麦', '新春37号', 68, '耐旱、灌浆稳定', '哈密、吐鲁番、阿克苏', 118, '春小麦示范品种'),
('小麦', '新麦56号', 72, '抗干热风', '喀什、库尔勒', 122, '适合绿洲灌区'),
('玉米', '新玉69号', 74, '耐密植、抗倒伏', '昌吉、石河子、伊犁', 126, '粮饲兼用型'),
('玉米', '屯玉168', 71, '耐旱、稳产', '阿克苏、喀什、哈密', 120, '适合滴灌高效栽培'),
('玉米', '金穗908', 76, '抗病、耐高温', '吐鲁番、库尔勒', 116, '高光热区适配'),
('葡萄', '无核白', 88, '耐干热、品质优', '吐鲁番、哈密', 150, '新疆特色鲜食葡萄'),
('葡萄', '红地球', 82, '果粒大、耐储运', '昌吉、伊犁、库尔勒', 160, '商品化示范'),
('葡萄', '新葡7号', 84, '抗旱、糖酸协调', '吐鲁番、哈密、阿克苏', 155, '富硒品质试验材料'),
('枸杞', '新杞1号', 86, '耐旱、耐盐', '哈密、吐鲁番、库尔勒', 170, '干旱区经济作物'),
('枸杞', '宁杞7号', 83, '结果稳定', '昌吉、阿克苏', 168, '适合标准化种植'),
('枸杞', '精杞优2号', 87, '抗逆性强', '喀什、哈密', 172, '富硒调控潜力较高'),
('番茄', '石番18号', 80, '抗病、适合加工', '石河子、昌吉', 115, '加工番茄模拟品种'),
('番茄', '新番58号', 78, '耐热、坐果稳定', '吐鲁番、库尔勒、阿克苏', 112, '设施和露地均可'),
('番茄', '天山红9号', 81, '高番茄红素、品质稳定', '喀什、伊犁', 118, '品质型示范品种');

INSERT INTO data_source (source_name, source_type, source_url, data_scope, collection_time, collection_method, simulated, remark) VALUES
('新疆统计局统计年鉴农业数据', '公开统计', 'https://tjj.xinjiang.gov.cn/', '农业播种面积、作物结构、特色作物宏观约束', '2026-05-25 00:00:00', '人工参考公开口径生成演示数据', 1, '初始化数据为模拟数据，仅用于系统演示'),
('新疆国民经济和社会发展统计公报', '公开统计', 'https://www.xinjiang.gov.cn/', '农业、棉花、水资源、林果等公开信息', '2026-05-25 00:00:00', '人工参考公开口径生成演示数据', 1, '未直接抓取原始数据'),
('国家统计局棉花产量公告', '公开统计', 'https://www.stats.gov.cn/', '棉花面积、单产、产量合理性参考', '2026-05-25 00:00:00', '人工参考公开口径生成演示数据', 1, '用于新疆棉花场景合理性约束'),
('中国土壤数据库', '公开土壤资料', 'https://vdb3.soil.csdb.cn/', '土壤类型、pH、有机质等范围参考', '2026-05-25 00:00:00', '人工参考公开知识生成演示数据', 1, '土壤样本为模拟数据'),
('土壤科学数据中心', '公开土壤资料', 'https://soil.geodata.cn/', '土壤理化性质与空间数据资料参考', '2026-05-25 00:00:00', '人工参考公开知识生成演示数据', 1, '用于土壤风险阈值解释'),
('NASA POWER 气象 API', '公开气象服务', 'https://power.larc.nasa.gov/', '温度、降水、太阳辐射等气象变量', '2026-05-25 00:00:00', '按新疆典型地区气候范围模拟生成', 1, '气象记录为模拟数据，仅用于系统演示');

DELIMITER //

CREATE PROCEDURE seed_field_plots()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE v_region VARCHAR(64);
  DECLARE v_county VARCHAR(64);
  DECLARE v_lon DECIMAL(10,6);
  DECLARE v_lat DECIMAL(10,6);
  DECLARE v_ph DECIMAL(4,2);
  DECLARE v_organic DECIMAL(6,2);
  DECLARE v_se DECIMAL(7,3);
  DECLARE v_salinity DECIMAL(6,2);
  DECLARE v_ec DECIMAL(7,2);
  DECLARE v_heavy VARCHAR(32);
  DECLARE v_risk VARCHAR(32);
  WHILE i <= 30 DO
    SET v_region = CASE MOD(i - 1, 9)
      WHEN 0 THEN '乌鲁木齐'
      WHEN 1 THEN '昌吉'
      WHEN 2 THEN '石河子'
      WHEN 3 THEN '阿克苏'
      WHEN 4 THEN '喀什'
      WHEN 5 THEN '库尔勒'
      WHEN 6 THEN '吐鲁番'
      WHEN 7 THEN '哈密'
      ELSE '伊犁' END;
    SET v_county = CASE v_region
      WHEN '乌鲁木齐' THEN '米东区'
      WHEN '昌吉' THEN '呼图壁县'
      WHEN '石河子' THEN '一四三团'
      WHEN '阿克苏' THEN '温宿县'
      WHEN '喀什' THEN '疏勒县'
      WHEN '库尔勒' THEN '焉耆县'
      WHEN '吐鲁番' THEN '高昌区'
      WHEN '哈密' THEN '伊州区'
      ELSE '霍城县' END;
    SET v_lon = (CASE v_region
      WHEN '乌鲁木齐' THEN 87.620000
      WHEN '昌吉' THEN 87.310000
      WHEN '石河子' THEN 86.080000
      WHEN '阿克苏' THEN 80.260000
      WHEN '喀什' THEN 75.990000
      WHEN '库尔勒' THEN 86.150000
      WHEN '吐鲁番' THEN 89.190000
      WHEN '哈密' THEN 93.510000
      ELSE 81.320000 END) + MOD(i, 5) * 0.018;
    SET v_lat = (CASE v_region
      WHEN '乌鲁木齐' THEN 43.820000
      WHEN '昌吉' THEN 44.010000
      WHEN '石河子' THEN 44.310000
      WHEN '阿克苏' THEN 41.170000
      WHEN '喀什' THEN 39.470000
      WHEN '库尔勒' THEN 41.760000
      WHEN '吐鲁番' THEN 42.950000
      WHEN '哈密' THEN 42.820000
      ELSE 43.920000 END) + MOD(i, 4) * 0.014;
    SET v_ph = ROUND(7.20 + MOD(i * 3, 16) * 0.08 + IF(MOD(i, 17) = 0, 0.26, 0), 2);
    SET v_organic = ROUND(14.0 + MOD(i * 7, 150) / 10, 2);
    SET v_se = ROUND(0.150 + MOD(i * 17, 330) / 1000, 3);
    SET v_salinity = ROUND(0.65 + MOD(i * 7, 190) / 100, 2);
    SET v_ec = ROUND(v_salinity * 1.25 + MOD(i, 5) * 0.12, 2);
    SET v_heavy = CASE WHEN MOD(i, 23) = 0 THEN '高' WHEN MOD(i, 11) = 0 THEN '中' ELSE '低' END;
    SET v_risk = CASE
      WHEN v_heavy = '高' OR v_salinity >= 2.20 OR v_ec >= 3.20 THEN '高风险'
      WHEN v_heavy = '中' OR v_salinity >= 1.75 OR v_ph >= 8.35 THEN '中度风险'
      WHEN v_salinity >= 1.35 OR v_ph >= 8.15 OR v_se < 0.18 THEN '轻度预警'
      ELSE '适宜' END;
    INSERT INTO field_plot (
      plot_code, plot_name, region, county, town, longitude, latitude, area_mu, soil_type,
      ph_value, organic_matter, selenium_content, salinity, electrical_conductivity,
      heavy_metal_risk, risk_level, irrigation_type, manager_name, status, remark
    ) VALUES (
      CONCAT('XJ-PL-', LPAD(i, 3, '0')),
      CONCAT(v_region, '富硒示范田', LPAD(i, 2, '0')),
      v_region,
      v_county,
      CONCAT('现代农业示范区', MOD(i, 6) + 1, '号片区'),
      v_lon,
      v_lat,
      ROUND(75 + MOD(i * 17, 190) + MOD(i, 3) * 0.5, 2),
      CASE MOD(i, 5) WHEN 0 THEN '灰漠土' WHEN 1 THEN '灌淤土' WHEN 2 THEN '棕漠土' WHEN 3 THEN '风沙土' ELSE '草甸土' END,
      v_ph,
      v_organic,
      v_se,
      v_salinity,
      v_ec,
      v_heavy,
      v_risk,
      CASE MOD(i, 3) WHEN 0 THEN '膜下滴灌' WHEN 1 THEN '滴灌' ELSE '渠灌+滴灌' END,
      CASE MOD(i, 6) WHEN 0 THEN '艾合买提' WHEN 1 THEN '王建国' WHEN 2 THEN '李晓霞' WHEN 3 THEN '张天山' WHEN 4 THEN '古丽娜' ELSE '陈志远' END,
      CASE WHEN MOD(i, 11) = 0 THEN '监测中' ELSE '正常' END,
      CONCAT('模拟地块，风险等级依据 pH、盐分、电导率和重金属风险生成：', v_risk)
    );
    SET i = i + 1;
  END WHILE;
END//

CREATE PROCEDURE seed_crop_records()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE v_variety_id BIGINT;
  DECLARE v_crop_type VARCHAR(32);
  DECLARE v_variety_name VARCHAR(120);
  DECLARE v_days INT;
  DECLARE v_sowing DATE;
  WHILE i <= 50 DO
    SET v_variety_id = MOD(i - 1, 18) + 1;
    SELECT crop_type, variety_name, growth_period_days INTO v_crop_type, v_variety_name, v_days FROM crop_variety WHERE id = v_variety_id;
    SET v_sowing = CASE v_crop_type
      WHEN '棉花' THEN DATE_ADD('2026-04-05', INTERVAL MOD(i, 18) DAY)
      WHEN '小麦' THEN DATE_ADD('2025-09-20', INTERVAL MOD(i, 25) DAY)
      WHEN '玉米' THEN DATE_ADD('2026-04-20', INTERVAL MOD(i, 22) DAY)
      WHEN '葡萄' THEN DATE_ADD('2026-03-15', INTERVAL MOD(i, 14) DAY)
      WHEN '枸杞' THEN DATE_ADD('2026-03-25', INTERVAL MOD(i, 16) DAY)
      ELSE DATE_ADD('2026-04-10', INTERVAL MOD(i, 20) DAY) END;
    INSERT INTO crop_record (
      plot_id, variety_id, crop_type, variety_name, season_year, sowing_date, harvest_date,
      expected_harvest_date, planting_area_mu, irrigation_method, fertilizer_method,
      irrigation_mode, fertilizer_plan, yield_kg_mu, quality_level, growth_status, remark
    ) VALUES (
      MOD(i - 1, 30) + 1,
      v_variety_id,
      v_crop_type,
      v_variety_name,
      CASE WHEN v_crop_type = '小麦' AND MONTH(v_sowing) = 9 THEN 2025 ELSE 2026 END,
      v_sowing,
      DATE_ADD(v_sowing, INTERVAL IFNULL(v_days, 120) DAY),
      DATE_ADD(v_sowing, INTERVAL IFNULL(v_days, 120) DAY),
      ROUND(38 + MOD(i * 9, 150) + MOD(i, 4) * 0.25, 2),
      CASE MOD(i, 3) WHEN 0 THEN '膜下滴灌' WHEN 1 THEN '水肥一体化滴灌' ELSE '常规滴灌' END,
      CASE MOD(i, 4) WHEN 0 THEN '有机肥+氮磷钾平衡追肥' WHEN 1 THEN '基肥深施+花期追肥' WHEN 2 THEN '控氮稳磷增钾' ELSE '富硒示范小区叶面补硒' END,
      CASE MOD(i, 3) WHEN 0 THEN '膜下滴灌' WHEN 1 THEN '水肥一体化滴灌' ELSE '常规滴灌' END,
      CASE MOD(i, 4) WHEN 0 THEN '有机肥+氮磷钾平衡追肥' WHEN 1 THEN '基肥深施+花期追肥' WHEN 2 THEN '控氮稳磷增钾' ELSE '富硒示范小区叶面补硒' END,
      ROUND(CASE v_crop_type WHEN '棉花' THEN 420 WHEN '小麦' THEN 510 WHEN '玉米' THEN 780 WHEN '葡萄' THEN 1650 WHEN '枸杞' THEN 310 ELSE 720 END + MOD(i * 17, 120), 2),
      CASE MOD(i, 4) WHEN 0 THEN '优' WHEN 1 THEN '良' WHEN 2 THEN '中' ELSE '优' END,
      CASE MOD(i, 5) WHEN 0 THEN '苗期监测' WHEN 1 THEN '营养生长期' WHEN 2 THEN '开花坐果期' WHEN 3 THEN '灌浆膨大期' ELSE '采收准备期' END,
      '模拟种植档案，关联地块用于展示土壤硒、pH 和风险等级'
    );
    SET i = i + 1;
  END WHILE;
END//

CREATE PROCEDURE seed_soil_samples()
BEGIN
  DECLARE i INT DEFAULT 1;
  WHILE i <= 100 DO
    INSERT INTO soil_sample (
      plot_id, sample_code, sample_date, ph_value, organic_matter, available_nitrogen,
      available_phosphorus, available_potassium, selenium_content, salinity,
      electrical_conductivity, heavy_metal_risk, risk_level, soil_moisture, sampler, remark
    )
    SELECT
      p.id,
      CONCAT('SOIL-', DATE_FORMAT(DATE_ADD('2025-03-01', INTERVAL MOD(i * 5, 430) DAY), '%Y%m%d'), '-', LPAD(i, 3, '0')),
      DATE_ADD('2025-03-01', INTERVAL MOD(i * 5, 430) DAY),
      ROUND(p.ph_value + (MOD(i, 5) - 2) * 0.03, 2),
      ROUND(p.organic_matter + (MOD(i, 7) - 3) * 0.25, 2),
      ROUND(62 + MOD(i * 11, 65), 2),
      ROUND(12 + MOD(i * 5, 24), 2),
      ROUND(135 + MOD(i * 13, 130), 2),
      ROUND(p.selenium_content + (MOD(i, 9) - 4) * 0.006, 3),
      ROUND(p.salinity + (MOD(i, 6) - 2) * 0.04, 2),
      ROUND(p.electrical_conductivity + (MOD(i, 4) - 1) * 0.06, 2),
      p.heavy_metal_risk,
      p.risk_level,
      ROUND(12.0 + MOD(i * 9, 170) / 10, 2),
      CASE MOD(i, 5) WHEN 0 THEN '张天山' WHEN 1 THEN '古丽娜' WHEN 2 THEN '王建国' WHEN 3 THEN '李晓霞' ELSE '艾合买提' END,
      '模拟土壤样本，仅用于系统演示'
    FROM field_plot p
    WHERE p.id = MOD(i - 1, 30) + 1;
    SET i = i + 1;
  END WHILE;
END//

CREATE PROCEDURE seed_weather_records()
BEGIN
  DECLARE i INT DEFAULT 1;
  DECLARE v_region VARCHAR(64);
  DECLARE v_week INT;
  WHILE i <= 90 DO
    SET v_region = CASE MOD(i - 1, 9)
      WHEN 0 THEN '乌鲁木齐'
      WHEN 1 THEN '昌吉'
      WHEN 2 THEN '石河子'
      WHEN 3 THEN '阿克苏'
      WHEN 4 THEN '喀什'
      WHEN 5 THEN '库尔勒'
      WHEN 6 THEN '吐鲁番'
      WHEN 7 THEN '哈密'
      ELSE '伊犁' END;
    SET v_week = FLOOR((i - 1) / 9);
    INSERT INTO weather_record (region, record_date, avg_temp, max_temp, min_temp, precipitation, sunshine_hours, solar_radiation, wind_speed, humidity)
    VALUES (
      v_region,
      DATE_ADD('2026-03-01', INTERVAL v_week * 7 DAY),
      ROUND(6.5 + v_week * 1.8 + MOD(i, 5) * 0.7 + IF(v_region IN ('吐鲁番','哈密'), 2.5, 0), 2),
      ROUND(14.0 + v_week * 2.0 + MOD(i, 4) * 0.9 + IF(v_region IN ('吐鲁番','哈密'), 3.2, 0), 2),
      ROUND(0.2 + v_week * 1.2 + MOD(i, 3) * 0.4, 2),
      ROUND(CASE WHEN v_region IN ('吐鲁番','哈密','库尔勒') THEN MOD(i, 4) * 0.6 ELSE MOD(i * 3, 11) * 0.8 END, 2),
      ROUND(7.2 + MOD(i * 2, 35) / 10, 2),
      ROUND(13.5 + v_week * 1.1 + MOD(i, 6) * 0.6, 2),
      ROUND(1.2 + MOD(i * 5, 22) / 10, 2),
      ROUND(CASE WHEN v_region IN ('吐鲁番','哈密') THEN 28 + MOD(i * 7, 18) ELSE 38 + MOD(i * 5, 28) END, 2)
    );
    SET i = i + 1;
  END WHILE;
END//

DELIMITER ;

CALL seed_field_plots();
CALL seed_crop_records();
CALL seed_soil_samples();
CALL seed_weather_records();

DROP PROCEDURE seed_field_plots;
DROP PROCEDURE seed_crop_records;
DROP PROCEDURE seed_soil_samples;
DROP PROCEDURE seed_weather_records;

INSERT INTO selenium_prediction (
  plot_id, crop_record_id, soil_sample_id, crop_type, variety_name, soil_selenium, ph_value,
  organic_matter, salinity, electrical_conductivity, avg_temperature, precipitation,
  irrigation_method, fertilizer_method, predicted_selenium, quality_level, confidence,
  risk_level, model_version, factor_contribution, factor_explanation, suggestion,
  input_signature, created_by
)
SELECT p.id, c.id, s.id, c.crop_type, c.variety_name, p.selenium_content, p.ph_value,
       p.organic_matter, p.salinity, p.electrical_conductivity, 22.60, 4.80,
       c.irrigation_method, c.fertilizer_method,
       CASE WHEN p.selenium_content > 0.42 THEN 0.1120 WHEN p.selenium_content > 0.26 THEN 0.0720 ELSE 0.0380 END,
       CASE WHEN p.selenium_content > 0.42 THEN '高硒风险' WHEN p.selenium_content > 0.26 THEN '富硒' WHEN p.selenium_content > 0.18 THEN '普通' ELSE '低硒' END,
       0.8600,
       p.risk_level,
       'XJ-Se-Explainable-0.2',
       '[{"factor":"土壤硒含量","contribution":46},{"factor":"pH","contribution":15},{"factor":"有机质","contribution":14},{"factor":"盐分/电导率","contribution":13},{"factor":"气象适宜性","contribution":12}]',
       '初始化演示预测：土壤硒含量为主要正向因素，盐分和电导率为主要抑制因素。',
       '建议保持水肥一体化管理，采收前复测产品硒含量。',
       SHA2(CONCAT(p.id, '-', c.id, '-', s.id), 256),
       'admin'
FROM field_plot p
JOIN crop_record c ON c.plot_id = p.id
JOIN soil_sample s ON s.plot_id = p.id
WHERE p.id IN (1, 5, 10, 18)
GROUP BY p.id, c.id, s.id
ORDER BY p.id
LIMIT 4;

INSERT INTO soil_assessment (
  plot_id, soil_sample_id, assessment_score, risk_level, ph_level, fertility_level, selenium_level,
  salinity_level, conductivity_level, heavy_metal_level, item_evaluation, radar_json,
  constraint_factor, overall_comment, improvement_advice, created_by
)
SELECT p.id, s.id,
       CASE p.risk_level WHEN '适宜' THEN 88.00 WHEN '轻度预警' THEN 76.00 WHEN '中度风险' THEN 63.00 ELSE 48.00 END,
       p.risk_level,
       CASE WHEN p.ph_value BETWEEN 6.8 AND 8.2 THEN '适宜' WHEN p.ph_value < 6.8 THEN '偏酸' ELSE '偏碱' END,
       CASE WHEN p.organic_matter >= 22 THEN '高' WHEN p.organic_matter >= 16 THEN '中' ELSE '低' END,
       CASE WHEN p.selenium_content >= 0.35 THEN '富硒背景' WHEN p.selenium_content >= 0.18 THEN '中等硒背景' ELSE '低硒背景' END,
       CASE WHEN p.salinity <= 1.30 THEN '适宜' WHEN p.salinity <= 1.80 THEN '轻度偏高' ELSE '偏高' END,
       CASE WHEN p.electrical_conductivity <= 2.00 THEN '适宜' WHEN p.electrical_conductivity <= 3.00 THEN '轻度偏高' ELSE '偏高' END,
       p.heavy_metal_risk,
       JSON_OBJECT('pH', p.ph_value, '有机质', p.organic_matter, '土壤硒', p.selenium_content, '盐分', p.salinity, '电导率', p.electrical_conductivity, '重金属风险', p.heavy_metal_risk),
       JSON_ARRAY(ROUND(100 - ABS(p.ph_value - 7.4) * 18, 2), LEAST(100, p.organic_matter * 4), LEAST(100, p.selenium_content * 220), GREATEST(20, 100 - p.salinity * 20), GREATEST(20, 100 - p.electrical_conductivity * 16), CASE p.heavy_metal_risk WHEN '低' THEN 90 WHEN '中' THEN 62 ELSE 35 END),
       CASE WHEN p.risk_level = '适宜' THEN '暂无显著限制因子' ELSE CONCAT('主要限制因子：', p.risk_level, '，需关注盐分、电导率或重金属风险') END,
       CONCAT(p.region, p.plot_name, '土壤综合风险等级为', p.risk_level, '。'),
       CASE WHEN p.risk_level = '适宜' THEN '保持秸秆还田、滴灌和常规监测。' ELSE '建议开展滴灌压盐、有机质提升和风险点复检，必要时调整种植作物。' END,
       'admin'
FROM field_plot p
JOIN soil_sample s ON s.plot_id = p.id
WHERE p.id IN (1, 5, 10, 18, 23)
GROUP BY p.id, s.id
ORDER BY p.id
LIMIT 5;

INSERT INTO decision_suggestion (plot_id, crop_record_id, suggestion_type, title, content, priority, status, generated_by)
SELECT p.id, c.id, '富硒种植建议',
       CONCAT(p.region, c.crop_type, '富硒种植建议'),
       CONCAT(p.plot_name, '土壤硒含量 ', p.selenium_content, ' mg/kg，风险等级 ', p.risk_level, '。建议结合', c.irrigation_method, '与', c.fertilizer_method, '开展分区管理。'),
       CASE WHEN p.risk_level IN ('高风险','中度风险') THEN '风险' WHEN p.risk_level = '轻度预警' THEN '谨慎' ELSE '推荐' END,
       '待处理',
       'admin'
FROM field_plot p
JOIN crop_record c ON c.plot_id = p.id
WHERE p.id IN (1, 7, 12)
GROUP BY p.id, c.id
ORDER BY p.id
LIMIT 3;

INSERT INTO report_record (report_name, report_type, related_plot_id, file_path, summary, content, source_summary, created_by)
VALUES
('新疆特色作物富硒品质监测月报', '智慧农业综合决策报告', NULL, '/reports/demo-monthly.html', '汇总地块、土壤、气象、预测和建议记录，形成示范区监测摘要。',
'<h1>新疆特色作物富硒品质监测月报</h1><p>本报告为系统初始化演示报告，包含地块概况、土壤评估、富硒预测与决策建议摘要。</p>',
'统计、土壤和气象来源均以公开资料为参考，初始化业务数据为模拟数据。', 'admin');
