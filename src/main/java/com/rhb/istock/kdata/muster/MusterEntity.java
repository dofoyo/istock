package com.rhb.istock.kdata.muster;

import java.math.BigDecimal;

public class MusterEntity {
	private String itemID;
	private BigDecimal close;
	private BigDecimal amount;
	private BigDecimal latestPrice; 
	private Integer limited;
	private BigDecimal highest;
	private BigDecimal lowest;
	private BigDecimal lowest21;
	private BigDecimal lowest34;
	private BigDecimal averageAmount;
	private BigDecimal averagePrice;
	private BigDecimal averagePrice8;
	private BigDecimal averagePrice13;
	private BigDecimal averagePrice21;
	private BigDecimal averagePrice34;
	private BigDecimal turnover_rate_f;
	private BigDecimal average_turnover_rate_f;
	private BigDecimal volume_ratio;
	private BigDecimal average_volume_ratio;
	private BigDecimal total_mv;
	private BigDecimal circ_mv;
	private BigDecimal total_share;
	private BigDecimal float_share;
	private BigDecimal free_share;	

	public MusterEntity(String itemID, 
			BigDecimal close, 
			BigDecimal amount, 
			BigDecimal latestPrice, 
			Integer limited, 
			BigDecimal highest, 
			BigDecimal lowest, 
			BigDecimal averageAmount, 
			BigDecimal averagePrice, 
			BigDecimal averagePrice8, 
			BigDecimal averagePrice13, 
			BigDecimal averagePrice21, 
			BigDecimal averagePrice34, 
			BigDecimal lowest21, 
			BigDecimal lowest34, 
			BigDecimal turnover_rate_f, 
			BigDecimal average_turnover_rate_f, 
			BigDecimal volume_ratio, 
			BigDecimal average_volume_ratio,
			BigDecimal total_mv,
			BigDecimal circ_mv,
			BigDecimal total_share,
			BigDecimal float_share,
			BigDecimal free_share
			) {
		this.itemID = itemID;
		this.close = close;
		this.amount = amount;
		this.latestPrice = latestPrice;
		this.limited = limited;
		this.highest = highest;
		this.lowest = lowest;
		this.lowest21 = lowest21;
		this.lowest34 = lowest34;
		this.averageAmount = averageAmount;
		this.averagePrice = averagePrice;
		this.averagePrice8 = averagePrice8;
		this.averagePrice13 = averagePrice13;
		this.averagePrice21 = averagePrice21;
		this.averagePrice34 = averagePrice34;
		this.turnover_rate_f = turnover_rate_f;
		this.average_turnover_rate_f = average_turnover_rate_f;
		this.volume_ratio = volume_ratio;
		this.average_volume_ratio = average_volume_ratio;
		this.total_mv = total_mv;
		this.circ_mv = circ_mv;
		this.total_share = total_share;
		this.float_share = float_share;
		this.free_share = free_share;
		
	}
	
	public MusterEntity(String txt) {
		String[] ss = txt.split(",");
		this.itemID = ss[0];
		this.close = new BigDecimal(ss[1]);
		this.amount = new BigDecimal(ss[2]);
		this.latestPrice = new BigDecimal(ss[3]);
		this.limited = Integer.parseInt(ss[4]);
		this.highest = new BigDecimal(ss[5]);
		this.lowest = new BigDecimal(ss[6]);
		this.averageAmount = new BigDecimal(ss[7]);
		this.averagePrice = new BigDecimal(ss[8]);
		this.averagePrice8 = new BigDecimal(ss[9]);
		this.averagePrice13 = new BigDecimal(ss[10]);
		this.averagePrice21 = new BigDecimal(ss[11]);
		this.averagePrice34 = new BigDecimal(ss[12]);
		this.lowest21 = new BigDecimal(ss[13]);
		this.lowest34 = new BigDecimal(ss[14]);
		this.turnover_rate_f = new BigDecimal(ss[15]);
		this.average_turnover_rate_f = new BigDecimal(ss[16]);
		this.volume_ratio = new BigDecimal(ss[17]);
		this.average_volume_ratio = new BigDecimal(ss[18]);
		this.total_mv = new BigDecimal(ss[19]);
		this.circ_mv = new BigDecimal(ss[20]);
		this.total_share = new BigDecimal(ss[21]);
		this.float_share = new BigDecimal(ss[22]);
		this.free_share = new BigDecimal(ss[23]);
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

	public void setVolume_ratio(BigDecimal volume_ratio) {
		this.volume_ratio = volume_ratio;
	}


	public BigDecimal getLowest34() {
		return lowest34;
	}

	public void setLowest34(BigDecimal lowest34) {
		this.lowest34 = lowest34;
	}

	public Integer getLimited() {
		return limited;
	}

	public void setLimited(Integer limited) {
		this.limited = limited;
	}

	public String toText() {
		return this.itemID + "," + 
				this.close + "," + 
				this.amount + "," + 
				this.latestPrice + "," + 
				this.limited + "," +
				this.highest + "," + 
				this.lowest + "," + 
				this.averageAmount + "," + 
				this.averagePrice + "," + 
				this.averagePrice8 + "," + 
				this.averagePrice13 + "," + 
				this.averagePrice21 + "," + 
				this.averagePrice34 + "," + 
				this.lowest21 + "," +  
				this.lowest34 + "," +  
				this.turnover_rate_f + "," + 
				this.average_turnover_rate_f + "," + 
				this.volume_ratio + "," +  
				this.average_volume_ratio + "," +  
				this.total_mv + "," +  
				this.circ_mv + "," +  
				this.total_share + "," +  
				this.float_share + "," +  
				this.free_share + "\n"; 
	}

	public BigDecimal getLowest21() {
		return lowest21;
	}

	public void setLowest21(BigDecimal lowest21) {
		this.lowest21 = lowest21;
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

	public BigDecimal getLatestPrice() {
		return latestPrice;
	}

	public void setLatestPrice(BigDecimal latestPrice) {
		this.latestPrice = latestPrice;
	}

	public BigDecimal getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(BigDecimal averagePrice) {
		this.averagePrice = averagePrice;
	}

	public BigDecimal getAveragePrice8() {
		return averagePrice8;
	}

	public void setAveragePrice8(BigDecimal averagePrice8) {
		this.averagePrice8 = averagePrice8;
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

	@Override
	public String toString() {
		return "MusterEntity [itemID=" + itemID + ", close=" + close + ", amount=" + amount + ", latestPrice="
				+ latestPrice + ", limited=" + limited + ", highest=" + highest + ", lowest=" + lowest + ", lowest21="
				+ lowest21 + ", lowest34=" + lowest34 + ", averageAmount=" + averageAmount + ", averagePrice="
				+ averagePrice + ", averagePrice8=" + averagePrice8 + ", averagePrice13=" + averagePrice13
				+ ", averagePrice21=" + averagePrice21 + ", averagePrice34=" + averagePrice34 + ", turnover_rate_f="
				+ turnover_rate_f + ", average_turnover_rate_f=" + average_turnover_rate_f + ", volume_ratio="
				+ volume_ratio + ", average_volume_ratio=" + average_volume_ratio + ", total_mv=" + total_mv
				+ ", circ_mv=" + circ_mv + ", total_share=" + total_share + ", float_share=" + float_share
				+ ", free_share=" + free_share + "]";
	}

	
}
