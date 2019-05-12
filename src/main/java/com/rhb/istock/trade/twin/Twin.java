package com.rhb.istock.trade.twin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rhb.istock.fund.Account;

/*
 * 双均线模型
 * 
 * 长(Long)和短(Short)两均线，
 * 在经过一段时间(duration)后，short向上穿越Long，做多(open),按市值的百分百计算买入仓位(position)
 * 在经过一段时间(duration)后，short向下穿越Long，退出(drop)。
 * 
 * 默认值：
 * Long = 337
 * short = 55
 * duration = 89
 * position = 30%
 * 
 *1 1 2 3 5 8 13 21 34 55 89 144 193 337 530
 *
 */
public class Twin {
	private Integer duration = 144; 	
	private Integer longLine = 337;
	private Integer shortLine = 144;
	private Integer position = 30;
	private Integer stopLossRatio = -8;
	
	private BigDecimal initCash = new BigDecimal(1000000);
	private Account account = new Account(initCash);
	
	private Map<String, Wdata> bdatas = new HashMap<String,Wdata>();

	public Integer getLongLine() {
		return this.longLine;
	}
	
	public void clearDatas() {
		bdatas = new HashMap<String,Wdata>();
	}
	
	public void addDailyData(String itemID,LocalDate date, BigDecimal price) {
		Wdata bdata = bdatas.get(itemID);
		if(bdata == null) {
			bdata = new Wdata(itemID, duration, longLine, shortLine);
			bdatas.put(itemID, bdata);
		}
		bdata.addBar(date, price);
		
		account.refreshHoldsPrice(itemID, price);
		account.setLatestDate(date);		
	}
	
	public boolean noData(String itemID) {
		Wdata bdata = bdatas.get(itemID);
		return bdata==null || bdata.isEmpty();
	}
	
	public Wfeature getFeature(String itemID) {
		Wdata bdata = bdatas.get(itemID);
		if(bdata==null) {
			return null;
		}else {
			return bdata.getFeature();
		}
	}
	
	public void doDrop(String itemID) {
		Wdata bdata = bdatas.get(itemID);
		if(bdata==null) {
			return;
		}
		Wfeature feature = bdata.getFeature();
		//System.out.println(feature);
		if(feature!=null && feature.getStatus()<0) {
			account.drop(itemID,"");
		}
	}
	
	public void doStop(String itemID) {
		Wdata bdata = bdatas.get(itemID);
		if(bdata==null) {
			return;
		}
		Wfeature feature = bdata.getFeature();
		BigDecimal openPrice = account.getLatestOpenPrice(itemID);
		if(feature!=null && openPrice.compareTo(new BigDecimal(0))==1) {
			BigDecimal now = feature.getNowPrice();
			boolean stop = now.subtract(openPrice).divide(openPrice,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue() < stopLossRatio;
			if(stop) {
				account.stopByItemID(itemID,"");
			}
		}
	}
	
	public boolean isFull() {
		return account.getCash().compareTo(new BigDecimal(1000))==-1;
	}
	
	public void doOpen(String itemID, LocalDate date, BigDecimal price) {
		account.refreshHoldsPrice(itemID, price);
		account.setLatestDate(date);	
		
		if(!account.getItemIDsOfHolds().contains(itemID)) {
			Integer quantity = getQuantity(itemID,price);
			if(quantity>0) {
				account.open(itemID, quantity,"");
			}
		}		
	}

	/*
	 *
	 * 每一手的数量，股票是100股，螺纹钢是10吨，...
	 * 目前只针对股票
	 */
	private BigDecimal getQuantityPerHand(String itemID) {
		return new BigDecimal(100);
	}
	
	private Integer getQuantity(String itemID,BigDecimal price) {
		BigDecimal amount = account.getTotal().multiply(new BigDecimal(position)).divide(new BigDecimal(100),BigDecimal.ROUND_DOWN);
		Integer quantity = amount.divide(price,BigDecimal.ROUND_DOWN).divide(getQuantityPerHand(itemID),BigDecimal.ROUND_DOWN).intValue() * getQuantityPerHand(itemID).intValue();
		
		amount = price.multiply(new BigDecimal(quantity));
		BigDecimal cash = account.getCash();
		if(amount.compareTo(cash)==1) {
			quantity = cash.divide(price,BigDecimal.ROUND_DOWN).divide(getQuantityPerHand(itemID),BigDecimal.ROUND_DOWN).intValue() * getQuantityPerHand(itemID).intValue();
		}
		return quantity;
	}
	
	public Set<String> getItemIDsOfHolds(){
		return account.getItemIDsOfHolds();
	}
	
	public Map<String,String> result() {
		if(account == null) return null;
		
		Map<String,String> result = new HashMap<String,String>();
		result.put("CSV", account.getCSV());
		result.put("initCash", this.initCash.toString());
		result.put("cash", account.getCash().toString());
		result.put("value", account.getValue().toString());
		result.put("total", account.getTotal().toString());
		result.put("winRatio", account.getWinRatio().toString()); //赢率
		result.put("cagr", account.getCAGR().toString());  //复合增长率的英文缩写为：CAGR（Compound Annual Growth Rate）
		return result;
	}
	

}
