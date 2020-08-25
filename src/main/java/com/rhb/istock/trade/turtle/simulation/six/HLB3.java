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
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhb.istock.account.Account;
import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.kdata.Muster;
/*
 * 高价价新高
 * 
 * 操作策略
 * 买入：突破89日高点
 * 卖出：跌破21日均线
 * 筛选范围：全部股票,按价格从大到小排序。
 * 筛选依据：选出价格最高的55个
 * 仓位控制：满仓，每只股票的均衡市值
 *
 */
public class HLB3 {
	protected static final Logger logger = LoggerFactory.getLogger(HLB_try.class);

	private Account account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	private Integer pool = 55;
	private Integer top = 1;
	private Integer hlgap_min = 8;
	private Integer hlgap_max = 25;
/*	private BigDecimal turnover_rate_f_max = new BigDecimal(8.63);
	private BigDecimal turnover_rate_f_min = new BigDecimal(0.18);
	private BigDecimal volumn_ratio = new BigDecimal(3.22);
*/	
	//private BigDecimal turnover_rate_f = new BigDecimal(14.8);
	//private BigDecimal volumn_ratio = new BigDecimal(1.58);
	//private BigDecimal total_mv = new BigDecimal("7000000000");
	
	public HLB3(BigDecimal initCash) {
		account = new Account(initCash);
		this.initCash = initCash;
	}
	
	public void doIt(Map<String,Muster> musters, LocalDate date, Integer sseiFlag) {
		Muster muster;
		account.setLatestDate(date);
/*		if(sseiFlag==1) {
			this.top = 8;
		}else {
			this.top = 1;
		}*/
		
		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		for(String itemID : holdItemIDs) {
			muster = musters.get(itemID);
			if(muster != null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice(), muster.getLatestHighest());
			}
		}
		
		//卖出跌破dropline或lowest的股票
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				if(muster.isDropAve(21) && !muster.isDownLimited()) { 		//跌破21日均线就卖
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
				}
				
				/*//涨幅超过21%，则跌破8日线
				if(account.getUpRatio(itemID)>=21 && muster.isDropAve(8) && !muster.isDownLimited()) {
					account.dropWithTax(itemID, "up "+account.getUpRatio(itemID).toString()+" and drop_ave8", muster.getLatestPrice());
				}*/
				
/*				if(muster.isDropLowest(13) && !muster.isDownLimited()) { 		//跌破21日低点就卖
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
				}*/
/*				if(sseiFlag==0 && muster.isDropAve(13) && !muster.isDownLimited()) { 		//跌破13日均线就卖
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
				}				
				if(sseiFlag==1 && muster.isDropLowest(34) && !muster.isDownLimited()) { 		//跌破21日低点就卖
					account.dropWithTax(itemID, "2", muster.getLatestPrice());
				}*/
			}
		}
		
			holdItemIDs = account.getItemIDsOfHolds();
			
			Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
			//List<Muster> dds = new ArrayList<Muster>();  //用list，有重复，表示可以加仓
			
			//确定突破走势的股票
			List<Muster> breakers = this.getBreakers(new ArrayList<Muster>(musters.values()));
			//breakers.addAll(keeps.getUps(musters));
			
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
			// 当cash不够买入新股时，要卖出市值高与平均值的股票。
			//此举可避免高位加仓的现象出现
			if(!dds.isEmpty()) {
				//if(!account.isEnoughCash(dds.size())) {
					Set<Integer> holdOrderIDs;
					for(String itemID: holdItemIDs) {
						holdOrderIDs = 	account.getHoldOrderIDs(itemID);
						muster = musters.get(itemID);
						if(muster!=null) {
							for(Integer holdOrderID : holdOrderIDs) {
								//if(account.isAboveAveValue(holdOrderID)==1) {
									account.dropByOrderID(holdOrderID, "0", muster.getLatestPrice());   //先卖
									dds.add(muster);						
								//}
							}
						}
					}
				//}
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
	
	private List<Muster> getBreakers(List<Muster> musters){
		List<Muster> breakers = new ArrayList<Muster>();

		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o2.getLatestPrice().compareTo(o1.getLatestPrice()); //a-z
			
				/*if(o1.getHLGap().compareTo(o2.getHLGap())==0){
					return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //a-z
					//return o2.getLatestPrice().compareTo(o1.getLatestPrice()); //z-a
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				}*/
			}
		});
		
/*		Distribution distribution = new Distribution();
		for(Muster m : musters) {
			if(m!=null && !m.isUpLimited() && !m.isDownLimited() && m.isUpBreaker() && m.isUp()) {
				distribution.add(m.getHLGap(), m.getItemID());
			}
		}
		distribution.show();
*/		
		Muster m;
		for(int i=0; i<musters.size() && breakers.size()<top && i<pool; i++) {
		//for(int i=0; i<musters.size() && i<pool; i++) {
			m = musters.get(i);
			if(m!=null 
					&& !m.isUpLimited() 
					//&& !m.isDownLimited() 
					&& m.isUpBreaker() 
					//&& m.isGapBreaker() 
					//&& m.isAboveAverageAmount()
					//&& Functions.between(m.getHLGap(), hlgap_min, hlgap_max)
					//&& m.isUp(21)
					//&& m.getVolume_ratio().compareTo(new BigDecimal(2))==1
					//&& m.getTurnover_rate_f().compareTo(turnover_rate_f_max)<0
					//&& m.getTurnover_rate_f().compareTo(turnover_rate_f_min)>0
					//&& m.getTurnover_rate_f().compareTo(turnover_rate_f)<0
					//&& m.getTotal_mv().compareTo(total_mv)<0
					) {
				breakers.add(m);
			}
		}
		
		return breakers;
	}
	
	class Distribution {
		TreeMap<Integer, Set<String>> ids = new TreeMap<Integer, Set<String>>();
		public void add(Integer i, String id) {
			Set<String> ss = ids.get(i);
			if(ss == null) {
				ss = new HashSet<String>();
				ids.put(i, ss);
			}
			ss.add(id);
		}
		
		public void show() {
			StringBuffer sb = new StringBuffer();
			for(Map.Entry<Integer, Set<String>> entry : ids.entrySet()) {
				sb.append(entry.getKey());
				sb.append(":");
				sb.append(entry.getValue().size());
				sb.append("\n");
			}
			logger.info(sb.toString());
		}
	}
}
