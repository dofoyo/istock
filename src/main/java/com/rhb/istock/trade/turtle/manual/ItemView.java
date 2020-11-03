package com.rhb.istock.trade.turtle.manual;

public class ItemView {
	private String itemID;
	private String name;
	private String type;
	
	public ItemView(String itemID, String name, String type) {
		this.itemID = itemID;
		this.name = name;
		this.type = type;
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

	public void setType(String type) {
		this.type = type;
	}
}
