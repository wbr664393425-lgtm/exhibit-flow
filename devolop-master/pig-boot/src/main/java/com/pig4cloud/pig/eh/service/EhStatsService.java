package com.pig4cloud.pig.eh.service;

import com.pig4cloud.pig.eh.vo.EhStatsCityVO;
import com.pig4cloud.pig.eh.vo.EhStatsExportVO;
import com.pig4cloud.pig.eh.vo.EhStatsFunnelVO;
import com.pig4cloud.pig.eh.vo.EhStatsIndustryVO;
import com.pig4cloud.pig.eh.vo.EhStatsKpiVO;
import com.pig4cloud.pig.eh.vo.EhStatsMonthlyVO;
import com.pig4cloud.pig.eh.vo.EhStatsStrategicVO;

import java.util.List;

public interface EhStatsService {

	List<EhStatsKpiVO> getKpis();

	List<EhStatsMonthlyVO> getMonthly();

	List<EhStatsCityVO> getCityDistribution();

	List<EhStatsIndustryVO> getIndustryDistribution();

	List<EhStatsStrategicVO> getStrategicDistribution();

	List<EhStatsFunnelVO> getFunnel();

	List<EhStatsExportVO> exportStatsRows();
}
