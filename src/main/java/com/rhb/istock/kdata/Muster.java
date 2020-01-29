package com.rhb.istock.kdata;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.rhb.istock.comm.util.Functions;

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
	private BigDecimal lowest34;
	private BigDecimal lowest21;
	private BigDecimal lowest13;
	private BigDecimal lowest8;
	private BigDecimal lowest5;
	private BigDecimal turnover_rate_f;  //换手率（自由流通股）
	private BigDecimal average_turnover_rate_f;
	private BigDecimal volume_ratio;    //量比
	private BigDecimal average_volume_ratio;
	private BigDecimal total_mv;
	private BigDecimal circ_mv;
	private BigDecimal total_share;
	private BigDecimal float_share;
	private BigDecimal free_share;	
	private BigDecimal amount5;
	private DecimalFormat df = new DecimalFormat("#.00");
	
	public BigDecimal getAmount5() {
		return amount5;
	}

	public void setAmount5(BigDecimal amount5) {
		this.amount5 = amount5;
	}

	public BigDecimal getTotal_share() {
		return total_share;
	}

	public void setTotal_share(BigDecimal total_share) {
		this.total_share = total_share;
	}

	public BigDecimal getFloat_share() {
		return float_share;
	}

	public void setFloat_share(BigDecimal float_share) {
		this.float_share = float_share;
	}

	public BigDecimal getFree_share() {
		return free_share;
	}

	public void setFree_share(BigDecimal free_share) {
		this.free_share = free_share;
	}

	public BigDecimal getTotal_mv() {
		return total_mv;
	}

	public void setTotal_mv(BigDecimal total_mv) {
		this.total_mv = total_mv;
	}

	public BigDecimal getCirc_mv() {
		return circ_mv;
	}

	public void setCirc_mv(BigDecimal circ_mv) {
		this.circ_mv = circ_mv;
	}

	public String getAverage_turnover_rate_f_str() {
		return df.format(this.average_turnover_rate_f);
	}

	public String getTurnover_rate_f_str() {
		return df.format(this.turnover_rate_f);
	}
	
	public String getNote() {
		return this.getHLGap().toString();
	}
	
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

	public BigDecimal getAverage_turnover_rate_f() {
		return average_turnover_rate_f;
	}

	public void setAverage_turnover_rate_f(BigDecimal average_turnover_rate_f) {
		this.average_turnover_rate_f = average_turnover_rate_f;
	}

	public BigDecimal getAverage_volume_ratio() {
		return average_volume_ratio;
	}

	public void setAverage_volume_ratio(BigDecimal average_volume_ratio) {
		this.average_volume_ratio = average_volume_ratio;
	}

	public BigDecimal getTurnover_rate_f() {
		return turnover_rate_f;
	}
	public void setTurnover_rate_f(BigDecimal turnover_rate_f) {
		this.turnover_rate_f = turnover_rate_f;
	}
	
	public BigDecimal getVolume_ratio() {
		return volume_ratio;
	}
	
	public BigDecimal cal_volume_ratio() {
		return amount.divide(amount5,BigDecimal.ROUND_HALF_UP);
	}
	
	public void setVolume_ratio(BigDecimal volume_ratio) {
		this.volume_ratio = volume_ratio;
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
		return this.getHNGap()<10;
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
	
	public boolean isBreaker(Integer ratio) {
		Integer r = Functions.ratio(this.averagePrice21, this.averagePrice);
		
		return //this.latestPrice.compareTo(this.close)==1 &&
				this.close.compareTo(this.averagePrice21)<=0 &&
				this.latestPrice.compareTo(this.averagePrice21)==1 &&
				r<=ratio && r>0;
		
	}
	
	public boolean isUp(Integer period) {
		if(period == 21) {
			return  latestPrice.compareTo(averagePrice8)==1 &&
					averagePrice8.compareTo(averagePrice13)==1 &&
					averagePrice13.compareTo(averagePrice21)==1 ;
			
		}else if(period == 34) {
			return  latestPrice.compareTo(averagePrice8)==1 &&
					averagePrice8.compareTo(averagePrice13)==1 &&
					averagePrice13.compareTo(averagePrice21)==1 &&
					averagePrice21.compareTo(averagePrice34)==1;
		}else {
			/*return  latestPrice.compareTo(averagePrice8)==1 &&
					averagePrice8.compareTo(averagePrice13)==1 &&
					averagePrice13.compareTo(averagePrice21)==1 &&
					averagePrice21.compareTo(averagePrice34)==1 &&
					averagePrice34.compareTo(averagePrice)==1;*/
			return  latestPrice.compareTo(averagePrice21)==1 &&
					averagePrice21.compareTo(averagePrice)==1;
		}
	}
	
	public boolean isDropAve(Integer period) {
		if(period == 8) {
			return latestPrice.compareTo(averagePrice8)==-1 && Functions.ratio(latestPrice, averagePrice8)<=-1;
		}else if(period == 13) {
			return latestPrice.compareTo(averagePrice13)==-1 && Functions.ratio(latestPrice, averagePrice13)<=-1;
		}else if(period == 21) {
			return latestPrice.compareTo(averagePrice21)==-1 && Functions.ratio(latestPrice, averagePrice21)<=-1;
		}else if(period == 34) {
			return latestPrice.compareTo(averagePrice34)==-1 && Functions.ratio(latestPrice, averagePrice34)<=-1;
		}else {
			return latestPrice.compareTo(averagePrice)==-1 && Functions.ratio(latestPrice, averagePrice)<=-1;
		}
	}
	
	public boolean isDownAve(Integer period) {
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
	
	public boolean isDown(Integer period) {
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
		if(period == 5) {
			return latestPrice.compareTo(lowest5)==-1;
		}else if(period == 8) {
			return latestPrice.compareTo(lowest8)==-1;
		}else if(period == 13) {
			return latestPrice.compareTo(lowest13)==-1;
		}else if(period == 21) {
			return latestPrice.compareTo(lowest21)==-1;
		}else if(period == 34) {
			return latestPrice.compareTo(lowest34)==-1;
		}else {
			return latestPrice.compareTo(lowest) == -1;
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
		return  latestPrice.compareTo(averagePrice8)==-1 && 
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

	public BigDecimal getLowest13() {
		return lowest13;
	}

	public void setLowest13(BigDecimal lowest13) {
		this.lowest13 = lowest13;
	}

	public BigDecimal getLowest8() {
		return lowest8;
	}

	public void setLowest8(BigDecimal lowest8) {
		this.lowest8 = lowest8;
	}

	public BigDecimal getLowest5() {
		return lowest5;
	}

	public void setLowest5(BigDecimal lowest5) {
		this.lowest5 = lowest5;
	}	
}
