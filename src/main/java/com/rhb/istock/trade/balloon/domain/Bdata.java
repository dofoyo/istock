package com.rhb.istock.trade.balloon.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class Bdata {
	protected static final Logger logger = LoggerFactory.getLogger(Bdata.class);
	private String itemID;
	private Integer tradeDuration;
	private Integer upDuration;
	private Integer midDuration;
	private Integer latestDuration;
	private Integer upLine;
	private Integer baseLine;
	private TreeSet<Bbar> bars;
	private Integer buyValue;
	private Integer minSlip;
	private Integer maxSlip;
	private Integer buyMoreThan;
	
	public Bdata(String itemID, 
			Integer tradeDuration, 
			Integer upDuration, 
			Integer midDuration,
			Integer latestDuration,
			Integer upLine,
			Integer baseLine,
			Integer buyValue,
			Integer minSlip,
			Integer maxSlip,
			Integer buyMoreThan) {
		this.itemID = itemID;
		this.tradeDuration = tradeDuration;
		this.upDuration = upDuration;
		this.midDuration = midDuration;
		this.latestDuration = latestDuration;
		this.upLine = upLine;
		this.baseLine = baseLine;
		this.buyValue = buyValue;
		this.minSlip = minSlip;
		this.maxSlip = maxSlip;
		this.buyMoreThan = buyMoreThan;
		
		this.bars = new TreeSet<Bbar>(new BbarComparator());
	}
	
	public void addBar(LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
		Bbar bar = new Bbar(date,open,high,low,close);
		this.bars.add(bar);
		if(this.bars.size()>midDuration) {
			this.bars.pollFirst();
		}
		
		bar.setUpLinePrice(getLinePrice(upLine));
		bar.setBaseLinePrice(getLinePrice(baseLine));
		bar.setMidPrice(getGoldenPrice(midDuration));
	}
	
	public void clearBars() {
		this.bars = new TreeSet<Bbar>();
	}
	
	public Bfeature getFeature() {
		Bfeature feature = new Bfeature(itemID);
		Bbar latestBar = this.bars.last();
		feature.setNow(latestBar.getClose());
		feature.setUpLinePrice(latestBar.getUpLinePrice());
		feature.setBaseLinePrice(latestBar.getBaseLinePrice());
		feature.setGoldenPrice(latestBar.getGoldenPrice());
		feature.setTradeDays(this.bars.size());
		feature.setSlips(getSlips(latestDuration));
		feature.setUps(getUps(upDuration));
		feature.setBuyValue(buyValue);
		feature.setMinSlip(minSlip);
		feature.setMaxSlip(maxSlip);
		feature.setTradeDuration(tradeDuration);
		feature.setYzb(latestBar.isYzb());
		feature.setBuyMoreThan(buyMoreThan);
		
		return feature;
	}
	
	private Integer getUps(Integer duration) {
		Integer fromIndex = this.bars.size()> duration ? this.bars.size()- duration : 0;
		List<Bbar> sublist = getBbars(fromIndex, this.bars.size());
		Integer count = 0;
		
		//StringBuffer sb = new StringBuffer();
		for(Bbar bar : sublist) {
			if(bar.isAboveUpLine()) {
				count++;
				//sb.append(bar.getDate()+"("+bar.getClose() +">" + bar.getUpLinePrice() +")"+ "=" + count + ",");
			}
		}
		//System.out.println(sb.toString());
		return count;
	}
	
	private Integer getSlips(Integer duration) {
		Integer fromIndex = this.bars.size()> duration ? this.bars.size()- duration : 0;
		List<Bbar> sublist = getBbars(fromIndex, this.bars.size());
		Integer count = 0;
		for(Bbar bar : sublist) {
			if(bar.isSlip()) {
				count++;
			}
		}
		return count;
	}
	
	private BigDecimal getGoldenPrice(Integer duration) {
		Integer fromIndex = this.bars.size()> duration ? this.bars.size()- duration : 0;
		List<Bbar> sublist = getBbars(fromIndex, this.bars.size());
		Set<BigDecimal> prices = new HashSet<BigDecimal>();
		for(Bbar bar : sublist) {
			prices.add(bar.getClose());
		}
		
		List<BigDecimal> list = new ArrayList<BigDecimal>(prices);
		Collections.sort(list);
		
		return list.get(new BigDecimal(prices.size()).multiply(new BigDecimal(0.618)).intValue());	
	}
	
	private BigDecimal getLinePrice(Integer line) {
		Integer fromIndex = this.bars.size()> line ? this.bars.size()-line : 0;
		List<Bbar> sublist = getBbars(fromIndex, this.bars.size());
		BigDecimal total = new BigDecimal(0);
		for(Bbar bar : sublist) {
			total = total.add(bar.getClose());
		}
		return total.divide(new BigDecimal(sublist.size()),BigDecimal.ROUND_HALF_UP);
	}

	private List<Bbar> getBbars(Integer fromIndex, Integer toIndex){
		return new ArrayList<Bbar>(this.bars).subList(fromIndex, toIndex);
	}
	
	public boolean isEmpty() {
		return this.bars.isEmpty();
	}
	
	class BbarComparator implements Comparator<Bbar>{
		@Override
		public int compare(Bbar o1, Bbar o2) {
			return o1.getDate().compareTo(o2.getDate());
		}

	}
}
