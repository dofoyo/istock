package com.rhb.istock.fund;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.kdata.Muster;

public class Account2 {
	protected static final Logger logger = LoggerFactory.getLogger(Account2.class);

	private BigDecimal initCash = null;

	private BigDecimal cash = null;

	private Map<Integer,Order2> orders = null;

	private LocalDate beginDate = null;
	private LocalDate endDate = null;
	private Integer orderID=1;
	//DecimalFormat orderIDFormat = new DecimalFormat("0000"); 
	
	public Account2(BigDecimal cash) {
		this.initCash = cash;
		this.cash = cash;
		this.orders = new TreeMap<Integer, Order2>();
	}

	public void setLatestDate(LocalDate date) {
		if(this.beginDate==null) this.beginDate = date;
		this.endDate = date;
	}
	
	public Set<String> getItemIDsOfHolds() {
		Set<String> ids = new HashSet<String>();
		for(Order2 order : orders.values()) {
			if(order.isHold()) ids.add(order.getItemID());
		}
		return ids;
	}
	
	public void refreshHoldsPrice(String itemID, BigDecimal price) {
		for(Order2 order : orders.values()) {
			if(order.getItemID().equals(itemID)) {
				order.setLatestPrice(price);
			}
		}
	}
	public BigDecimal getTotal() {
		return this.cash.add(this.getValue());
	}
	public BigDecimal getValue() {
		BigDecimal total = new BigDecimal(0);
		for(Order2 order : orders.values()) {
			
			total = total.add(order.getValue());
		}
		return total;
	}
	
	public void dropWithTax(String itemID, String note, BigDecimal price) {
		for(Order2 order : orders.values()) {
			if(order.isHold() && order.getItemID().equals(itemID)) {
				BigDecimal amount = order.sell(this.endDate, price, order.getQuantity(), note);

				cash = cash.add(amount); 	//卖出时，现金增加
			}
		}
	}
	
	public void dropToAve(BigDecimal value) {
		BigDecimal a, amount;
		Integer quantity;
		for(Order2 order : orders.values()) {
			if(order.isHold() && order.getValue().compareTo(value)==1) {
				a = order.getValue().subtract(value);
				quantity = a.divide(order.getLatestPrice(),BigDecimal.ROUND_DOWN).intValue();
				amount = order.sell(this.endDate,order.getLatestPrice(),quantity, "0");

				cash = cash.add(amount); 	//卖出时，现金增加
			}
		}
	}

	public void open(Set<Muster> items) {
		if(items.isEmpty()) return;
		Order2 order;
		Integer quantity;
		BigDecimal quota = this.cash.divide(new BigDecimal(items.size()),BigDecimal.ROUND_DOWN);
		BigDecimal amount;
		for(Muster item : items) {
			order = new Order2(orderID++,item.getItemID(), item.getItemName(),item.getLatestPrice());
			quantity = quota.divide(item.getLatestPrice(),BigDecimal.ROUND_DOWN).divide(new BigDecimal(100),BigDecimal.ROUND_DOWN).intValue()*100;;
			amount = order.buy(this.endDate, item.getLatestPrice(), quantity, "");
			orders.put(order.getOrderID(), order);
			
			cash = cash.subtract(amount); 	//买入时，现金减少
		}
	}
	
	public String getDailyAmount() {
		StringBuffer sb = new StringBuffer();
		sb.append(endDate.toString());
		sb.append(",");
		sb.append(this.cash.toString());
		sb.append(",");
		sb.append(this.getValue().toString());
		sb.append(",");
		sb.append(this.getTotal().toString());
		return sb.toString();
	}
	
	public String getCSV() {
		StringBuffer sb = new StringBuffer(this.getCSVTitle());
		for(Order2 order : this.orders.values()) {
			sb.append(order.getCSV(this.endDate));
		}
		return sb.toString();
	}
	
	public BigDecimal getCash () {
		return this.cash;
	}
	
	public Integer getWinRatio() {
		Integer wins = 0;;
		Integer all = orders.size();
		for(Order2 order : orders.values()) {
			if(order.getProfit().compareTo(BigDecimal.ZERO)==1) {
				wins++;
			}
		}

		return all==0 ? 0 :(wins*100)/all;
	}
	
	/*
	 * 复合增长率的英文缩写为：CAGR（Compound Annual Growth Rate）
	 * b = a(1+x)^n
	 * x = (b/a)^(1/n) - 1
	 * 
	 */
	public Integer getCAGR() {
		Integer cage = 0;
		if(endDate!=null && beginDate!=null) {
			Integer years = endDate.getYear() - beginDate.getYear() + 1;
			Double ba = this.getTotal().divide(this.initCash,BigDecimal.ROUND_HALF_UP).doubleValue();
			Double x = (Math.pow(ba, 1.0/years) - 1) * 100;
			cage =  x.intValue();
		}
		return cage;
	}
	
	
	public String getCSVTitle() {
		StringBuffer sb = new StringBuffer();
		sb.append("orderID");
		sb.append(",");
		sb.append("itemID");
		sb.append(",");
		sb.append("itemName");
		sb.append(",");
		sb.append("date");
		sb.append(",");
		sb.append("price");
		sb.append(",");
		sb.append("quantity");
		sb.append(",");
		sb.append("amount");
		sb.append(",");
		sb.append("fee");
		sb.append(",");
		sb.append("note");
		sb.append("\n");
		return sb.toString();
	}
}
