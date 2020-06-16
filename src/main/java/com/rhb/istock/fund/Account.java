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

public class Account {
	protected static final Logger logger = LoggerFactory.getLogger(Account.class);

	private BigDecimal initCash = null;

	private BigDecimal cash = null;
	private BigDecimal value = null;

	private Map<String,HoldState> states = null;
	private TreeMap<Integer,Order> holds = null;
	private TreeMap<Integer,Order> opens = null;
	private TreeMap<Integer,Order> drops = null;
	private TreeMap<Integer,Order> stops = null;
	private TreeMap<String,BigDecimal> prices = null;
	private LocalDate beginDate = null;
	private LocalDate endDate = null;
	private Integer orderID=0;
	DecimalFormat orderIDFormat = new DecimalFormat("0000"); 
	
	public Account(BigDecimal cash) {
		this.initCash = cash;
		
		this.cash = cash;
		this.value = new BigDecimal(0);
		
		states = new HashMap<String, HoldState>();
		holds = new TreeMap<Integer,Order>();
		opens = new TreeMap<Integer,Order>();
		drops = new TreeMap<Integer,Order>();
		stops = new TreeMap<Integer,Order>();
		prices = new TreeMap<String,BigDecimal>();
	}
	
	public boolean isEnoughCash(Integer count) {
		if(holds.size()==0) return true;
		
		BigDecimal aveCash = this.cash.divide(new BigDecimal(count),BigDecimal.ROUND_HALF_UP);
		BigDecimal aveValue = this.value.divide(new BigDecimal(holds.size()), BigDecimal.ROUND_HALF_UP);
		return aveCash.compareTo(aveValue)==1;
	}
	
	/*
	 * 各industry中，按盈利的股票数量和盈利的幅度排序
	 * 
	 */
	public String getWinIndustrys(){
		Map<String, Industry> ins = new HashMap<String,Industry>();
		Industry industry;
		
		Order openOrder, dsOrder;
		for(Map.Entry<Integer,Order> entry : opens.entrySet()) {
			openOrder = entry.getValue();
			dsOrder = getDropOrStopOrder(entry.getKey());
			if(dsOrder==null) {
				dsOrder = new Order(openOrder.getOrderID(),openOrder.getItemID(),openOrder.getItemName(), openOrder.getIndustry(),endDate,prices.get(openOrder.getItemID()),openOrder.getQuantity());
			}
			if(ins.containsKey(openOrder.getIndustry())) {
				industry = ins.get(openOrder.getIndustry());
				industry.addRatio(openOrder.getPrice(), dsOrder.getPrice());
				industry.SetProfit(dsOrder.getAmount().subtract(openOrder.getAmount()));
			}else {
				industry = new Industry(openOrder.getIndustry(),openOrder.getPrice(), dsOrder.getPrice());
				industry.SetProfit(dsOrder.getAmount().subtract(openOrder.getAmount()));
				ins.put(openOrder.getIndustry(), industry);
			}				
		}
		
		StringBuffer winIndustrys = new StringBuffer();
		
		List<Industry> ii = new ArrayList<Industry>(ins.values());
		
		Collections.sort(ii, new Comparator<Industry>() {
			@Override
			public int compare(Industry o1, Industry o2) {
				return o2.getProfit().compareTo(o1.getProfit()); //倒叙
			}
		});
		
		for(Industry i : ii) {
			if(i.getProfit().intValue()>=0) {
				winIndustrys.append(i.getName());
				winIndustrys.append(",");
			}
		}
		
		if(winIndustrys.length()>0) {
			winIndustrys.deleteCharAt(winIndustrys.length()-1);
		}
		
		return winIndustrys.toString();
	}
	
	public String getLostIndustrys(){
		Map<String, Industry> ins = new HashMap<String,Industry>();
		Industry industry;
		
		Order openOrder, dsOrder;
		for(Map.Entry<Integer,Order> entry : opens.entrySet()) {
			openOrder = entry.getValue();
			dsOrder = getDropOrStopOrder(entry.getKey());
			if(dsOrder==null) {
				dsOrder = new Order(openOrder.getOrderID(),openOrder.getItemID(),openOrder.getItemName(), openOrder.getIndustry(),endDate,prices.get(openOrder.getItemID()),openOrder.getQuantity());
			}
			
			if(ins.containsKey(openOrder.getIndustry())) {
				industry = ins.get(openOrder.getIndustry());
				industry.addRatio(openOrder.getPrice(), dsOrder.getPrice());
				industry.SetProfit(dsOrder.getAmount().subtract(openOrder.getAmount()));
			}else {
				industry = new Industry(openOrder.getIndustry(),openOrder.getPrice(), dsOrder.getPrice());
				industry.SetProfit(dsOrder.getAmount().subtract(openOrder.getAmount()));
				ins.put(openOrder.getIndustry(), industry);
			}				
		}
		
		StringBuffer lostIndustrys = new StringBuffer();
		
		List<Industry> ii = new ArrayList<Industry>(ins.values());

		for(Industry i : ii) {
			if(i.getProfit().intValue()<0) {
				lostIndustrys.append(i.getName());
				lostIndustrys.append(",");
			}
		}
		
		if(lostIndustrys.length()>0) {
			lostIndustrys.deleteCharAt(lostIndustrys.length()-1);
		}
		
		return lostIndustrys.toString();
	}
	
	class Industry{
		private String name;
		private BigDecimal profit;
		private List<Integer> ratios;
		
		public Industry(String name, BigDecimal profit) {
			this.name = name;
			this.profit = profit;
			this.ratios = new ArrayList<Integer>();
		}
		
		public void SetProfit(BigDecimal profit) {
			this.profit = this.profit.add(profit);
		}
		
		public BigDecimal getProfit() {
			return this.profit;
		}
		
		public Industry(String name, Integer ratio) {
			this.name = name;
			this.ratios = new ArrayList<Integer>();
			this.ratios.add(ratio);
		}
		
		public Industry(String name, BigDecimal buyPrice, BigDecimal sellPrice) {
			this.name = name;
			this.ratios = new ArrayList<Integer>();
			this.addRatio(buyPrice, sellPrice);
			this.profit = new BigDecimal(0);
		}
		
		public void addRatio(BigDecimal buyPrice, BigDecimal sellPrice) {
			this.addRatio(sellPrice.subtract(buyPrice).divide(buyPrice,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue());
		}
		
		public void addRatio(Integer ratio) {
			this.ratios.add(ratio);
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
		public Integer getWinCount() {
			Integer count = 0;
			for(Integer ratio : ratios) {
				if(ratio>0) {
					count++;
				}else {
					count--;
				}
			}
			return count;
		}
		
		public Integer getWinRatio() {
			Integer count = 0;
			Integer ratio = 0;
			for(Integer r : ratios) {
				ratio = ratio + r;
				count ++;
			}
			return ratio/count;
		}
	}
	
	public String getDailyAmount() {
		StringBuffer sb = new StringBuffer();
		sb.append(endDate.toString());
		sb.append(",");
		sb.append(cash.toString());
		sb.append(",");
		sb.append(this.getValue().toString());
		sb.append(",");
		sb.append(this.getTotal().toString());
		return sb.toString();
	}
	
	private Integer getOrderID() {
		return orderID++;
	}
	
	public void reopen(String itemID,String itemName,String industry, Integer quantity, String note) {
		Order order = new Order(this.getOrderID(),itemID,itemName, industry,LocalDate.parse(endDate.toString()),prices.get(itemID),quantity);
		order.setNote("reopen，" + note);
		
		//System.out.println(order); //----------------------------------------------
		
		cash = cash.subtract(order.getAmount());  // 买入时，现金减少
		value = value.add(order.getAmount()); // 市值增加
		holds.put(order.getOrderID(), order);
		opens.put(order.getOrderID(), order);
	}
	
	public void open(String itemID,String itemName,String industry, Integer quantity, String note, BigDecimal price) {
		if(quantity<=0) return;
		
		//logger.info("buy " + itemID + " " + quantity + " units on " + price + " yuan.");
		Order order = new Order(this.getOrderID(),itemID,itemName, industry,LocalDate.parse(endDate.toString()),price,quantity);
		order.setNote(note);

		//System.out.println(order); //----------------------------------------------

		cash = cash.subtract(order.getAmount().add(order.getFee()));  // 买入时，现金减少
		value = value.add(order.getAmount()); // 市值增加
		holds.put(order.getOrderID(), order);
		opens.put(order.getOrderID(), order);
		
		if(!states.containsKey(itemID)) {
			states.put(itemID, new HoldState(itemID,price));
		}else {
			HoldState hs = states.get(itemID);
			hs.setHold(true);
		}
		
	}

	/**
	 * 满仓买入, 可重复买入，即可以加仓
	 * @param items
	 */
	public void openAll(List<Muster> items) {
		if(items.isEmpty()) return;
		
		//int position = items.size()<2 ? 2 : items.size();
		
		int position = items.size();
		
		BigDecimal quota = this.cash.divide(new BigDecimal(position),BigDecimal.ROUND_DOWN);
		for(Muster item : items) {
			this.prices.put(item.getItemID(), item.getLatestPrice());
			this.open(item.getItemID(), item.getItemName(), item.getIndustry(), this.getQuantity(quota, item.getLatestPrice()), "" , item.getLatestPrice());
		}
	}
	
	/**
	 * 满仓买入， 不可重复买入，即不可加仓
	 * @param items
	 */
	public void openAll(Set<Muster> items) {
		if(items.isEmpty()) return;
		
		BigDecimal c = this.cash;
/*		if(holds.size()==0 && items.size()==1) {
			c = this.cash.divide(new BigDecimal(2),BigDecimal.ROUND_DOWN);
		}
*/		//int position = items.size()<3 ? 3 : items.size();
		int position = items.size();
		
		BigDecimal quota = c.divide(new BigDecimal(position),BigDecimal.ROUND_DOWN);
		for(Muster item : items) {
			this.prices.put(item.getItemID(), item.getLatestPrice());
			this.open(item.getItemID(), item.getItemName(), item.getIndustry(), this.getQuantity(quota, item.getLatestPrice()), item.getNote() , item.getLatestPrice());
		}
	}
	
	private Integer getQuantity(BigDecimal quota,BigDecimal price) {
		return quota.divide(price,BigDecimal.ROUND_DOWN).divide(new BigDecimal(100),BigDecimal.ROUND_DOWN).intValue()*100;
	}
	
	public void open(String itemID, String itemName, String industry, Integer quantity, String note) {
		this.open(itemID, itemName, industry, quantity, note, prices.get(itemID));
	}
	
	public void dropHoldState(String itemID) {
		states.remove(itemID);
	}
	
	public void dropByOrderID(Integer orderID, String note, BigDecimal price) {
		Order openOrder = holds.get(orderID);
		Order dropOrder = new Order(openOrder.getOrderID(),openOrder.getItemID(),openOrder.getItemName(),openOrder.getIndustry(), LocalDate.parse(endDate.toString()), price, openOrder.getQuantity());
		dropOrder.setNote(note);

		//System.out.println(dropOrder); //----------------------------------------------
	
		cash = cash.add(dropOrder.getAmount()); 			//卖出时，现金增加
		value = value.subtract(dropOrder.getAmount());		//市值减少
		
		holds.remove(orderID);
		drops.put(dropOrder.getOrderID(), dropOrder);
		
		HoldState hs = states.get(openOrder.getItemID());
		hs.setHold(false);
	}
	
	public void dropWithTaxByOrderID(Integer orderID, String note, BigDecimal price) {
		Order openOrder = holds.get(orderID);
		Order dropOrder = new Order(openOrder.getOrderID(),openOrder.getItemID(),openOrder.getItemName(),openOrder.getIndustry(), LocalDate.parse(endDate.toString()), price, openOrder.getQuantity());
		dropOrder.setNote(note);

		//System.out.println(dropOrder); //----------------------------------------------
	
		cash = cash.add(dropOrder.getAmount().subtract(dropOrder.getFeeAndTax())); 			//卖出时，现金增加
		value = value.subtract(dropOrder.getAmount());		//市值减少
		
		holds.remove(orderID);
		drops.put(dropOrder.getOrderID(), dropOrder);
		
		HoldState hs = states.get(openOrder.getItemID());
		hs.setHold(false);
	}

	public void drop(String itemID, String note, BigDecimal price) {
		Set<Integer> orderIDs = this.getHoldOrderIDs(itemID);
		for(Integer orderID : orderIDs) {
			this.dropByOrderID(orderID, note, price);
		}
	}
	
	public void dropWithTax(String itemID, String note, BigDecimal price) {
		Set<Integer> orderIDs = this.getHoldOrderIDs(itemID);
		for(Integer orderID : orderIDs) {
			this.dropWithTaxByOrderID(orderID, note, price);
		}
	}
	
	public void drop(String itemID, String note) {
		this.drop(itemID, note, prices.get(itemID));
	}
	
	public void stopByItemID(String itemID, String note) {
		Order openOrder;
		for(Iterator<Map.Entry<Integer, Order>> hands_it = holds.entrySet().iterator(); hands_it.hasNext();) {
			openOrder = hands_it.next().getValue();
			if(openOrder.getItemID().equals(itemID)) {
				Order stopOrder = new Order(openOrder.getOrderID(),itemID,openOrder.getItemName(),openOrder.getIndustry(), LocalDate.parse(endDate.toString()), prices.get(itemID), openOrder.getQuantity());
				stopOrder.setNote("stop，" + note);

				//System.out.println(stopOrder); //----------------------------------------------
			
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
			Order stopOrder = new Order(openOrder.getOrderID(), openOrder.getItemID(),openOrder.getItemName(),openOrder.getIndustry(), LocalDate.parse(endDate.toString()), prices.get(openOrder.getItemID()), openOrder.getQuantity());
			stopOrder.setNote("stop");

			//System.out.println(stopOrder); //----------------------------------------------
			
			cash = cash.add(stopOrder.getAmount().subtract(stopOrder.getAmount().multiply(new BigDecimal(0.002)))); 			//卖出时，现金增加
			value = value.subtract(stopOrder.getAmount());		//市值减少	
			
			holds.remove(orderID);
			stops.put(stopOrder.getOrderID(), stopOrder);
		}
	}
	
	public void cancelByOrderID(Integer orderID) {
		Order openOrder = holds.get(orderID);
		if(openOrder!=null) {
			Order stopOrder = new Order(openOrder.getOrderID(), openOrder.getItemID(),openOrder.getItemName(),openOrder.getIndustry(), LocalDate.parse(endDate.toString()), prices.get(openOrder.getItemID()), openOrder.getQuantity());
			stopOrder.setNote("cancel");

			//System.out.println(stopOrder); //----------------------------------------------
			
			cash = cash.add(stopOrder.getAmount().subtract(stopOrder.getAmount().multiply(new BigDecimal(0.002)))); 			//卖出时，现金增加
			value = value.subtract(stopOrder.getAmount());		//市值减少	
			
			holds.remove(orderID);
			stops.put(stopOrder.getOrderID(), stopOrder);
		}
	}
	
	public LocalDate getEndDate() {
		return this.endDate;
	}
	
	public void setLatestDate(LocalDate date) {
		if(this.beginDate==null) this.beginDate = date;
		this.endDate = date;
	}

	public void refreshHoldsPrice(String itemID, BigDecimal price) {
		if(states.containsKey(itemID)) states.get(itemID).setLatestPrice(price);
		
		prices.put(itemID, price);
		
		for(Order order : holds.values()) {
			if(order.getItemID().equals(itemID)) {
				order.setLatest(price);
			}
		}
	}
	
	public Integer getUpRatio(String itemID) {
		HoldState hs = states.get(itemID);
		if(hs!=null && hs.isHold) {
			return Functions.growthRate(hs.getLatestPrice(), hs.getBuyPrice());
		}else {
			return 0;
		}
	}
	
	public Set<String> getItemIDsOfLost(Integer days) {
		Set<String> ids = new HashSet<String>();
		for(HoldState state : states.values()) {
			//logger.info(state.toString(days));
			if(state.isLost(days)) {
				ids.add(state.getItemID());
			}
		}
		return ids;
	}
	
	public Set<String> getItemIDsOfHolds() {
		Set<String> ids = new HashSet<String>();
		for(Order order : holds.values()) {
			ids.add(order.getItemID());
		}
		return ids;
	}
	
	public Integer getProfitRatio(String itemID) {
		BigDecimal cost = new BigDecimal(0);  //买入成本
		BigDecimal value = new BigDecimal(0); //现在市值
		for(Order order : holds.values()) {
			if(order.getItemID().equals(itemID)) {
				cost = cost.add(order.getAmount());
				value = value.add(order.getValue());
			}
		}
		
		Integer ratio = null;
		
		if(cost.intValue()!=0) {
			ratio = value.subtract(cost).divide(cost,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
		}

		//logger.info(String.format("%s cost=%02f, value=%02f, ratio=%d", itemID,cost,value,ratio));
		
		return ratio;
	}
	
	public boolean isStupid(String itemID, Integer orderID, Integer days) {
		boolean flag = false;
		//Integer dd = Period.between(holds.get(orderID).getDate(), endDate).getDays();
		if(Period.between(holds.get(orderID).getDate(), endDate).getDays() >= days && 
				holds.get(orderID).getPrice().compareTo(prices.get(itemID))< 0
				) {
			flag = true;
		}
		return flag;
	}
	
	public boolean isHold(String itemID){
		for(Order order : holds.values()) {
			if(order.getItemID().equals(itemID)) {
				//System.out.println("is hold " + itemID);
				return true;
			}
		}		
		//System.out.println("NOT hold " + itemID);

		return false;
	}
	
	public Set<Integer> getHoldOrderIDs(String itemID){
		Set<Integer> ids = new HashSet<Integer>();
		for(Order order : holds.values()) {
			if(order.getItemID().equals(itemID)) {
				ids.add(order.getOrderID());
			}
		}		
		return ids;
	}
	
	public String getHoldString() {
		StringBuffer sb = new StringBuffer();
		for(Order order : holds.values()) {
			sb.append(order.toString()+"\n");
		}
		return sb.toString();
	}
	
	public String getHoldStateString() {
		StringBuffer sb = new StringBuffer();
		for(HoldState hs : states.values()) {
			sb.append(hs.toString()+"\n");
		}
		return sb.toString();
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
				ps.put(orderIDFormat.format(order.getOrderID()), order.getPrice());
			}
		}		
		return ps;
	}
	
	public BigDecimal getLatestOpenPrice(String itemID){
		BigDecimal price = new BigDecimal(0);
		LocalDate date = null;
		for(Order order : holds.values()) {
			if(order.getItemID().equals(itemID)) {
				if(date == null) {
					date = order.getDate();
					price = order.getPrice();
				}else if(date.isBefore(order.getDate())){
					date = order.getDate();
					price = order.getPrice();
				}				
			}
		}		
		return price;
	}
	
	public BigDecimal getHighestPriceOfHold(String itemID) {
		BigDecimal highest = new BigDecimal(0);
		for(Order order : holds.values()) {
			if(order.getItemID().equals(itemID)) {
				if(highest.compareTo(order.getHighest())==-1) {
					highest = order.getHighest();
				}
			}
		}		
		
		return highest;
	}
	
	public Integer getWinRatio() {
		Integer wins = 0;;
		Integer all = opens.size();
		Order openOrder;
		Order dropOrder;
		Order stopOrder;
		for(Map.Entry<Integer,Order> entry : opens.entrySet()) {
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
	
	public Integer isAboveAveValue(Integer orderID) {
		if(holds.size()<=1) return 1;
		Order order = holds.get(orderID);
		return order.getAmount().compareTo(this.getAveValue());
	}
	
	public BigDecimal getAveValue() {
		if(holds.size()==0) {
			return new BigDecimal(0);
		}else {
			return this.getValue().divide(new BigDecimal(holds.size()),BigDecimal.ROUND_HALF_UP);
		}
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
		sb.append("itemName");
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
		sb.append(",");
		sb.append("industry");
		sb.append("\n");
		return sb.toString();
	}
	
	public String getCSV() {
		Order openOrder, dsOrder;
		StringBuffer sb = new StringBuffer(this.getCSVTitle());
		for(Map.Entry<Integer,Order> entry : opens.entrySet()) {
			openOrder = entry.getValue();
			dsOrder = getDropOrStopOrder(entry.getKey());
			if(dsOrder==null) {
				dsOrder = new Order(openOrder.getOrderID(),openOrder.getItemID(),openOrder.getItemName(), openOrder.getIndustry(),endDate,prices.get(openOrder.getItemID()),openOrder.getQuantity());
				dsOrder.setNote("hold");
			}
			sb.append(openOrder.getOrderID());
			sb.append(",");
			sb.append(openOrder.getItemID());
			sb.append(",");
			sb.append(openOrder.getItemName());
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
			sb.append(",");
			sb.append(openOrder.getIndustry());
			sb.append("\n");

		}
		return sb.toString();
	}
	
	private Order getDropOrStopOrder(Integer orderID) {
		Order dropOrder = drops.get(orderID);
		if(dropOrder == null) {
			return stops.get(orderID);
		}else {
			return dropOrder;
		}
	}
	
	class HoldState{
		private String itemID;
		private BigDecimal buyPrice;
		private BigDecimal latestPrice;
		private Integer holdsDays;
		private boolean isHold;
		
		public HoldState(String itemID, BigDecimal buyPrice) {
			this.itemID = itemID;
			this.buyPrice = buyPrice;
			this.latestPrice = buyPrice;
			this.holdsDays = 1;
			this.isHold = true;
		}
		
		public boolean isHold() {
			return isHold;
		}

		public void setHold(boolean isHold) {
			this.isHold = isHold;
		}


		public boolean isLost(Integer days) {
			return holdsDays>=days && latestPrice.compareTo(buyPrice)==-1;
		}
		
		public String getItemID() {
			return itemID;
		}
		public void setItemID(String itemID) {
			this.itemID = itemID;
		}
		public BigDecimal getBuyPrice() {
			return buyPrice;
		}
		public void setBuyPrice(BigDecimal buyPrice) {
			this.buyPrice = buyPrice;
		}
		public BigDecimal getLatestPrice() {
			return latestPrice;
		}
		public void setLatestPrice(BigDecimal latestPrice) {
			if(isHold) {
				this.latestPrice = latestPrice;
				this.holdsDays = this.holdsDays + 1;
				//logger.info("itemID=" + this.itemID + ",holdsDays=" + this.holdsDays);
			}
		}
		public Integer getHoldsDays() {
			return holdsDays;
		}
		public void setHoldsDays(Integer holdsDays) {
			this.holdsDays = holdsDays;
		}

		@Override
		public String toString() {
			return "HoldState [itemID=" + itemID + ", buyPrice=" + buyPrice + ", latestPrice=" + latestPrice
					+ ", holdsDays=" + holdsDays + ", isHold=" + isHold + "]";
		}

		
		
	}
	
	class Order {
		private Integer orderID;
		private String itemID;
		private String itemName;
		private String industry;
		private LocalDate date;  
		private BigDecimal price;
		private Integer quantity;	
		private String note;
		private BigDecimal latest;
		private BigDecimal highest;
		
		public Order(Integer orderID,String itemID, String itemName, String industry, LocalDate date, BigDecimal price, Integer quantity) {
			this.orderID = orderID;
			this.itemID = itemID;
			this.date = date;
			this.price = price;
			this.quantity = quantity;
			this.latest = price;
			this.highest = price;
			this.industry = industry;
			this.itemName = itemName;
		}
		
		public Integer getRatio() {
			return latest.subtract(price).divide(price,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
		}
		
		
		public String getItemName() {
			return itemName;
		}

		public void setItemName(String itemName) {
			this.itemName = itemName;
		}

		public String getIndustry() {
			return industry;
		}

		public void setIndustry(String industry) {
			this.industry = industry;
		}

		public BigDecimal getLatest() {
			return latest;
		}

		public void setLatest(BigDecimal latest) {
			this.latest = latest;
			if(highest.compareTo(latest)==-1) {
				highest = latest;
			}
		}

		public BigDecimal getHighest() {
			return highest;
		}

		public BigDecimal getAmount() {
			return price.multiply(new BigDecimal(quantity));
		}
		
		private BigDecimal getFeeAndTax() {
			//return BigDecimal.ZERO;
			return this.getAmount().multiply(new BigDecimal(0.002));
		}
		
		private BigDecimal getFee() {
			return BigDecimal.ZERO;
			//return this.getAmount().multiply(new BigDecimal(0.001));
		}

		public BigDecimal getValue() {
			return latest.multiply(new BigDecimal(quantity));
		}
		
		public Integer getOrderID() {
			return orderID;
		}

		public void setOrderID(Integer orderID) {
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
			return "Order [orderID=" + orderID + ", itemID=" + itemID + ", itemName=" + itemName + ", industry="
					+ industry + ", date=" + date + ", price=" + price + ", quantity=" + quantity + ", note=" + note
					+ ", latest=" + latest + ", highest=" + highest + "]";
		}
		
	}
}
