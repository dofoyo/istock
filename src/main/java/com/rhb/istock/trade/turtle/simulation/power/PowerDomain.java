package com.rhb.istock.trade.turtle.simulation.power;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PowerDomain {
	Map<String,Power> onHands = new HashMap<String,Power>();
	List<Power> hists = new ArrayList<Power>();
	
	public void add(String itemID, LocalDate date, BigDecimal price) {
		if(!onHands.containsKey(itemID)) {
			onHands.put(itemID, new Power(itemID, date, price));
		}
	}
	
	public void put(String itemID, LocalDate date, BigDecimal price, boolean isUpLimited, boolean isDownLimited, boolean isDrop, boolean isDown) {
		if(onHands.containsKey(itemID)) {
			Power power = onHands.get(itemID);

			power.setPrice(date, price);
			if(!power.isBuy() && isDrop) {
				doDrop(itemID);
			}else if(power.isBuy() && isDrop && !isDownLimited) {
				doSell(itemID);
			}else if(!power.isBuy() && power.getUpRatio()>0 && power.getUpRatio()<10 && power.getHighestRatio()<10 && !isUpLimited && !isDrop && !isDown){
				doBuy(itemID);
			}
		}
	}
	
	public void doDrop(String itemID) {
		Power power = onHands.get(itemID);
		onHands.remove(itemID);
	}
	
	public void doBuy(String itemID) {
		Power power = onHands.get(itemID);
		power.buy();
	}
	
	public void doSell(String itemID) {
		Power power = onHands.get(itemID);
		power.sell();
		hists.add(power);
		onHands.remove(itemID);
	}
	
	public Set<String> getIDs(){
		return onHands.keySet();
	}
	
	public String getResult() {
		List<Power> ps = new ArrayList<Power>(onHands.values());
		ps.addAll(hists);
		
		Collections.sort(ps, new Comparator<Power>() {
			@Override
			public int compare(Power o1, Power o2) {
				return o1.breakDate.compareTo(o2.breakDate);
			}
		});
		
		
		StringBuffer sb = new StringBuffer("itemID,breakDate,breakPrice,buyDate,buyPrice,sellDate,sellPrice,nowDate,nowPrice,profitRatio,highestRatio,lowestRatio\n");
		for(Power p : ps) {
			if(p.getProfitRatio()!=null) {
				sb.append(p.getItemID() + ",");
				sb.append(p.getBreakDate() + ",");
				sb.append(p.getBreakPrice() + ",");
				sb.append(p.getBuyDate() + ",");
				sb.append(p.getBuyPrice() + ",");
				sb.append(p.getSellDate() + ",");
				sb.append(p.getSellPrice() + ",");
				sb.append(p.getNowDate() + ",");
				sb.append(p.getNowPrice() + ",");
				sb.append(p.getProfitRatio() + ",");
				sb.append(p.getHighestRatio() + ",");
				sb.append(p.getLowestRatio() + "\n");
			}
		}
		
		return sb.toString();
	}
	
	class Power{
		private String itemID;
		private LocalDate breakDate;
		private BigDecimal breakPrice;
		private LocalDate buyDate;
		private BigDecimal buyPrice;
		private LocalDate sellDate;
		private BigDecimal sellPrice;
		private LocalDate nowDate;
		private BigDecimal nowPrice;
		private LocalDate highestDate;
		private BigDecimal highestPrice;
		private LocalDate lowestDate;
		private BigDecimal lowestPrice;
		
		public BigDecimal getBreakPrice() {
			return this.breakPrice;
		}
		
		public Integer getUpRatio() {
			return nowPrice.subtract(breakPrice).divide(breakPrice,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
		}
		
		public void sell() {
			this.sellDate = this.nowDate;
			this.sellPrice = this.nowPrice;
		}
		
		public void buy() {
			this.buyDate = this.nowDate;
			this.buyPrice = this.nowPrice;
		}
		
		public Power(String itemID,LocalDate breakDate, BigDecimal breakPrice) {
			this.itemID = itemID;
			this.breakDate = breakDate;
			this.breakPrice = breakPrice;
			this.highestDate = breakDate;
			this.highestPrice = breakPrice;
			this.lowestDate = breakDate;
			this.lowestPrice = breakPrice;
		}
		
		public String getItemID() {
			return itemID;
		}

		public void setPrice(LocalDate date, BigDecimal price) {
			this.nowDate = date;
			this.nowPrice = price;
			if(highestPrice.compareTo(price)==-1) {
				this.highestDate = date;
				this.highestPrice = price;
			}
			if(lowestPrice.compareTo(price)==1) {
				this.lowestDate = date;
				this.lowestPrice = price;
			}
		}
		
		public boolean isBuy() {
			return buyDate==null ? false : true;
		}
		
		public boolean isSell() {
			return sellDate==null ? false : true;
		}
		
		public Integer getProfitRatio() {
			if(isBuy()) {
				BigDecimal price = isSell() ? sellPrice : nowPrice;
				return price.subtract(buyPrice).divide(buyPrice,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
			}else {
				return null;
			}
		}
		
		public Integer getHighestRatio() {
				return highestPrice.subtract(breakPrice).divide(breakPrice,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
		}
		
		public Integer getLowestRatio() {
				return lowestPrice.subtract(breakPrice).divide(breakPrice,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
		}

		@Override
		public String toString() {
			return "Power [itemID=" + itemID + ", breakDate=" + breakDate + ", breakPrice=" + breakPrice + ", buyDate="
					+ buyDate + ", buyPrice=" + buyPrice + ", sellDate=" + sellDate + ", sellPrice=" + sellPrice
					+ ", nowDate=" + nowDate + ", nowPrice=" + nowPrice + ", getProfitRatio()=" + getProfitRatio()
					+ "]";
		}

		public LocalDate getBreakDate() {
			return breakDate;
		}

		public void setBreakDate(LocalDate breakDate) {
			this.breakDate = breakDate;
		}

		public LocalDate getBuyDate() {
			return buyDate;
		}

		public void setBuyDate(LocalDate buyDate) {
			this.buyDate = buyDate;
		}

		public BigDecimal getBuyPrice() {
			return buyPrice;
		}

		public void setBuyPrice(BigDecimal buyPrice) {
			this.buyPrice = buyPrice;
		}

		public LocalDate getSellDate() {
			return sellDate;
		}

		public void setSellDate(LocalDate sellDate) {
			this.sellDate = sellDate;
		}

		public BigDecimal getSellPrice() {
			return sellPrice;
		}

		public void setSellPrice(BigDecimal sellPrice) {
			this.sellPrice = sellPrice;
		}

		public LocalDate getNowDate() {
			return nowDate;
		}

		public void setNowDate(LocalDate nowDate) {
			this.nowDate = nowDate;
		}

		public BigDecimal getNowPrice() {
			return nowPrice;
		}

		public void setNowPrice(BigDecimal nowPrice) {
			this.nowPrice = nowPrice;
		}

		public void setItemID(String itemID) {
			this.itemID = itemID;
		}

		public void setBreakPrice(BigDecimal breakPrice) {
			this.breakPrice = breakPrice;
		}

		
		
	}
	
}

