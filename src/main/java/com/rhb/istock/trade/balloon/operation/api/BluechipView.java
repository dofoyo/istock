package com.rhb.istock.trade.balloon.operation.api;

public class BluechipView {
	private String itemID;
	private String code;
	private String name;
	private Integer ups;
	private Integer biasOfBaseLine;
	private Integer biasOfGolden;
	private Integer slips;
	private Integer status;
	
	public Integer getUpPower() {
		return ups-biasOfBaseLine-biasOfGolden;
	}
	
	public Integer getSlips() {
		return slips;
	}
	public void setSlips(Integer slips) {
		this.slips = slips;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public void setBiasOfBaseLine(Integer bias) {
		this.biasOfBaseLine = bias;
	}
	public void setBiasOfGolden(Integer bias) {
		this.biasOfGolden = bias;
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
	public Integer getUps() {
		return ups;
	}
	public void setUps(Integer ups) {
		this.ups = ups;
	}
	public Integer getBiasOfBaseLine() {
		return biasOfBaseLine;
	}
	public Integer getBiasOfGolden() {
		return biasOfGolden;
	}
	public String getItemID() {
		return itemID;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
	@Override
	public String toString() {
		return "BluechipView [itemID=" + itemID + ", code=" + code + ", name=" + name + ", ups=" + ups
				+ ", biasOfBaseLine=" + biasOfBaseLine + ", biasOfGolden=" + biasOfGolden + ", slips=" + slips
				+ ", status=" + status + "]";
	}
	
}
