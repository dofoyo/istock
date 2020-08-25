package com.rhb.istock.trade.turtle.simulation.six;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import com.rhb.istock.account.Account;
import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.kdata.Muster;
/*
 * 盘整
 * 
 * 操作策略
 * 买入：突破89日高点后,13日内在13日均线处买入
 * 卖出：跌破21日均线
 * 筛选范围：全部股票中筛选出21个，再从中最多选出5个突破的，同时考虑换手率和量比。
 * 筛选依据：89天的高点和低点形成的通道越窄、价格越低
 * 仓位控制：满仓，每只股票的均衡市值
 *
 */
public class HLB2 {
	protected static final Logger logger = LoggerFactory.getLogger(HLB_try.class);

	private Account account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	private Integer pool = 21;
	private Integer top = 3;
	private Integer hlgap_min = 8;
	private Integer hlgap_max = 25;
	private Map<String,Integer> breakers = new HashMap<String,Integer>();
/*	private BigDecimal turnover_rate_f_max = new BigDecimal(8.63);
	private BigDecimal turnover_rate_f_min = new BigDecimal(0.18);
	private BigDecimal volumn_ratio = new BigDecimal(3.22);
*/	
	//private BigDecimal turnover_rate_f = new BigDecimal(14.8);
	//private BigDecimal volumn_ratio = new BigDecimal(1.58);
	//private BigDecimal total_mv = new BigDecimal("7000000000");
	
	public HLB2(BigDecimal initCash) {
		account = new Account(initCash);
		this.initCash = initCash;
	}
	
	public void doIt(Map<String,Muster> musters, LocalDate date, Integer sseiFlag) {
		Muster muster;
		account.setLatestDate(date);
		Integer value;
		Map.Entry<String, Integer> entry;
		for (Iterator<Map.Entry<String, Integer>> iterator = this.breakers.entrySet().iterator(); iterator.hasNext();){
		    entry = iterator.next();
			value = entry.getValue();
			if(value>=13) {    //突破后13日内回调有效
				iterator.remove();
			}else {
				entry.setValue(value++);
			}
		}
		
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
					account.drop(itemID, "1", muster.getLatestPrice());
				}
/*				if(muster.isDropLowest(13) && !muster.isDownLimited()) { 		//跌破21日低点就卖
					account.drop(itemID, "1", muster.getLatestPrice());
				}*/
/*				if(sseiFlag==0 && muster.isDropAve(13) && !muster.isDownLimited()) { 		//跌破13日均线就卖
					account.drop(itemID, "1", muster.getLatestPrice());
				}				
				if(sseiFlag==1 && muster.isDropLowest(34) && !muster.isDownLimited()) { 		//跌破21日低点就卖
					account.drop(itemID, "2", muster.getLatestPrice());
				}*/
			}
		}
		
			holdItemIDs = account.getItemIDsOfHolds();
			
			Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
			//List<Muster> dds = new ArrayList<Muster>();  //用list，有重复，表示可以加仓
			
			//确定突破走势的股票
			this.setBreakers(new ArrayList<Muster>(musters.values()));
			List<Muster> bs = this.getBreakers(musters);
			//breakers.addAll(keeps.getUps(musters));
			
			breakers_sb.append(date.toString() + ",");
			StringBuffer sb = new StringBuffer();
			for(Muster breaker : bs) {
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
	
	private List<Muster> getBreakers(Map<String,Muster> musters){
		List<Muster> bs = new ArrayList<Muster>();
		String id;
		Integer days;
		Muster m;
		Map.Entry<String, Integer> entry;
		for (Iterator<Map.Entry<String, Integer>> iterator = this.breakers.entrySet().iterator(); iterator.hasNext();){
		    entry = iterator.next();
			id = entry.getKey();
			days = entry.getValue();
			if(musters.containsKey(id)) {
				m = musters.get(id);
				if(m.getLatestPrice().compareTo(m.getAveragePrice13())<0
						&& m.getLatestPrice().compareTo(m.getAveragePrice21())>=0
						) {  //突破后跌破13日线
					bs.add(musters.get(id));
					iterator.remove();
				}else if(m.getLatestPrice().compareTo(m.getAveragePrice21())<0) {
					iterator.remove();
				}
			}
		}
		
		return bs;
	}
	
	private void setBreakers(List<Muster> musters){
		//List<Muster> breakers = new ArrayList<Muster>();

		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				//return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
			
				if(o1.getHLGap().compareTo(o2.getHLGap())==0){
					return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //a-z
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				}
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
		for(int i=0; i<musters.size() && i<pool; i++) {
		//for(int i=0; i<musters.size() && i<pool; i++) {
			m = musters.get(i);
			if(m!=null 
					&& !m.isUpLimited() 
					&& !m.isDownLimited() 
					&& m.isUpBreaker() 
					//&& m.isAboveAverageAmount()
					//&& Functions.between(m.getHLGap(), hlgap_min, hlgap_max)
					//&& m.isUp(21)
					//&& m.getVolume_ratio().compareTo(new BigDecimal(2))==1
					//&& m.getTurnover_rate_f().compareTo(turnover_rate_f_max)<0
					//&& m.getTurnover_rate_f().compareTo(turnover_rate_f_min)>0
					//&& m.getTurnover_rate_f().compareTo(turnover_rate_f)<0
					//&& m.getTotal_mv().compareTo(total_mv)<0
					&& !breakers.containsKey(m.getItemID())
					) {
				breakers.put(m.getItemID(),0);
			}
		}
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
