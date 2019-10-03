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

public class Paul {
	protected static final Logger logger = LoggerFactory.getLogger(Paul.class);

	private Account account = null;
	private BigDecimal initCash = null;
	private BigDecimal quota = null;
	private Integer holdDays = 5;
	private Integer theProfitRatio = 8;//盈利8%以上才能加仓
	private Map<Integer,BigDecimal> yearAmount;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	
	public Paul(BigDecimal initCash, BigDecimal quote) {
		account = new Account(initCash);
		this.quota = quote;
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
				account.open(breaker.getItemID(),breaker.getItemName(), breaker.getIndustry(), this.getQuantity(breaker.getLatestPrice()), "", breaker.getLatestPrice());
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
	 * flag = 1, 可以买入
	 * flag = 0, 不可买入
	 * flag = -1, 清仓
	 * 
	 */
	public void doIt_plus(Map<String,Muster> musters, List<Muster> breakers, LocalDate date,Integer flag) {
		//logger.info(date.toString());
		Muster muster;
		account.setLatestDate(date);

		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		//如果flag=-1,清仓
		if(flag == -1) {
			for(String itemID: holdItemIDs) {
				muster = musters.get(itemID);
				if(muster!=null) {
					account.refreshHoldsPrice(itemID, muster.getLatestPrice());
					account.drop(itemID, "清仓", muster.getLatestPrice()); 
					account.dropHoldState(itemID);
				}
			}			
		}else {
			//卖出跌破dropline的股票
			//logger.info("卖出跌破dropline的股票");
			for(String itemID: holdItemIDs) {
				muster = musters.get(itemID);
				if(muster!=null) {
					account.refreshHoldsPrice(itemID, muster.getLatestPrice());
					
					if(muster.isDrop() && !muster.isDownLimited()) {
						account.drop(itemID, "跌破dropLine", muster.getLatestPrice()); 
						account.dropHoldState(itemID);
					}
				}
			}				
		}
		
	

/*		
 * 		//使用此功能，收益率大幅降低
 * 		//卖出几天都不涨的股票
		//logger.info("卖出几天都不涨的股票");
		holdIDs = account.getItemIDsOfLost(holdDays);
		for(String itemID: holdIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				account.drop(itemID, "lost", muster.getLatestPrice()); 
				account.dropHoldState(itemID);
			}
		}	*/
		
		//Map<String, BigDecimal> dds = new HashMap<String,BigDecimal>();
		Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
		//List<Muster> dds = new ArrayList<Muster>();  //用list，有重复，表示可以加仓
		
		//确定突破走势的股票
		if(flag == 1) {
			breakers_sb.append(date.toString() + ",");

			Integer profitRatio = null;
			for(Muster breaker : breakers) {
				//if(!breaker.isUpLimited() && breaker.getHLGap()<50) {
				if(!breaker.isUpLimited()) {
					profitRatio = account.getProfitRatio(breaker.getItemID());
					if(profitRatio==null || profitRatio>=theProfitRatio) {
						dds.add(breaker);
					}
					
	/*				if(profitRatio!=null && profitRatio>=theProfitRatio) {
						System.out.println("加仓" + breaker.getItemID() + breaker.getItemName());
					}*/
					
					//account.refreshHoldsPrice(breaker.getItemID(), breaker.getLatestPrice());
					//account.open(breaker.getItemID(), this.getQuantity(breaker.getLatestPrice()), "", breaker.getLatestPrice());
				}
				breakers_sb.append(breaker.getItemID());
				breakers_sb.append(",");
			}
			breakers_sb.deleteCharAt(breakers_sb.length()-1);
			breakers_sb.append("\n");
			
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
	
	private Integer getQuantity(BigDecimal price) {
		return this.quota.divide(price,BigDecimal.ROUND_DOWN).divide(new BigDecimal(100),BigDecimal.ROUND_DOWN).intValue()*100;
	}

}
