package com.rhb.istock.trade.balloon.domain;

import java.math.BigDecimal;

public class Bfeature {
	private String itemID;
	private BigDecimal now;
	private BigDecimal upLinePrice;
	private BigDecimal baseLinePrice;
	private BigDecimal goldenPrice;
	private Integer tradeDays;
	private Integer slips;
	private Integer ups;
	private Integer buyValue;
	private Integer minSlip;
	private Integer maxSlip;
	private Integer tradeDuration;
	private boolean yzb;
	
	private Integer reopenRatio;
	private Integer stopLossRatio;		//最近一次买入下跌8%以上，止损;
	private Integer stopWinRatio;		//买入后，股价从最高点下跌超过13%，止盈;
	private BigDecimal latestOpenPrice = new BigDecimal(0);
	private BigDecimal highestPriceOfHold = new BigDecimal(0);

	public Bfeature(String itemID) {
		this.itemID = itemID;
	}
	
	public Integer getStatus() {
		Integer status = 0;
		if(isStopLoss() || isStopWin()) {
			status = -2;
		}
		if(slips>=maxSlip) {					//近10个交易日股价低于upline均线
			status = -1;
		}
		if(status==0 &&
			ups > buyValue &&  					
			now.compareTo(upLinePrice)==1 && 		//股价高于upLine
			this.getBiasBaseLine()<13 &&			//股价高于baseLine不超过8%
			this.getBiasOfUpLineAndBaseLine()>0 &&  //upLine高于baseLine
			Math.abs(this.getBiasOfGolden()) <13 && //股价与黄金价位的距离在13% 以内
			slips<minSlip && 						//近10个交易日低于60日均线的次数  < minSlip
			tradeDays>=tradeDuration &&				//新股不能买（ipo日期起算，300天后才能买）
			!yzb) 									//当天股价没有一字板
		{
			if(!hasHolds()) {
				status = 1;
			}else if(isReopen()){
				status = 2;
			}
		}
		return status;
	}
	
	
	public Integer getStopLossRatio() {
		return stopLossRatio;
	}

	public void setStopLossRatio(Integer stopLossRatio) {
		this.stopLossRatio = stopLossRatio;
	}

	public Integer getStopWinRatio() {
		return stopWinRatio;
	}

	public void setStopWinRatio(Integer stopWinRatio) {
		this.stopWinRatio = stopWinRatio;
	}

	public BigDecimal getHighestPriceOfHold() {
		return highestPriceOfHold;
	}

	public BigDecimal getHighestPriceAfterOpen() {
		return highestPriceOfHold;
	}

	public void setHighestPriceOfHold(BigDecimal getHighestPriceOfHold) {
		this.highestPriceOfHold = getHighestPriceOfHold;
	}

	public boolean hasHolds() {
		return latestOpenPrice.compareTo(new BigDecimal(0))==1;
	}
	
	public boolean isReopen() {
		return now.subtract(latestOpenPrice).divide(latestOpenPrice,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue() > reopenRatio;
	}
	
	public boolean isStopLoss() {
		if(latestOpenPrice.equals(BigDecimal.ZERO)) return false;
		return now.subtract(latestOpenPrice).divide(latestOpenPrice,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue() < stopLossRatio;
	}
	
	public boolean isStopWin() {
		if(highestPriceOfHold.equals(BigDecimal.ZERO)) return false;
		return now.subtract(highestPriceOfHold).divide(highestPriceOfHold,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue() < stopWinRatio;
	}
	
	public Integer getReopenRatio() {
		return reopenRatio;
	}

	public void setReopenRatio(Integer reopenRatio) {
		this.reopenRatio = reopenRatio;
	}

	public BigDecimal getLatestOpenPrice() {
		return latestOpenPrice;
	}

	public BigDecimal isLatestOpenPrice() {
		return latestOpenPrice;
	}

	public void setLatestOpenPrice(BigDecimal latestOpenPrice) {
		this.latestOpenPrice = latestOpenPrice;
	}

	public boolean isYzb() {
		return yzb;
	}

	public void setYzb(boolean yzb) {
		this.yzb = yzb;
	}

	public Integer getTradeDuration() {
		return tradeDuration;
	}

	public void setTradeDuration(Integer tradeDuration) {
		this.tradeDuration = tradeDuration;
	}

	public Integer getBuyValue() {
		return buyValue;
	}

	public void setBuyValue(Integer buyValue) {
		this.buyValue = buyValue;
	}

	public Integer getMinSlip() {
		return minSlip;
	}

	public void setMinSlip(Integer minSlip) {
		this.minSlip = minSlip;
	}

	public Integer getMaxSlip() {
		return maxSlip;
	}

	public void setMaxSlip(Integer maxSlip) {
		this.maxSlip = maxSlip;
	}
	
	public boolean isUp() {
		//System.out.format("%d-%d-%d=%d>%d\n",upProbability,getBiasBaseLIne(),getBiasOfMidPrice(),(upProbability-getBiasBaseLIne()-getBiasOfMidPrice()),buyValue);
		return (ups-getBiasBaseLine()-getBiasOfGolden())>buyValue;
	}
	
	public Integer getUps() {
		return ups;
	}

	public void setUps(Integer ups) {
		this.ups = ups;
	}

	public Integer getBiasOfGolden(){
		return getBias(now,goldenPrice)/2;  //经测试，取中位数的半值结果最好
	}
	
	public Integer getBiasBaseLine() {
		return getBias(now, baseLinePrice);
	}
	
	public Integer getBiasOfUpLineAndBaseLine() {
		return getBias(upLinePrice,baseLinePrice);
	}
	
	private Integer getBias(BigDecimal price1, BigDecimal price2) {
		BigDecimal i = new BigDecimal(0);
		if(price1!=null && price2!=null){
			i = (price1.subtract(price2)).divide(price2,2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
		}
		return i.intValue();
	}
	
	public Integer getSlips() {
		return slips;
	}

	public void setSlips(Integer slips) {
		this.slips = slips;
	}

	public Integer getTradeDays() {
		return tradeDays;
	}

	public void setTradeDays(Integer tradeDays) {
		this.tradeDays = tradeDays;
	}

	public BigDecimal getUpLinePrice() {
		return upLinePrice;
	}

	public BigDecimal getBaseLinePrice() {
		return baseLinePrice;
	}

	public BigDecimal getGoldenPrice() {
		return goldenPrice;
	}

	public void setGoldenPrice(BigDecimal price) {
		this.goldenPrice = price;
	}

	public void setUpLinePrice(BigDecimal price) {
		this.upLinePrice = price;
	}

	public void setBaseLinePrice(BigDecimal price) {
		this.baseLinePrice = price;
	}

	public BigDecimal getNow() {
		return now;
	}
	public void setNow(BigDecimal price) {
		this.now = price;
	}
	
	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	@Override
	public String toString() {
		return	String.format("%s:status=%d,nowPrice=%.2f(>%.2f),biasBaseLine=%d(<13),biasOfUpLineAndBaseLine=%d(>3),biasOfGolden=%d(<13),slips=%d(<%d),ups=%d(>%d),biasBaseLine=%d(<%d)\n",
				itemID,getStatus(),now,baseLinePrice,getBiasBaseLine(),getBiasOfUpLineAndBaseLine(),getBiasOfGolden(),slips,minSlip,ups,buyValue,getBiasBaseLine(),reopenRatio);

	}


}
