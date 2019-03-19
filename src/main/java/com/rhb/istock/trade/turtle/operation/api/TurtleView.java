package com.rhb.istock.trade.turtle.operation.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.rhb.istock.comm.util.Line;

public class TurtleView {
	private String itemID;
	private String code;
	private String name;
	private String high;
	private String low;
	private String now;
	private String drop;
	private String hlgap;
	private String nhgap;
	private String line;
	private String atr;
	private String status;

	public TurtleView(Map<String, String> map) {
		//System.out.println(map);
		this.itemID = map.get("itemID");
		this.code = map.get("code");
		this.name = map.get("name");
		this.high = map.get("high");
		this.low = map.get("low");
		this.now = map.get("now");
		this.drop = map.get("drop");
		this.atr = map.get("atr");
		this.hlgap = map.get("hlgap");
		this.nhgap = map.get("nhgap");
		this.status = map.get("status");

		// System.out.println(map.get("openHigh"));
		// System.out.println(this.high);

		Map<String, BigDecimal> prices = new HashMap<String, BigDecimal>();
		prices.put("now", new BigDecimal(this.now));
		prices.put("high", new BigDecimal(this.high));
		prices.put("low", new BigDecimal(this.low));
		prices.put("drop", new BigDecimal(this.drop));
		
		this.line = Line.draw(prices);
	}

	public String getLine() {
		return line;
	}

	public String getAtr() {
		return atr;
	}

	public void setAtr(String atr) {
		this.atr = atr;
	}

	public void setLine(String line) {
		this.line = line;
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

	public String getHigh() {
		return high;
	}

	public void setHigh(String high) {
		this.high = high;
	}

	public String getLow() {
		return low;
	}

	public void setLow(String low) {
		this.low = low;
	}

	public String getNow() {
		return now;
	}

	public void setNow(String now) {
		this.now = now;
	}

	public String getDrop() {
		return drop;
	}

	public void setDrop(String drop) {
		this.drop = drop;
	}

	public String getHlgap() {
		return hlgap;
	}

	public void setHlgap(String hlgap) {
		this.hlgap = hlgap;
	}

	public String getNhgap() {
		return nhgap;
	}

	public void setNhgap(String nhgap) {
		this.nhgap = nhgap;
	}

	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "PreyView [itemID=" + itemID + ", code=" + code + ", name=" + name + ", high=" + high + ", low=" + low
				+ ", now=" + now + ", drop=" + drop + ", hlgap=" + hlgap + ", nhgap=" + nhgap + ", line=" + line
				+ ", atr=" + atr + "]";
	}

}
