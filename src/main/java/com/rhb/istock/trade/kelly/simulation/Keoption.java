package com.rhb.istock.trade.kelly.simulation;

import java.math.BigDecimal;

public class Keoption {
	private BigDecimal deficitFactor;
	private Integer openDuration;
	private Integer dropDuration;
	private Integer maxOfLot;
	private BigDecimal initCash;
	private Integer stopStrategy;
	private Integer cancels; 	//多少天后，如果还不涨，取消持股
	private Integer gap;

	
	
	public Integer getCancels() {
		return cancels;
	}
	public void setCancels(Integer cancels) {
		this.cancels = cancels;
	}
	public BigDecimal getDeficitFactor() {
		return deficitFactor;
	}
	public void setDeficitFactor(BigDecimal deficitFactor) {
		this.deficitFactor = deficitFactor;
	}
	public Integer getOpenDuration() {
		return openDuration;
	}
	public void setOpenDuration(Integer openDuration) {
		this.openDuration = openDuration;
	}
	public Integer getDropDuration() {
		return dropDuration;
	}
	public void setDropDuration(Integer dropDuration) {
		this.dropDuration = dropDuration;
	}
	public Integer getMaxOfLot() {
		return maxOfLot;
	}
	public void setMaxOfLot(Integer maxOfLot) {
		this.maxOfLot = maxOfLot;
	}
	public BigDecimal getInitCash() {
		return initCash;
	}
	public void setInitCash(BigDecimal initCash) {
		this.initCash = initCash;
	}
	public Integer getStopStrategy() {
		return stopStrategy;
	}
	public void setStopStrategy(Integer stopStrategy) {
		this.stopStrategy = stopStrategy;
	}
	public Integer getGap() {
		return gap;
	}
	public void setGap(Integer gap) {
		this.gap = gap;
	}
}
