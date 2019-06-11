package com.rhb.istock.trade.turtle.simulation.muster;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rhb.istock.fund.Account;
import com.rhb.istock.kdata.Muster;

public class Paul {
	private Account account = null;
	private BigDecimal initCash = null;
	private BigDecimal quota = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	
	public Paul(BigDecimal initCash, BigDecimal quote) {
		account = new Account(initCash);
		this.quota = quote;
		this.initCash = initCash;
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
				
				if(muster.isDrop() && !muster.isDownLimited()) {
					account.drop(itemID, "", muster.getLatestPrice());
				}				
			}
		}
		
		//买入突破走势的股票
		breakers_sb.append(date.toString() + ",");

		for(Muster breaker : breakers) {
			if(!holdIDs.contains(breaker.getItemID()) && !breaker.isUpLimited()) {
				account.refreshHoldsPrice(breaker.getItemID(), breaker.getLatestPrice());
				account.open(breaker.getItemID(), this.getQuantity(breaker.getLatestPrice()), "", breaker.getLatestPrice());
			}
			breakers_sb.append(breaker.getItemID());
			breakers_sb.append(",");
		}
		breakers_sb.deleteCharAt(breakers_sb.length()-1);
		breakers_sb.append("\n");
		
		dailyAmount_sb.append(account.getDailyAmount() + "\n");

		//account.doDailyReport(date);
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
		return result;
	}
	
	private Integer getQuantity(BigDecimal price) {
		return this.quota.divide(price,BigDecimal.ROUND_DOWN).divide(new BigDecimal(100),BigDecimal.ROUND_DOWN).intValue()*100;
	}

}
