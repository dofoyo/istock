package com.rhb.istock.selector.potential;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Potential {
	private String itemID;
	private BigDecimal amount;
	private BigDecimal averageAmount;
	private BigDecimal highest;
	private BigDecimal lowest;
	private BigDecimal latestPrice;
	private BigDecimal nowPrice;
	private Integer bhl;
	private Integer bdt;
	private Integer bav;
	private Integer hlb;
	private Integer dtb;
	private Integer avb;
	
	public Potential(String itemID, BigDecimal averageAmount, BigDecimal amount, BigDecimal highest, BigDecimal lowest, BigDecimal latestPrice,BigDecimal nowPrice) {
		this.itemID = itemID;
		this.amount = amount;
		this.averageAmount = averageAmount;
		this.highest = highest;
		this.lowest = lowest;
		this.latestPrice = latestPrice;
		this.nowPrice = nowPrice;
	}
	
	public List<String> getLabels() {
		List<String> labels = new ArrayList<String>();
		if(bhl!=null) labels.add("bhl(" + bhl + ")");
		if(bdt!=null) labels.add("bdt(" + bdt + ")");
		if(bav!=null) labels.add("bav(" + bav + ")");
		if(hlb!=null) labels.add("hlb(" + hlb + ")");
		if(dtb!=null) labels.add("dtb(" + dtb + ")");
		if(avb!=null) labels.add("avb(" + avb + ")");
		return labels;
	}
	
	public BigDecimal getNowPrice() {
		return nowPrice;
	}

	public void setNowPrice(BigDecimal nowPrice) {
		this.nowPrice = nowPrice;
	}

	public String getStatus() {
		return nowPrice.compareTo(highest)==1 ? "2" : "0";
	}
	
	public Integer getHLGap() {
		return highest.subtract(lowest).divide(lowest,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
	}
	
	public Integer getHNGap() {
		return highest.subtract(nowPrice).divide(nowPrice,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
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

	public BigDecimal getLatestPrice() {
		return latestPrice;
	}

	public void setLatestPrice(BigDecimal latestPrice) {
		this.latestPrice = latestPrice;
	}
	
	public String toText() {
		return this.itemID + "," + this.amount + "," + this.averageAmount + "," + this.highest + "," + this.lowest + "," + this.latestPrice;
	}

	public Potential(String txt) {
		String[] ss = txt.split(",");
		this.itemID = ss[0];
		this.amount = new BigDecimal(ss[1]);
		this.averageAmount = new BigDecimal(ss[2]);
		this.highest = new BigDecimal(ss[3]);
		this.lowest = new BigDecimal(ss[4]);
		this.latestPrice = new BigDecimal(ss[5]);
	}
	
	@Override
	public String toString() {
		return "Potential [itemID=" + itemID + ", amount=" + amount + ", averageAmount=" + averageAmount + ", highest="
				+ highest + ", lowest=" + lowest + ", latestPrice=" + latestPrice + ", nowPrice=" + nowPrice + ", bhl="
				+ bhl + ", bdt=" + bdt + ", bav=" + bav + ", hlb=" + hlb + ", dtb=" + dtb + ", avb=" + avb
				+ ", getHLGap()=" + getHLGap() + ", getHNGap()=" + getHNGap() + "]";
	}

	public Integer getBhl() {
		return bhl;
	}

	public void setBhl(Integer bhl) {
		this.bhl = bhl;
	}

	public Integer getBdt() {
		return bdt;
	}

	public void setBdt(Integer bdt) {
		this.bdt = bdt;
	}

	public Integer getBav() {
		return bav;
	}

	public void setBav(Integer bav) {
		this.bav = bav;
	}

	public Integer getHlb() {
		return hlb;
	}

	public void setHlb(Integer hlb) {
		this.hlb = hlb;
	}

	public Integer getDtb() {
		return dtb;
	}

	public void setDtb(Integer dtb) {
		this.dtb = dtb;
	}

	public Integer getAvb() {
		return avb;
	}

	public void setAvb(Integer avb) {
		this.avb = avb;
	}

	

}
