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
import com.rhb.istock.kdata.Muster;

/*
 * 操作策略
 * 买入：21日趋势形成
 * 卖出：跌破21日低点
 * 筛选范围：业绩高增长的股票中选3个
 * 筛选依据：89天的高点和低点形成的通道越窄、价格越低
 * 仓位控制：每只股票不能超过市值的1/3
 *
 */
public class Blue {
	protected static final Logger logger = LoggerFactory.getLogger(Blue.class);

	private Account account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	
	private BigDecimal valueRatio = new BigDecimal(3);  //每只股票不能超过市值的1/3
	private Integer top = 3;

	public Blue(BigDecimal initCash) {
		account = new Account(initCash);
		this.initCash = initCash;
	}
	
	public void doIt(Map<String,Muster> musters, List<String> ms, LocalDate date, Integer sseiFlag) {
		Muster muster;
		account.setLatestDate(date);
		
		//处理在手的股票
		Set<String> holdIDs = account.getItemIDsOfHolds();
		for(String itemID: holdIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice(), muster.getLatestHighest());
				if(sseiFlag==0 && muster.isDropAve(21) && !muster.isDownLimited()) { 		//跌破21日均线就卖
					account.drop(itemID, "跌破dropline", muster.getLatestPrice());
				}				
				if(sseiFlag==1 && muster.isDropLowest(21) && !muster.isDownLimited()) { 		//跌破21日低点就卖
					account.drop(itemID, "跌破lowest", muster.getLatestPrice());
				}
			}
		}
		
		//买入突破走势的股票
		List<Muster> breakers = this.getTops(musters, ms);
/*		breakers_sb.append(date.toString() + ",");
		for(Muster breaker : breakers) {
			if(!holdIDs.contains(breaker.getItemID()) && !breaker.isUpLimited()) {
				account.refreshHoldsPrice(breaker.getItemID(), breaker.getLatestPrice());
				account.open(breaker.getItemID(),breaker.getItemName(), breaker.getIndustry(), this.getQuantity(account.getCash(),account.getTotal(),breaker.getLatestPrice()), "", breaker.getLatestPrice());
			}
			breakers_sb.append(breaker.getItemID());
			breakers_sb.append(",");
		}
		breakers_sb.deleteCharAt(breakers_sb.length()-1);
		breakers_sb.append("\n");
*/	
		
		Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
		Set<String> holdItemIDs = account.getItemIDsOfHolds();
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
		if(!dds.isEmpty()) {
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
	
	private Integer getQuantity(BigDecimal cash, BigDecimal total,BigDecimal price) {
		BigDecimal dd = total.divide(valueRatio,BigDecimal.ROUND_DOWN);
		BigDecimal ee = dd.compareTo(cash)<=0 ? dd : cash;
		return ee.divide(price,BigDecimal.ROUND_DOWN).divide(new BigDecimal(100),BigDecimal.ROUND_DOWN).intValue()*100;
	}
	
	private List<Muster> getTops(Map<String,Muster> musters, List<String> bluechips){
		List<Muster> ms = new ArrayList<Muster>();
		for(String id : bluechips) {
			if(musters.get(id)!=null) {
				ms.add(musters.get(id));
			}
		}
		
		Collections.sort(ms, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o2.cal_volume_ratio().compareTo(o1.cal_volume_ratio()); //Z-A

/*				if(o1.getHLGap().compareTo(o2.getHLGap())==0){
					return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //a-z
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				}*/
/*				if(o1.getHLGap().compareTo(o2.getHLGap())==0){
					if(o1.getLNGap().compareTo(o2.getLNGap())==0){  
						return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //a-z
					}else {
						return o1.getLNGap().compareTo(o2.getLNGap()); //a-z
					}
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				}*/
/*				if(o1.getHLGap().compareTo(o2.getHLGap())==0) {
					return o1.getLNGap().compareTo(o2.getLNGap());
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				}*/
				
/*						if(o1.getHLGap().compareTo(o2.getHLGap())==0){
					if(o1.getN21Gap().compareTo(o2.getN21Gap())==0){  
						return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //a-z
					}else {
						return o1.getN21Gap().compareTo(o2.getN21Gap()); //a-z
					}
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				}*/
			}
		});
		
		List<Muster>  tops = new ArrayList<Muster>();
		for(Muster muster : ms) {
			if(muster!=null 
					&& !muster.isNewLowest() 
					&& !muster.isUpLimited() 
					&& !muster.isDownLimited() 
					&& muster.isUpBreaker() 
					&& muster.isUp(21)
					//&& muster.cal_volume_ratio().compareTo(new BigDecimal(2))==1
					) {
				tops.add(muster);
			}
			
			if(tops.size()>=top) {
				break;
			}
		}
		
		return tops;
	}

}
