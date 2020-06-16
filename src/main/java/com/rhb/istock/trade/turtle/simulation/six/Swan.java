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

import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.fund.Account;
import com.rhb.istock.kdata.Muster;

/*
 * 天鹅
 * 
 * 操作策略
 * 选股: 贵州茅台之类的白天鹅股
 * 买入：跌破89日低点
 * 卖出：上涨超过89%,又跌破21日均线
 * 仓位控制：满仓，每只股票的均衡市值
 *
 */
public class Swan {
	protected static final Logger logger = LoggerFactory.getLogger(Swan.class);

	private Account account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	private Integer pool = 89;
	private Integer top = 3;
	private Map<String,BigDecimal> holds = new HashMap<String,BigDecimal>();
	
	public Swan(BigDecimal initCash) {
		account = new Account(initCash);
		this.initCash = initCash;
	}
	
	public void doIt(Map<String,Muster> musters, LocalDate date, Integer sseiFlag) {
		//logger.info(date.toString());
		Muster muster;
		account.setLatestDate(date);
		
		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		for(String itemID : holdItemIDs) {
			muster = musters.get(itemID);
			if(muster != null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice());
				
				if(holds.containsKey(itemID)) {
					if(holds.get(itemID).compareTo(muster.getLatestPrice())==1) {
						holds.put(itemID, muster.getLatestPrice());
					}
				}else {
					holds.put(itemID, account.getLatestOpenPrice(itemID));
				}
			}
			
		}
		
		//卖出
		Integer ratio;
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				if(muster.isDropAve(21) && !muster.isDownLimited()) { 
					ratio =  Functions.growthRate(muster.getLatestPrice(),holds.get(itemID));
					//logger.info(String.format("%.2f,%.2f,%d\n", holds.get(itemID), muster.getLatestPrice(), ratio));
					if(ratio>55) {
						account.drop(itemID, "1", muster.getLatestPrice());
						holds.remove(itemID);	
						//logger.info("drop\n");
					}
				}
			}
		}				
		
		holdItemIDs = account.getItemIDsOfHolds();
		
		Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
		//List<Muster> dds = new ArrayList<Muster>();  //用list，有重复，表示可以加仓
		
		//确定突破走势的股票
		List<Muster> breakers = this.getTops(musters);
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
						account.dropByOrderID(holdOrderID, "0", muster.getLatestPrice());   //先卖
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
	
	private List<Muster> getTops(Map<String,Muster> musters){
		List<Muster> breakers = new ArrayList<Muster>();
		
		
/*		Set<String> ids = new HashSet<String>();
		ids.add("sh600519");//贵州茅台
		ids.add("sz000858");//五粮液
		ids.add("sz000568");//泸州老窖
		ids.add("sh603288");//海天味业
		ids.add("sh600305");//恒顺醋业
		ids.add("sh600276");//恒瑞医药
		ids.add("sh600009");//上海机场
		ids.add("sz000089");//深圳机场
		ids.add("sh601318");//中国平安
		ids.add("sh601398");//工商银行
		
		Muster m;
		for(String id : ids) {
			m = musters.get(id);
			if(m!=null && m.isDownBreaker()) {
				breakers.add(m);
			}			
		}*/
		
		List<Muster> ms = new ArrayList<Muster>(musters.values());

		Collections.sort(ms, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o2.getTotal_mv().compareTo(o1.getTotal_mv()); //Z-A
			}
		});

		Muster m;
		for(int i=0; i<ms.size() && i<pool && breakers.size()<top; i++) {
			m = ms.get(i);
			if(m!=null 
					&& !m.isUpLimited() 
					&& !m.isDownLimited() 
					&& m.isDownBreaker()
					&& m.getItemID().startsWith("sh688")
					) {
				breakers.add(m);
			}
		}
		
		return breakers;
	}
	
}
