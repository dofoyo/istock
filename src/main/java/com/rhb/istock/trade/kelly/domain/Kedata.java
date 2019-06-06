package com.rhb.istock.trade.kelly.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * openHigh和openLow是 openDuration期间的高点和低点，当前价now如果突破openHigh，则做多，如果跌破openLow，则做空。
 * dropHigh和dropLow时dropDuration期间的高点和低点，当前价now如果跌破dropLow时，则多头平仓，如果突破dropHigh，则空头平仓
 * 
 * 
 * addBar -- 新增历史数据
 * 	openHigh, openLow, hlgap, dropHigh, dropLow会变化
 * 
 * 
 * setLastestBar -- 设置实时数据
 *  now, nhgap, nlgap, status会变化
 * 
 * 
 * hlgap：高点比低点高出的百分百
 * nhgap: 当前价位比高点高出的百分百，为正表示当前价高于高点，向上突破
 * nlgap: 当前价位比低点低出的百分百，为正表示当前价低于低点，向下突破
 * status: 
 * 	2 -- 表示当前价位高于高点high，做多
 *  1 -- 表示当前价位低于高点high，高于dropLow,空头平仓
 * -1 -- 表示当前价位高于低点low，低于dropHigh，多头平仓
 * -2 -- 表示当前价位低于低点low，做空
 * 
 */
public class Kedata {
	protected static final Logger logger = LoggerFactory.getLogger(Kedata.class);
	private String itemID;
	private Integer openDuration;
	private Integer dropDuration;
	private List<Kebar> bars;
	private Kebar latestBar;
	private Kefeature feature;
	
	public Kedata(String itemID, Integer openDuration, Integer dropDuration) {
		this.itemID = itemID;
		this.openDuration = openDuration;
		this.dropDuration = dropDuration;
		this.bars = new ArrayList<Kebar>();
		this.feature = new Kefeature(itemID);
	}
	
	//一字板
	public boolean yzb() {
		if(latestBar==null) {
			return true;
		}else {
			return latestBar.getHigh().compareTo(latestBar.getLow())==0;
		}
	}
	
	public void addBar(LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
		Kebar bar = new Kebar(date,open,high,low,close);
		if(this.bars.size()==0) {
			bar.setTr(getTR(bar.getHigh(), bar.getLow(), bar.getClose()));
		}else {
			Kebar preBar = this.bars.get(this.bars.size()-1);
			BigDecimal preClose = preBar.getClose();
			bar.setTr(getTR(bar.getHigh(), bar.getLow(), preClose));
		}
		
		this.bars.add(bar);
		if(this.bars.size()>openDuration) {
			this.bars.remove(0);
		}
	}
	
	/*
	 * 计算真实波动幅度
	 */
	private BigDecimal getTR(BigDecimal high, BigDecimal low, BigDecimal preClose) {
		BigDecimal h_l = high.subtract(low);
		BigDecimal h_p = high.subtract(preClose);
		BigDecimal p_l = preClose.subtract(low);
		
		BigDecimal max = h_l.compareTo(h_p)>0 ? h_l : h_p;
		max = max.compareTo(p_l)>0 ? max : p_l;
		
		return max;
	}	
	
	private BigDecimal getDropPrice(Integer duration) {
		BigDecimal dropPrice = null;
		BigDecimal total = new BigDecimal(0);
		Integer fromIndex = this.bars.size()>duration ? this.bars.size()-duration : 0;
		Integer toIndex = this.bars.size();
		List<Kebar> subBars = this.bars.subList(fromIndex, toIndex);
		for(Kebar bar : subBars) {
			total = total.add(bar.getClose());
		}
		dropPrice = total.divide(new BigDecimal(subBars.size()),BigDecimal.ROUND_HALF_UP);
		
		return dropPrice;
	}
	
	
/*	private BigDecimal[] getHighestAndLowest(Integer duration) {
		Integer fromIndex = this.bars.size()>duration ? this.bars.size()-duration : 0;
		Integer toIndex = this.bars.size();
		List<Tbar> subBars = this.bars.subList(fromIndex, toIndex);
		
		BigDecimal highest = new BigDecimal(-1000000);
		BigDecimal lowest = new BigDecimal(1000000);
		for(Tbar bar : subBars) {
			if(bar.getHigh().compareTo(highest)>0) {
				highest = bar.getHigh();
			}
			
			if(bar.getLow().compareTo(lowest)<0
					//&& bar.getLow().compareTo(new BigDecimal(0))==1
					) {
				lowest = bar.getLow();
			}
		}
		BigDecimal rate = highest.subtract(lowest).divide(lowest,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

		return new BigDecimal[]{highest,lowest,rate}; 
	}*/
	
	private Map<String,String> getHighestAndLowest(Integer duration) {
		Map<String,String> result = new HashMap<String,String>();
		
		Integer fromIndex = this.bars.size()>duration ? this.bars.size()-duration : 0;
		Integer toIndex = this.bars.size();
		List<Kebar> subBars = this.bars.subList(fromIndex, toIndex);
		
		LocalDate highestDate = null;
		BigDecimal highest = new BigDecimal(-1000000);
		BigDecimal lowest = new BigDecimal(1000000);
		for(Kebar bar : subBars) {
			if(bar.getHigh().compareTo(highest)>0) {
				highest = bar.getHigh();
				highestDate = bar.getDate();
			}
			
			if(bar.getLow().compareTo(lowest)<0
					//&& bar.getLow().compareTo(new BigDecimal(0))==1
					) {
				lowest = bar.getLow();
			}
		}
		BigDecimal rate = highest.subtract(lowest).divide(lowest,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
		
		result.put("highestPrice", highest.toString());
		result.put("highestDate", highestDate.toString());
		result.put("lowestPrice", lowest.toString());
		result.put("rate", rate.toString());
		
		return result; 
	}
	
	
	public void setLatestBar(LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
		latestBar = new Kebar(date,open,high,low,close);
		feature.setNow(close);
		feature.setNowDate(date);
	}
	
	public Kefeature getFeature() {
		if(this.latestBar==null || this.bars.size()<openDuration) return null;
		
		Map<String,String> hl = getHighestAndLowest(openDuration);
		this.feature.setOpenHighDate(LocalDate.parse(hl.get("highestDate")));
		this.feature.setOpenHigh(new BigDecimal(hl.get("highestPrice")));
		this.feature.setOpenLow(new BigDecimal(hl.get("lowestPrice")));
		this.feature.setHlgap(new BigDecimal(hl.get("rate")).intValue());
		
		hl = getHighestAndLowest(dropDuration);
		this.feature.setDropHigh(new BigDecimal(hl.get("highestPrice")));
		this.feature.setDropLow(new BigDecimal(hl.get("lowestPrice")));
		
		this.feature.setDropPrice(getDropPrice(dropDuration));
		
		this.feature.setAtr(getATR());
		
		this.feature.reset();
		
		return this.feature;
	}

	public void clearBars() {
		this.bars = new ArrayList<Kebar>();
		this.feature = new Kefeature(itemID);
	}
	
	//----------
	

	
	public Kebar getLatestBar() {
		return this.latestBar;
	}
	

	
	public List<Kebar> getTbars(){
		return bars;
	}
	
	public String getItemID() {
		return itemID;
	}

	
	/*
	 * 计算平均波动幅度
	 */
	public BigDecimal getATR() {
		BigDecimal sum_tr = new BigDecimal(0);
		for(Kebar bar : bars) {
			sum_tr = sum_tr.add(bar.getTr());
		}
		return sum_tr.divide(new BigDecimal(this.bars.size()),BigDecimal.ROUND_HALF_UP); 
	}
	

	public Integer getOpenDuration() {
		return openDuration;
	}

	public Integer getDropDuration() {
		return dropDuration;
	}

}
