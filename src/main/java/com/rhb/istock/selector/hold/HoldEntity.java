package com.rhb.istock.selector.hold;

import java.math.BigDecimal;

public class HoldEntity {
	private String itemID;
	private BigDecimal price;
	
	public HoldEntity(String itemID, BigDecimal price) {
		this.itemID = itemID;
		this.price = price;
		
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	

	@Override
	public String toString() {
		return "HoldEntity [itemID=" + itemID + ", price=" + price + "]";
	}
	
	
}
