package com.rhb.istock.item;

public class Item {
	private String itemID;
	private String code;
	private String name;
	private String area;
	private String industry;
	private String ipo;
	
	public String getItemID() {
		return itemID;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getIndustry() {
		return industry;
	}
	public void setIndustry(String industry) {
		this.industry = industry;
	}
	public String getIpo() {
		return ipo;
	}
	public void setIpo(String ipo) {
		this.ipo = ipo;
	}
	@Override
	public String toString() {
		return "Item [itemID=" + itemID + ", code=" + code + ", name=" + name + ", area=" + area + ", industry="
				+ industry + ", ipo=" + ipo + "]";
	}

}
