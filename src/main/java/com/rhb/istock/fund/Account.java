package com.rhb.istock.fund;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Account {
	protected static final Logger logger = LoggerFactory.getLogger(Account.class);

	private BigDecimal initCash = null;

	private BigDecimal cash = null;
	private BigDecimal value = null;

	private Map<String,Order> holds = null;
	private Map<String,Order> opens = null;
	private Map<String,Order> drops = null;
	private Map<String,Order> stops = null;
	private Map<String,BigDecimal> prices = null;
	private LocalDate beginDate = null;
	private LocalDate endDate = null;
	
	public Account(BigDecimal cash) {
		this.initCash = cash;
		
		this.cash = cash;
		this.value = new BigDecimal(0);
		
		holds = new HashMap<String,Order>();
		opens = new HashMap<String,Order>();
		drops = new HashMap<String,Order>();
		stops = new HashMap<String,Order>();
		prices = new HashMap<String,BigDecimal>();
	}
	
	public String reopen(String itemID, Integer quantity) {
		String orderID = UUID.randomUUID().toString();
		Order order = new Order(orderID,itemID,LocalDate.parse(endDate.toString()),prices.get(itemID),quantity);
		order.setNote("reopen");
		
		System.out.println(order); //----------------------------------------------
		
		cash = cash.subtract(order.getAmount());  // 买入时，现金减少
		value = value.add(order.getAmount()); // 市值增加
		holds.put(order.getOrderID(), order);
		opens.put(order.getOrderID(), order);
		
		return orderID;
	}
	
	public String open(String itemID, Integer quantity) {
		String orderID = UUID.randomUUID().toString();
		Order order = new Order(orderID,itemID,LocalDate.parse(endDate.toString()),prices.get(itemID),quantity);
		order.setNote("open");

		System.out.println(order); //----------------------------------------------

		cash = cash.subtract(order.getAmount());  // 买入时，现金减少
		value = value.add(order.getAmount()); // 市值增加
		holds.put(order.getOrderID(), order);
		opens.put(order.getOrderID(), order);
		
		return orderID;
	}
	
	public void drop(String itemID) {
		Order openOrder;
		for(Iterator<Map.Entry<String, Order>> hands_it = holds.entrySet().iterator(); hands_it.hasNext();) {
			openOrder = hands_it.next().getValue();
			if(openOrder.getItemID().equals(itemID)) {
				Order dropOrder = new Order(openOrder.getOrderID(),itemID, LocalDate.parse(endDate.toString()), prices.get(itemID), openOrder.getQuantity());
				dropOrder.setNote("drop");

				System.out.println(dropOrder); //----------------------------------------------
			
				cash = cash.add(dropOrder.getAmount()); 			//卖出时，现金增加
				value = value.subtract(dropOrder.getAmount());		//市值减少
				
				hands_it.remove();
				drops.put(dropOrder.getOrderID(), dropOrder);
			}
		}
	}
	
	public void stopByItemID(String itemID) {
		Order openOrder;
		for(Iterator<Map.Entry<String, Order>> hands_it = holds.entrySet().iterator(); hands_it.hasNext();) {
			openOrder = hands_it.next().getValue();
			if(openOrder.getItemID().equals(itemID)) {
				Order stopOrder = new Order(openOrder.getOrderID(),itemID, LocalDate.parse(endDate.toString()), prices.get(itemID), openOrder.getQuantity());
				stopOrder.setNote("stop");

				System.out.println(stopOrder); //----------------------------------------------
			
				cash = cash.add(stopOrder.getAmount()); 			//卖出时，现金增加
				value = value.subtract(stopOrder.getAmount());		//市值减少
				
				hands_it.remove();
				drops.put(stopOrder.getOrderID(), stopOrder);
			}
		}
	}	
	
	public void stopByOrderID(String orderID) {
		Order openOrder = holds.get(orderID);
		if(openOrder!=null) {
			Order stopOrder = new Order(openOrder.getOrderID(), openOrder.getItemID(), LocalDate.parse(endDate.toString()), prices.get(openOrder.getItemID()), openOrder.getQuantity());
			stopOrder.setNote("stop");

			System.out.println(stopOrder); //----------------------------------------------
		
			
			cash = cash.add(stopOrder.getAmount()); 			//卖出时，现金增加
			value = value.subtract(stopOrder.getAmount());		//市值减少	
			
			holds.remove(orderID);
			stops.put(stopOrder.getOrderID(), stopOrder);
		}
	}
	
	public void setLatestDate(LocalDate date) {
		if(this.beginDate==null) this.beginDate = date;
		this.endDate = date;
	}

	public void refreshHoldsPrice(String itemID, BigDecimal price) {
		prices.put(itemID, price);
	}
	
	public Set<String> getItemIDsOfHolds() {
		Set<String> ids = new HashSet<String>();
		for(Order order : holds.values()) {
			ids.add(order.getItemID());
		}
		return ids;
	}
	
	public Integer getLots(String itemID) {
		Integer lot = 0;
		for(Order order : holds.values()) {
			if(order.getItemID().equals(itemID)) {
				lot++;
			}
		}
		return lot;
	}
	
	public Map<String,BigDecimal> getOpenPrices(String itemID){
		Map<String,BigDecimal> ps = new HashMap<String,BigDecimal>();
		
		for(Order order : holds.values()) {
			if(order.getItemID().equals(itemID)) {
				ps.put(order.getOrderID(), order.getPrice());
			}
		}		
		return ps;
	}
	
	public BigDecimal getLatestOpenPrice(String itemID){
		BigDecimal price = new BigDecimal(0);
		for(Order order : holds.values()) {
			if(order.getItemID().equals(itemID)) {
				price = price.compareTo(order.getPrice())==-1 ? order.getPrice() : price;
			}
		}		
		return price;
	}
	
	public Integer getWinRatio() {
		Integer wins = 0;
		Integer all = opens.size();
		Order openOrder;
		Order dropOrder;
		Order stopOrder;
		for(Map.Entry<String,Order> entry : opens.entrySet()) {
			openOrder = entry.getValue();
			
			dropOrder = drops.get(entry.getKey());
			if(dropOrder!=null && dropOrder.getPrice().compareTo(openOrder.getPrice())==1) {
				wins++;
			}
			
			stopOrder = stops.get(entry.getKey());
			if(stopOrder!=null && stopOrder.getPrice().compareTo(openOrder.getPrice())==1) {
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
	
	public BigDecimal getCash () {
		return this.cash;
	}
	
	public BigDecimal getValue() {
		BigDecimal total = new BigDecimal(0);
		for(Order order : holds.values()) {
			total = total.add(prices.get(order.getItemID()).multiply(new BigDecimal(order.getQuantity())));
		}

		return total;
	}
	
	public BigDecimal getTotal() {
		return this.cash.add(this.getValue());
	}

	public String getCSVTitle() {
		StringBuffer sb = new StringBuffer();
		sb.append("orderID");
		sb.append(",");
		sb.append("itemID");
		sb.append(",");
		sb.append("name");
		sb.append(",");
		sb.append("openDate");
		sb.append(",");
		sb.append("openPrice");
		sb.append(",");
		sb.append("quantity");
		sb.append(",");
		sb.append("openAmount");
		sb.append(",");
		sb.append("buyNote");
		sb.append(",");
		sb.append("closeDate");
		sb.append(",");
		sb.append("closePrice");
		sb.append(",");
		sb.append("closeAmount");
		sb.append(",");
		sb.append("sellNote");
		sb.append(",");
		sb.append("profit");
		sb.append(",");
		sb.append("year");
		sb.append(",");
		sb.append("month");
		sb.append("\n");
		return sb.toString();
	}
	
	public String getCSV() {
		Order openOrder, dsOrder;
		StringBuffer sb = new StringBuffer(this.getCSVTitle());
		for(Map.Entry<String,Order> entry : opens.entrySet()) {
			openOrder = entry.getValue();
			dsOrder = getDropOrStopOrder(entry.getKey());
			if(dsOrder==null) {
				dsOrder = new Order(openOrder.getOrderID(),openOrder.getItemID(),endDate,prices.get(openOrder.getItemID()),openOrder.getQuantity());
				dsOrder.setNote("hold");
			}
			sb.append("'" + openOrder.getOrderID());
			sb.append(",");
			sb.append("'" + openOrder.getItemID());
			sb.append(",");
			sb.append("");
			sb.append(",");
			sb.append(openOrder.getDate());
			sb.append(",");
			sb.append(openOrder.getPrice());
			sb.append(",");
			sb.append(openOrder.getQuantity());
			sb.append(",");
			sb.append(openOrder.getAmount());
			sb.append(",");
			sb.append(openOrder.getNote());
			sb.append(",");
			sb.append(dsOrder.getDate());
			sb.append(",");
			sb.append(dsOrder.getPrice());
			sb.append(",");
			sb.append(dsOrder.getAmount());
			sb.append(",");
			sb.append(dsOrder.getNote());
			sb.append(",");
			sb.append(dsOrder.getAmount().subtract(openOrder.getAmount()));
			sb.append(",");
			sb.append(dsOrder.getDate().getYear());
			sb.append(",");
			sb.append(dsOrder.getDate().getMonth().getValue());
			sb.append("\n");

		}
		return sb.toString();
	}
	
	private Order getDropOrStopOrder(String orderID) {
		Order dropOrder = drops.get(orderID);
		if(dropOrder == null) {
			return stops.get(orderID);
		}else {
			return dropOrder;
		}
	}
	
	class Order {
		private String orderID;
		private String itemID;
		private LocalDate date;  
		private BigDecimal price;
		private Integer quantity;	
		private String note;
		
		public Order(String orderID,String itemID, LocalDate date, BigDecimal price, Integer quantity) {
			this.orderID = orderID;
			this.itemID = itemID;
			this.date = date;
			this.price = price;
			this.quantity = quantity;
		}
		
		public BigDecimal getAmount() {
			return price.multiply(new BigDecimal(quantity));
		}

		public String getOrderID() {
			return orderID;
		}

		public void setOrderID(String orderID) {
			this.orderID = orderID;
		}

		public String getItemID() {
			return itemID;
		}

		public void setItemID(String itemID) {
			this.itemID = itemID;
		}

		public LocalDate getDate() {
			return date;
		}

		public void setDate(LocalDate date) {
			this.date = date;
		}

		public BigDecimal getPrice() {
			return price;
		}

		public void setPrice(BigDecimal price) {
			this.price = price;
		}

		public Integer getQuantity() {
			return quantity;
		}

		public void setQuantity(Integer quantity) {
			this.quantity = quantity;
		}

		public String getNote() {
			return note;
		}

		public void setNote(String note) {
			this.note = note;
		}

		@Override
		public String toString() {
			return "Order [orderID=" + orderID + ", itemID=" + itemID + ", date=" + date + ", price=" + price
					+ ", quantity=" + quantity + ", note=" + note + "]";
		}
		
	}
}
