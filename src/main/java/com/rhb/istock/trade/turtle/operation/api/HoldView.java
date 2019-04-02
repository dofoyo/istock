package com.rhb.istock.trade.turtle.operation.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.rhb.istock.comm.util.Line;

public class HoldView {
	private String itemID;
	private String code;
	private String name;
	private String industry;
	private String area;
	private String atr;
	private BigDecimal nowPrice;
	private BigDecimal buyPrice;
	private Map<String, BigDecimal> prices;
	
	public HoldView(String itemID, String code, String name, String atr) {
		this.itemID = itemID;
		this.code = code;
		this.name = name;
		this.atr = atr;
		prices = new HashMap<String, BigDecimal>();
	}
	
	public void setNowPrice(BigDecimal price) {
		this.nowPrice = price;
		prices.put("now", price);
	}
	public void setHighPrice(BigDecimal price) {
		prices.put("high", price);
	}
	public void setLowPrice(BigDecimal price) {
		prices.put("low", price);
	}
	public void setBuyPrice(BigDecimal price) {
		this.buyPrice = price;
		prices.put("buy", price);
	}
	public void setStopPrice(BigDecimal price) {
		prices.put("stop", price);
	}
	public void setDropPrice(BigDecimal price) {
		prices.put("drop", price);
	}
	public void setReopenPrice(BigDecimal price) {
		prices.put("reopen", price);
	}

	public String getAtr() {
		return atr;
	}

	public void setAtr(String atr) {
		this.atr = atr;
	}

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

	public String getLine() {
		return Line.draw(prices);
	}
	
	public String getStatus() {
		return nowPrice.compareTo(buyPrice)==1 ? "2" : "-2";
	}
	

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}
	
	@Override
	public String toString() {
		return "HoldView [itemID=" + itemID + ", code=" + code + ", name=" + name + ", atr=" + atr + ", prices="
				+ prices + "]";
	}

}
