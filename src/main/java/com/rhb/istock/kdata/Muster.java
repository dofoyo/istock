package com.rhb.istock.kdata;

import java.math.BigDecimal;

public class Muster {
	private String itemID;
	private String itemName;
	private String industry;
	private BigDecimal close; 		//上一交易日收盘价
	private BigDecimal amount;
	private Integer limited;        //当日是否一字板
	private BigDecimal latestPrice;  //当日收盘价
	private BigDecimal highest;
	private BigDecimal lowest;
	private BigDecimal averageAmount;
	private BigDecimal averagePrice;
	private BigDecimal averagePrice8;
	private BigDecimal averagePrice13;
	private BigDecimal averagePrice21;
	private BigDecimal averagePrice34;
	private BigDecimal lowest21;
	private BigDecimal lowest34;
	
	public boolean isAboveAveragePrice(Integer period) {
		if(period == 8) {
			return latestPrice.compareTo(averagePrice8)==1;
		}else if(period == 13) {
			return latestPrice.compareTo(averagePrice13)==1;
		}else if(period == 21) {
			return latestPrice.compareTo(averagePrice21)==1;
		}else if(period == 34) {
			return latestPrice.compareTo(averagePrice34)==1;
		}else {
			return latestPrice.compareTo(averagePrice)==1;
		}		
	}
	
	public BigDecimal getLowest34() {
		return lowest34;
	}

	public void setLowest34(BigDecimal lowest34) {
		this.lowest34 = lowest34;
	}

	public BigDecimal getLowest21() {
		return lowest21;
	}

	public void setLowest21(BigDecimal lowest21) {
		this.lowest21 = lowest21;
	}

	public BigDecimal getAveragePrice8() {
		return averagePrice8;
	}

	public void setAveragePrice8(BigDecimal averagePrice8) {
		this.averagePrice8 = averagePrice8;
	}

	public BigDecimal getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(BigDecimal averagePrice) {
		this.averagePrice = averagePrice;
	}

	public BigDecimal getAveragePrice13() {
		return averagePrice13;
	}

	public void setAveragePrice13(BigDecimal averagePrice13) {
		this.averagePrice13 = averagePrice13;
	}

	public BigDecimal getAveragePrice21() {
		return averagePrice21;
	}

	public void setAveragePrice21(BigDecimal averagePrice21) {
		this.averagePrice21 = averagePrice21;
	}

	public BigDecimal getAveragePrice34() {
		return averagePrice34;
	}

	public void setAveragePrice34(BigDecimal averagePrice34) {
		this.averagePrice34 = averagePrice34;
	}

	public boolean isPotential() {
		return this.getHNGap()<10 && !this.isDrop(21);
	}
	
	public Integer getLimited() {
		return limited;
	}

	public void setLimited(Integer limited) {
		this.limited = limited;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	
	public Integer getN21Gap() {
		return latestPrice.subtract(averagePrice21).divide(averagePrice21,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
	}
	
	public Integer getLNGap() {
		return latestPrice.subtract(lowest).divide(lowest,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
	}

	public Integer getHNGap() {
		return highest.subtract(latestPrice).divide(latestPrice,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
	}
	
	public boolean isUpLimited() {
		return limited==1 || latestPrice.subtract(close).divide(close,BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal(0.095))>=0;
		//return latestPrice.subtract(close).divide(close,BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal(0.095))>=0;
	}

	public boolean isDownLimited() {
		return limited==1 || latestPrice.subtract(close).divide(close,BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal(-0.095))<=0;
		//return latestPrice.subtract(close).divide(close,BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal(-0.095))<=0;
	}
	
	public boolean isUp() {
		return  latestPrice.compareTo(averagePrice8)==1 &&
				averagePrice8.compareTo(averagePrice13)==1 &&
				averagePrice13.compareTo(averagePrice21)==1 ;
				//averagePrice21.compareTo(averagePrice34)==1 &&
				//averagePrice34.compareTo(averagePrice)==1;
	}
	
	public boolean isDrop(Integer period) {
		if(period == 8) {
			return latestPrice.compareTo(averagePrice8)==-1;
		}else if(period == 13) {
			return latestPrice.compareTo(averagePrice13)==-1;
		}else if(period == 21) {
			return latestPrice.compareTo(averagePrice21)==-1;
		}else if(period == 34) {
			return latestPrice.compareTo(averagePrice34)==-1;
		}else {
			return latestPrice.compareTo(averagePrice)==-1;
		}
	}
	
	public boolean isDropLowest(Integer period) {
		if(period == 21) {
			return latestPrice.compareTo(lowest21)==-1;
		}else if(period == 34) {
			return latestPrice.compareTo(lowest34)==-1;
		}else {
			return false;
		}
	}
	
	public boolean isNewLowest() {
		return latestPrice.compareTo(lowest)==-1;
	}
	
	public boolean isUpBreaker() {
		return latestPrice.compareTo(highest)==1; 
	}
	
	public boolean isDownBreaker() {
		return latestPrice.compareTo(lowest)==-1; 
	}
	
	public boolean isDown() {
		return  //latestPrice.compareTo(averagePrice8)==-1 && 
				averagePrice8.compareTo(averagePrice13)==-1 &&
				averagePrice13.compareTo(averagePrice21)==-1 &&
				averagePrice21.compareTo(averagePrice34)==-1 &&
				averagePrice34.compareTo(averagePrice)==-1;
	}
	
	public BigDecimal getLatestPrice() {
		return latestPrice;
	}

	public void setLatestPrice(BigDecimal latestPrice) {
		this.latestPrice = latestPrice;
	}

	public Integer getHLGap() {
		return highest.subtract(lowest).divide(lowest,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
	}
	
	public String getItemID() {
		return itemID;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public BigDecimal getAverageAmount() {
		return averageAmount;
	}
	public void setAverageAmount(BigDecimal averageAmount) {
		this.averageAmount = averageAmount;
	}
	public BigDecimal getHighest() {
		return highest;
	}
	public void setHighest(BigDecimal highest) {
		this.highest = highest;
	}
	public BigDecimal getLowest() {
		return lowest;
	}
	public void setLowest(BigDecimal lowest) {
		this.lowest = lowest;
	}
	public BigDecimal getClose() {
		return close;
	}
	public void setClose(BigDecimal close) {
		this.close = close;
	}
}
