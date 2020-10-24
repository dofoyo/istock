package com.rhb.istock.trade.turtle.manual;

public class ItemView {
	private String itemID;
	private String name;
	private String type;
	private Integer cagr;  //利润年均增长率
	
	public ItemView(String itemID, String name, String type, Integer cagr) {
		this.itemID = itemID;
		this.name = name;
		this.type = type;
		this.cagr  =cagr;
	}
	
	public String getItemID() {
		return itemID;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public Integer getCagr() {
		return cagr;
	}

	@Override
	public String toString() {
		return "ItemView [itemID=" + itemID + ", name=" + name + ", type=" + type + ", cagr=" + cagr + "]";
	}
	
}
