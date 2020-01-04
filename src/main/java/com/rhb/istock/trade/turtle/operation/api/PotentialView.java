package com.rhb.istock.trade.turtle.operation.api;

public class PotentialView {
	private String itemID;
	private String itemName;
	private String industry;
	private Integer industryHot;
	private Integer hlGap;
	private Integer hnGap;
	
	public PotentialView(String itemID, String name, String industry, Integer hlGap, Integer hnGap) {
		this.itemID = itemID;
		this.itemName = name;
		this.industry = industry;
		this.hlGap = hlGap;
		this.hnGap = hnGap;
	}
	
	public Integer getHnGap() {
		return hnGap;
	}

	public void setHnGap(Integer hnGap) {
		this.hnGap = hnGap;
	}

	public String getHlGap() {
		return String.format("%d",hlGap);
	}

	public void setHlGap(Integer hlGap) {
		this.hlGap = hlGap;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public Integer getIndustryHot() {
		return industryHot;
	}

	public void setIndustryHot(Integer industryHot) {
		this.industryHot = industryHot;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	
	@Override
	public String toString() {
		return "PotentialView [itemID=" + itemID + ", itemName=" + itemName + ", industry=" + industry + ", industryHot="
				+ industryHot + "]";
	}

}
