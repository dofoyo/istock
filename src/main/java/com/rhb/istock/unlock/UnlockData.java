package com.rhb.istock.unlock;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.rhb.istock.comm.util.Functions;

public class UnlockData {
	private String itemID;
	private String itemName;
    private LocalDate ann_date;
    private LocalDate float_date;
    private BigDecimal float_share;
    private BigDecimal float_ratio;
    private BigDecimal annPrice;
    private BigDecimal floatPrice;    
    private BigDecimal latestPrice;    
    private BigDecimal highest;
    
    public Integer getRatio() {
    	if(highest==null || annPrice==null || annPrice.equals(BigDecimal.ZERO)) {
    		return 0;
    	}else {
        	return Functions.growthRate(highest, this.annPrice);
    	}
    }
    public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public UnlockData(String itemID) {
		this.itemID = itemID;
	}
    public BigDecimal getHighest() {
		return highest;
	}
	public void setHighest(BigDecimal highest) {
		this.highest = highest;
	}
	public BigDecimal getFloatPrice() {
		return floatPrice;
	}

	public void setFloatPrice(BigDecimal floatPrice) {
		this.floatPrice = floatPrice;
	}

	public BigDecimal getAnnPrice() {
		return annPrice;
	}

	public void setAnnPrice(BigDecimal annPrice) {
		this.annPrice = annPrice;
	}

	public BigDecimal getLatestPrice() {
		return latestPrice;
	}

	public void setLatestPrice(BigDecimal latestPrice) {
		this.latestPrice = latestPrice;
	}

	public void addFloat_ratio(BigDecimal float_ratio) {
    	this.float_ratio = this.float_ratio.add(float_ratio);
    }
    public void addFloat_share(BigDecimal float_share) {
    	this.float_share = this.float_share.add(float_share);
    } 
    public LocalDate getAnn_date() {
		return ann_date;
	}
	public void setAnn_date(LocalDate ann_date) {
		this.ann_date = ann_date;
	}
	public LocalDate getFloat_date() {
		return float_date;
	}
	public void setFloat_date(LocalDate float_date) {
		this.float_date = float_date;
	}
	public BigDecimal getFloat_share() {
		return float_share;
	}
	public void setFloat_share(BigDecimal float_share) {
		this.float_share = float_share;
	}
	public BigDecimal getFloat_ratio() {
		return float_ratio;
	}
	public void setFloat_ratio(BigDecimal float_ratio) {
		this.float_ratio = float_ratio;
	}
	public String getItemID() {
		return itemID;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	@Override
	public String toString() {
		return "UnlockData [itemID=" + itemID + ", itemName=" + itemName + ", ann_date=" + ann_date + ", float_date="
				+ float_date + ", float_share=" + float_share + ", float_ratio=" + float_ratio + ", annPrice="
				+ annPrice + ", floatPrice=" + floatPrice + ", latestPrice=" + latestPrice + ", highest=" + highest
				+ ", getRatio()=" + getRatio() + "]";
	}
	
	public String getInfo() {
		return String.format("%s,%tF,%tF,%.0f,%.2f,%.2f,%.2f,%.2f,%.2f,%d",
				itemID,ann_date,float_date,float_share,float_ratio,annPrice,floatPrice,latestPrice,highest,getRatio());
	}
	
}
