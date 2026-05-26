package com.xj.agri.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xj.agri.dto.CropRecordView;
import com.xj.agri.entity.CropRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CropRecordMapper extends BaseMapper<CropRecord> {
    @Select("""
            <script>
            SELECT
              c.id,
              c.plot_id AS plotId,
              p.plot_name AS plotName,
              p.region AS region,
              p.selenium_content AS plotSeleniumContent,
              p.ph_value AS plotPhValue,
              p.risk_level AS plotRiskLevel,
              c.variety_id AS varietyId,
              c.crop_type AS cropType,
              c.variety_name AS varietyName,
              c.season_year AS seasonYear,
              c.sowing_date AS sowingDate,
              c.harvest_date AS harvestDate,
              c.expected_harvest_date AS expectedHarvestDate,
              c.planting_area_mu AS plantingAreaMu,
              c.irrigation_method AS irrigationMethod,
              c.fertilizer_method AS fertilizerMethod,
              c.yield_kg_mu AS yieldKgMu,
              c.quality_level AS qualityLevel,
              c.growth_status AS growthStatus,
              c.remark,
              c.created_at AS createdAt,
              c.updated_at AS updatedAt
            FROM crop_record c
            JOIN field_plot p ON p.id = c.plot_id
            <where>
              <if test="keyword != null and keyword != ''">
                AND (c.crop_type LIKE CONCAT('%', #{keyword}, '%')
                  OR c.variety_name LIKE CONCAT('%', #{keyword}, '%')
                  OR p.plot_name LIKE CONCAT('%', #{keyword}, '%')
                  OR p.region LIKE CONCAT('%', #{keyword}, '%'))
              </if>
              <if test="cropType != null and cropType != ''">
                AND c.crop_type = #{cropType}
              </if>
              <if test="plotId != null">
                AND c.plot_id = #{plotId}
              </if>
            </where>
            ORDER BY c.season_year DESC, c.id DESC
            </script>
            """)
    Page<CropRecordView> selectViewPage(Page<CropRecordView> page,
                                        @Param("keyword") String keyword,
                                        @Param("cropType") String cropType,
                                        @Param("plotId") Long plotId);
}
