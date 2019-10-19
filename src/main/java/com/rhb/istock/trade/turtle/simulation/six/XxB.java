package com.rhb.istock.trade.turtle.simulation.six;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhb.istock.fund.Account;
import com.rhb.istock.kdata.Muster;

public class XxB {
	protected static final Logger logger = LoggerFactory.getLogger(XxB.class);

	private Account account = null;
	private BigDecimal initCash = null;
	private Map<Integer,BigDecimal> yearAmount;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	
	private BigDecimal highest = new BigDecimal(0);
	private Integer max_down_ratio = -5; //当前总资产相比highest下跌超过此数值，清仓
	private Integer breaker_ratio = 2; //清仓后，breakers与musters的比率超过此数值，再次开始买入操作
	
	public XxB(BigDecimal initCash) {
		account = new Account(initCash);
		this.initCash = initCash;
		this.yearAmount = new HashMap<Integer,BigDecimal>();
	}
	
	public void doIt(Map<String,Muster> musters, List<Muster> breakers, LocalDate date) {
		Muster muster;
		account.setLatestDate(date);

		
		//处理在手的股票
		Set<String> holdIDs = account.getItemIDsOfHolds();
		for(String itemID: holdIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice());
				
				if(muster.isDrop() && !muster.isDownLimited()) { 		//跌破21日均线就卖
				//if(muster.isDown() && !muster.isDownLimited()) {  //下跌就卖
				//if(!muster.isBreaker() && !muster.isDownLimited()) {  //没有突破就卖
					account.drop(itemID, "", muster.getLatestPrice());
				}				
			}
		}
		
		//买入突破走势的股票
		breakers_sb.append(date.toString() + ",");

		for(Muster breaker : breakers) {
			if(!holdIDs.contains(breaker.getItemID()) && !breaker.isUpLimited() && breaker.getHLGap()<50) {
				account.refreshHoldsPrice(breaker.getItemID(), breaker.getLatestPrice());
				account.open(breaker.getItemID(),breaker.getItemName(), breaker.getIndustry(), this.getQuantity(account.getCash(),breaker.getLatestPrice()), "", breaker.getLatestPrice());
			}
			breakers_sb.append(breaker.getItemID());
			breakers_sb.append(",");
		}
		breakers_sb.deleteCharAt(breakers_sb.length()-1);
		breakers_sb.append("\n");
		
		dailyAmount_sb.append(account.getDailyAmount() + "\n");

		//account.doDailyReport(date);
	}
	
	/*
	 * 满仓操作
	 * 每只股票市值相同
	 * 
	 * 总市值回撤5%，清仓
	 */
	public void doIt_plus1(Map<String,Muster> musters, List<Muster> breakers, LocalDate date,Integer mCount, Integer bCount) {
		//logger.info(date.toString());
		Muster muster;
		account.setLatestDate(date);

		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		for(String itemID : holdItemIDs) {
			muster = musters.get(itemID);
			if(muster != null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice());
			}
		}
		
		Integer ratio  = account.getTotal().subtract(highest).divide(account.getTotal(),BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
		logger.info(String.format("%s: highest=%d, now=%d, up_ratio=%d", date.toString(),highest.intValue(), account.getTotal().intValue(),ratio));
		if(ratio <= max_down_ratio) {
			highest = account.getTotal();
			int cleans  = holdItemIDs.size();
			for(String itemID: holdItemIDs) {
				muster = musters.get(itemID);
				if(muster!=null && !muster.isDownLimited()) {
					account.drop(itemID, "清仓", muster.getLatestPrice()); 
					account.dropHoldState(itemID);
				}
			}
			holdItemIDs = account.getItemIDsOfHolds();
			logger.info(String.format("clean %d items, cleaned %d. %s can NOT be cleaned for limited",cleans,cleans-holdItemIDs.size(),holdItemIDs.toString()));
		}else {
			highest = highest.compareTo(account.getTotal())==-1 ? account.getTotal() : highest;
		}
		
		logger.info(String.format("musters=%d, breakers=%d, breaker_ratio=%d", mCount,bCount,bCount*100/mCount));

		
		//卖出跌破dropline的股票
		//logger.info("卖出跌破dropline的股票")
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				if(muster.isDrop() && !muster.isDownLimited()) {
					account.drop(itemID, "跌破dropLine", muster.getLatestPrice()); 
					account.dropHoldState(itemID);
				}
			}
		}				
		
		Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
		//List<Muster> dds = new ArrayList<Muster>();  //用list，有重复，表示可以加仓
		
		//确定突破走势的股票
		breakers_sb.append(date.toString() + ",");
		StringBuffer sb = new StringBuffer();
		for(Muster breaker : breakers) {
			if(!breaker.isUpLimited()) {
				dds.add(breaker);
				sb.append(breaker.getItemName());
				sb.append(",");
			}
			breakers_sb.append(breaker.getItemID());
			breakers_sb.append(",");
		}
		breakers_sb.deleteCharAt(breakers_sb.length()-1);
		breakers_sb.append("\n");
		
		logger.info(String.format("new open %d items: %s", dds.size(), sb.toString()));
		
		//先卖后买，完成调仓
		//logger.info("先卖后买，完成调仓");
		if(!dds.isEmpty()) {
			holdItemIDs = account.getItemIDsOfHolds();
			Set<String> holdOrderIDs;
			for(String itemID: holdItemIDs) {
				holdOrderIDs = 	account.getHoldOrderIDs(itemID);
				muster = musters.get(itemID);
				if(muster!=null) {
					for(String holdOrderID : holdOrderIDs) {
						account.dropByOrderID(holdOrderID, "调仓", muster.getLatestPrice());   //先卖
						dds.add(muster);						
					}
				}
			}
			
			//System.out.println(dds.size());
			account.openAll(dds);			//后买
		}
		

		dailyAmount_sb.append(account.getDailyAmount() + "," + mCount + "," + bCount + "\n");
		yearAmount.put(date.getYear(), account.getTotal());

		//account.doDailyReport(date);
	}
	
	/*
	 * 满仓操作
	 * 每只股票市值相同
	 *
	 * 
	 */
	public void doIt_plus(Map<String,Muster> musters, List<Muster> breakers, LocalDate date) {
		//logger.info(date.toString());
		Muster muster;
		account.setLatestDate(date);

		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		for(String itemID : holdItemIDs) {
			muster = musters.get(itemID);
			if(muster != null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice());
			}
		}
		
		//卖出跌破dropline的股票
		//logger.info("卖出跌破dropline的股票")
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				if(muster.isDrop() && !muster.isDownLimited()) {
					account.drop(itemID, "跌破dropLine", muster.getLatestPrice()); 
					account.dropHoldState(itemID);
				}
			}
		}				
		
		holdItemIDs = account.getItemIDsOfHolds();
		
		Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
		//List<Muster> dds = new ArrayList<Muster>();  //用list，有重复，表示可以加仓
		
		//确定突破走势的股票
		breakers_sb.append(date.toString() + ",");
		StringBuffer sb = new StringBuffer();
		for(Muster breaker : breakers) {
			if(!breaker.isUpLimited() && !breaker.isDownLimited() && !holdItemIDs.contains(breaker.getItemID())) {
				dds.add(breaker);
				sb.append(breaker.getItemName());
				sb.append(",");
			}
			breakers_sb.append(breaker.getItemID());
			breakers_sb.append(",");
		}
		breakers_sb.deleteCharAt(breakers_sb.length()-1);
		breakers_sb.append("\n");
		
		//先卖后买，完成调仓和开仓
		//logger.info("先卖后买，完成调仓");
		if(!dds.isEmpty()) {
			holdItemIDs = account.getItemIDsOfHolds();
			Set<String> holdOrderIDs;
			for(String itemID: holdItemIDs) {
				holdOrderIDs = 	account.getHoldOrderIDs(itemID);
				muster = musters.get(itemID);
				if(muster!=null) {
					for(String holdOrderID : holdOrderIDs) {
						account.dropByOrderID(holdOrderID, "调仓", muster.getLatestPrice());   //先卖
						dds.add(muster);						
					}
				}
			}
			
			//System.out.println(dds.size());
			account.openAll(dds);			//后买
		}
		

		dailyAmount_sb.append(account.getDailyAmount() + "\n");
		yearAmount.put(date.getYear(), account.getTotal());

		//account.doDailyReport(date);
	}
	
	public Map<Integer,BigDecimal> getYearAmount(){
		return this.yearAmount;
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
		result.put("dailyAmount", dailyAmount_sb.toString());
		result.put("breakers", breakers_sb.toString());
		result.put("lostIndustrys", account.getLostIndustrys());
		result.put("winIndustrys", account.getWinIndustrys());
		return result;
	}
	
	private Integer getQuantity(BigDecimal cash,BigDecimal price) {
		return cash.divide(price,BigDecimal.ROUND_DOWN).divide(new BigDecimal(100),BigDecimal.ROUND_DOWN).intValue()*100;
	}

}
