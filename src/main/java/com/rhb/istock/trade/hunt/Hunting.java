package com.rhb.istock.trade.hunt;

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

public class Hunting {
	protected static final Logger logger = LoggerFactory.getLogger(Hunting.class);

	private Account account = null;
	private BigDecimal initCash = null;
	private BigDecimal quota = null;
	private Map<Integer,BigDecimal> yearAmount;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer potentials_sb = new StringBuffer();

	private Map<String,Integer> holdStatus = new HashMap<String,Integer>();//0 表示还未突破21均线，1表示突破21均线
	
	public Hunting(BigDecimal initCash, BigDecimal quote) {
		account = new Account(initCash);
		this.quota = quote;
		this.initCash = initCash;
		this.yearAmount = new HashMap<Integer,BigDecimal>();
	}
	
	
	/*
	 * 满仓操作
	 * 每只股票市值相同
	 * 
	 * 买入：
	 * 每天在低位买入一只横盘的潜力股
	 * 
	 * 卖出：
	 * 1、突破21均线前，跌破前期最低点，即卖出
	 * 2、突破21均线后，跌破21均线，即卖出
	 * 
	 */
	public void doIt_plus(Map<String,Muster> musters, Set<Muster> potentials, LocalDate date) {
		//logger.info(date.toString());
		Muster muster;
		account.setLatestDate(date);

		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		
		//卖出跌破dropline的股票
		//logger.info("卖出跌破dropline的股票")
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice());
				if(muster.isNewLowest() && !muster.isDownLimited()) {
					account.drop(itemID, "跌破lowest", muster.getLatestPrice()); 
					account.dropHoldState(itemID);
					holdStatus.remove(itemID);
				}else if(muster.isDrop() && !muster.isDownLimited() && holdStatus.get(itemID)==1){
					account.drop(itemID, "跌破dropLine", muster.getLatestPrice()); 
					account.dropHoldState(itemID);
					holdStatus.remove(itemID);
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

		for(Muster mu : dds) {
			if(!holdStatus.containsKey(mu.getItemID())) {
				holdStatus.put(mu.getItemID(), 0);
			}
			if(mu.isUp() && holdStatus.containsKey(mu.getItemID())){
				holdStatus.put(mu.getItemID(), 1);
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
		result.put("breakers", potentials_sb.toString());
		result.put("lostIndustrys", account.getLostIndustrys());
		result.put("winIndustrys", account.getWinIndustrys());
		return result;
	}
	
	private Integer getQuantity(BigDecimal price) {
		return this.quota.divide(price,BigDecimal.ROUND_DOWN).divide(new BigDecimal(100),BigDecimal.ROUND_DOWN).intValue()*100;
	}

}
