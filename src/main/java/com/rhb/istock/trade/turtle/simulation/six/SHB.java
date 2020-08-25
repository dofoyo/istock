package com.rhb.istock.trade.turtle.simulation.six;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhb.istock.account.Account;
import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.kdata.Muster;

/*
 * 股本
 * 操作策略
 * 买入：起势向上
 * 卖出：跌破21日均线
 * 筛选范围：全部股票中筛选出21个，再从中最多选出3个突破的（可能会选不出来）
 * 筛选依据：总股本排序，越小越好
 * 仓位控制：满仓，每只股票的均衡市值
 *
 */
public class SHB {
	protected static final Logger logger = LoggerFactory.getLogger(SHB.class);

	private Account account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	private Integer pool = 21;
	private Integer top = 3;
	
	public SHB(BigDecimal initCash) {
		account = new Account(initCash);
		this.initCash = initCash;
	}
	
	public void doIt(Map<String,Muster> musters, List<String> sz50, LocalDate date, Integer sseiFlag) {
		//logger.info(date.toString());
		Muster muster;
		account.setLatestDate(date);

		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		for(String itemID : holdItemIDs) {
			muster = musters.get(itemID);
			if(muster != null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice(), muster.getLatestHighest());
			}
		}
		
		//卖出接近高点的股票
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null && !muster.isDownLimited()) {
				if(Functions.growthRate(muster.getHighest(), muster.getLatestPrice())<=3) { 		
					account.drop(itemID, "接近高点" +muster.getHighest().toString(), muster.getLatestPrice());
				}else if(muster.getHighest().compareTo(muster.getLatestPrice()) <=0) { 		
					account.drop(itemID, "超出高点" + muster.getHighest().toString(), muster.getLatestPrice());
				}else if(muster.getLatestPrice().compareTo(muster.getLowest())==-1) { 		
					account.drop(itemID, "跌破低点" + muster.getLowest().toString(), muster.getLatestPrice());
				}			
			}
		}				
		
		holdItemIDs = account.getItemIDsOfHolds();
		
		Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
		//List<Muster> dds = new ArrayList<Muster>();  //用list，有重复，表示可以加仓
		
		//确定接近低点的股票
		List<Muster> breakers = this.getTops(musters, sz50);
		breakers_sb.append(date.toString() + ",");
		StringBuffer sb = new StringBuffer();
		for(Muster breaker : breakers) {
			if(!holdItemIDs.contains(breaker.getItemID())) {
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
			Set<Integer> holdOrderIDs;
			for(String itemID: holdItemIDs) {
				holdOrderIDs = 	account.getHoldOrderIDs(itemID);
				muster = musters.get(itemID);
				if(muster!=null) {
					for(Integer holdOrderID : holdOrderIDs) {
						account.dropByOrderID(holdOrderID, "调仓", muster.getLatestPrice());   //先卖
						dds.add(muster);						
					}
				}
			}
			
			//System.out.println(dds.size());
			account.openAll(dds);			//后买
		}

		dailyAmount_sb.append(account.getDailyAmount() + "\n");
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
	
	private List<Muster> getTops(Map<String,Muster> musters, List<String> sz50){
		List<Muster>  ms = new ArrayList<Muster>();

		for(String id : sz50) {
			if(musters.get(id) != null) {
				ms.add(musters.get(id));
			}
		}
		
		List<Muster> breakers = new ArrayList<Muster>();

		Collections.sort(ms, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o1.getHLGap().compareTo(o2.getHLGap());
			}
		});

		Muster m;
		for(int i=0; i<ms.size() && i<pool; i++) {
			m = ms.get(i);
			if(m!=null 
					&& !m.isUpLimited() 
					&& !m.isDownLimited() 
					//&& m.isUpBreaker() 
					//&& m.isUp(21)
					//&& Functions.between(m.getVolume_ratio(), 2, 5)
					&& m.getLatestPrice().compareTo(m.getLowest())==1
					&& Functions.growthRate(m.getLatestPrice(), m.getLowest())<=3  //买入接近低点的股票
					) {
				breakers.add(m);
			}
			if(breakers.size()>=top) {
				break;
			}
		}
		
		return breakers;
	}
	
}
