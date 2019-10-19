package com.rhb.istock.trade.hunt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhb.istock.fund.Account;
import com.rhb.istock.kdata.Muster;

public class Hunting {
	protected static final Logger logger = LoggerFactory.getLogger(Hunting.class);

	private Account account = null;
	private BigDecimal initCash = null;
	private Map<Integer,BigDecimal> yearAmount;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer potentials_sb = new StringBuffer();

	public Hunting(BigDecimal initCash) {
		account = new Account(initCash);
		this.initCash = initCash;
		this.yearAmount = new HashMap<Integer,BigDecimal>();
	}
	
	
	/*
	 * 满仓操作
	 * 每只股票市值相同
	 * 
	 * 买入：
	 * 每天只买一只
	 * 低位横盘股
	 * 21日均线上
	 * 
	 * 卖出：
	 * 跌破21均线
	 * 
	 */
	public void doIt_plus(Map<String,Muster> musters, Set<Muster> potentials, LocalDate date) {
		//logger.info(date.toString());
		Muster muster;
		account.setLatestDate(date);

		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		
		//卖出跌破dropline的股票
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice());
				if(muster.isDrop() && !muster.isDownLimited()){
					account.drop(itemID, "跌破dropLine", muster.getLatestPrice()); 
					account.dropHoldState(itemID);
				}
			}
		}				
		
		potentials_sb.append(date.toString() + ",");
		Set<Muster> dds = new HashSet<Muster>();
		holdItemIDs = account.getItemIDsOfHolds();
		for(Muster must : potentials) {
			if(!holdItemIDs.contains(must.getItemID())) {
				dds.add(must);
				potentials_sb.append(must.getItemID());
				potentials_sb.append(",");
			}
		}
		potentials_sb.deleteCharAt(potentials_sb.length()-1);
		potentials_sb.append("\n");
		
		//先卖后买，完成调仓和开仓
		//logger.info("先卖后买，完成调仓");
		if(!dds.isEmpty()) {
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
			
			account.openAll(dds);			//后买
		}

		dailyAmount_sb.append(account.getDailyAmount() + "\n");
		yearAmount.put(date.getYear(), account.getTotal());
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
		result.put("breakers", potentials_sb.toString());
		result.put("lostIndustrys", account.getLostIndustrys());
		result.put("winIndustrys", account.getWinIndustrys());
		return result;
	}
	
}
