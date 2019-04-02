package com.rhb.istock.trade.turtle.domain;

import java.math.BigDecimal;


/**
 * openHigh和openLow是 openDuration期间的高点和低点，当前价now如果突破openHigh，则做多，如果跌破openLow，则做空。
 * dropHigh和dropLow时dropDuration期间的高点和低点，当前价now如果跌破dropLow时，则多头平仓，如果突破dropHigh，则空头平仓
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
public class Tfeature {
	private String itemID;
	private BigDecimal openHigh;
	private BigDecimal openLow;
	private Integer hlgap;
	private Integer nhgap;
	private Integer nlgap;
	private BigDecimal dropHigh;
	private BigDecimal dropLow;
	private BigDecimal now;
	private BigDecimal atr;
	private Integer status;
	
	public Tfeature(String itemID) {
		this.itemID = itemID;
	}
	
	public void reset() {
		try {
			nhgap = now.subtract(openHigh).divide(openHigh,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).abs().intValue();
			nlgap = now.subtract(openLow).divide(openLow,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).abs().intValue();
		}catch(Exception e) {
			System.out.println(this.toString());
			e.printStackTrace();
		}
		
		if(now.compareTo(openHigh)>=0) {
			status = 2;
		}else if(now.compareTo(openHigh)==-1 && now.compareTo(dropLow)>=0) {
			status = 1;
		}else if(now.compareTo(openLow)>=0 && now.compareTo(dropHigh)==-1) {
			status = -1;
		}else {
			status = -2;
		}
	}
	
	public BigDecimal getOpenHigh() {
		return openHigh;
	}
	public void setOpenHigh(BigDecimal openHigh) {
		this.openHigh = openHigh;
	}
	public BigDecimal getOpenLow() {
		return openLow;
	}
	public void setOpenLow(BigDecimal openLow) {
		this.openLow = openLow;
	}

	public Integer getHlgap() {
		return hlgap;
	}

	public void setHlgap(Integer hlgap) {
		this.hlgap = hlgap;
	}

	public Integer getNhgap() {
		return nhgap;
	}

	public void setNhgap(Integer nhgap) {
		this.nhgap = nhgap;
	}

	public Integer getNlgap() {
		return nlgap;
	}

	public void setNlgap(Integer nlgap) {
		this.nlgap = nlgap;
	}

	public BigDecimal getDropHigh() {
		return dropHigh;
	}
	public void setDropHigh(BigDecimal dropHigh) {
		this.dropHigh = dropHigh;
	}
	public BigDecimal getDropLow() {
		return dropLow;
	}
	public void setDropLow(BigDecimal dropLow) {
		this.dropLow = dropLow;
	}
	public BigDecimal getNow() {
		return now;
	}
	public void setNow(BigDecimal now) {
		this.now = now;
	}
	
	public BigDecimal getAtr() {
		return atr;
	}
	public void setAtr(BigDecimal atr) {
		this.atr = atr;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	@Override
	public String toString() {
		return "Feature [itemID=" + itemID + ", openHigh=" + openHigh + ", openLow=" + openLow + ", hlgap=" + hlgap
				+ ", nhgap=" + nhgap + ", nlgap=" + nlgap + ", dropHigh=" + dropHigh + ", dropLow=" + dropLow + ", now="
				+ now + ", atr=" + atr + ", status=" + status + "]";
	}
	
	
	
	
	
}
