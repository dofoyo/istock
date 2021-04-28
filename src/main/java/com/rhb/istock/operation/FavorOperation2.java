package com.rhb.istock.operation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.rhb.istock.account.Account;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.SelectorService;

/*
 * 买3 + 有盈利5%就跑 +  市值跌幅超过8%清仓
 * 
 * Favor操作模式: 持续跟踪21个交易日，向上突破21日均线就买入, up21
 * 
 * 买入：满仓　＋　市值平均　＋　单只股票不加仓
 * 卖出：跌破21日线或回落超过8%
 */
@Scope("prototype")
@Service("favorOperation2")
public class FavorOperation2 implements Operation {
	protected static final Logger logger = LoggerFactory.getLogger(FavorOperation2.class);

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
/*	@Autowired
	@Qualifier("selectorServiceImp")
	SelectorService selectorServiceImp;*/
	
/*	@Autowired
	@Qualifier("drum")
	Producer producer;*/
	
	private StringBuffer dailyAmount_sb;
	private StringBuffer breakers_sb;
	//private Integer previous_period  = 13; //历史纪录区间，主要用于后面判断
	private Keeper breaksKeeper;  //包含所有创新高的股票,因为当天涨停或价格过高不能买入,等待价格回落后买入
	//private Keeper dropsKeeper; //包含所有跌破21日线卖出的票,在13天内如果涨回21日线,说明调整结束,可以再次买入
	//private Keeper up21Keeper; //包含所有涨回21日线的票
	
	public Map<String,String> run(Account account, Map<LocalDate, List<String>> buyList,LocalDate beginDate, LocalDate endDate, String label, int top, boolean isAveValue, Integer quantityType) {
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		
		//logger.info(buyList.toString());
		
		dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
		breakers_sb = new StringBuffer();
		breaksKeeper = new Keeper(55);  //包含所有创新高的股票,因为当天涨停或价格过高不能买入,等待价格回落后买入
		//up21Keeper = new Keeper(55);  //包含所有突破21线的股票,因为当天涨停或价格过高不能买入,等待价格回落后买入
		//dropsKeeper = new Keeper(55); //包含所有跌破21日线卖出的票,在21天内如果涨回21日线,说明调整结束,可以再次买入
		
		
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++," " + label +  " favorOperation2 run:" + date.toString());
			this.doIt(date, account, buyList.get(date), top, isAveValue,quantityType);
		}
		return this.result(account);
	}
	
	private void doIt(LocalDate date,Account account, List<String> buyList, int top, boolean isAveValue, Integer quantityType) {
		//logger.info(date.toString());

		Map<String,Muster> musters = kdataService.getMusters(date);
		if(musters==null || musters.size()==0) return;
		
		//System.out.println(breaksKeeper);

		//Integer sseiFlag = kdataService.getSseiFlag(date);
		//Integer sseiTrend = kdataService.getSseiTrend(date, previous_period);
		
		Muster muster;
		account.setLatestDate(date);
		
		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		for(String itemID : holdItemIDs) {
			muster = musters.get(itemID);
			if(muster != null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice(), muster.getLatestHighest());
			}
		}
		account.refreshHighestAmount();
		
		//logger.info("sseiFlag =  " + sseiFlag.toString());
		boolean bomb = account.getAmountRatio()<=-8 ? true : false;
		
		//卖出
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null && !muster.isDownLimited()) {
				if(account.isGain(itemID, 5)) {  //有2%的盈利就跑
					account.dropWithTax(itemID, "3", muster.getLatestPrice());
				}
				
				/*if(muster.isDropAve(21) 
						//&& muster.getLatestPrice().compareTo(muster.getClose())==1
						) { 		//跌破21日均线就卖
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
					//dropsKeeper.add(date, itemID);
					//logger.info("dropsKeeper add " + itemID);
				}*/
				
				/*//高位回落超过8%
				if(account.isFallOrder(itemID, -8) 
						//&& muster.getN21Gap()>8
						) {
					account.dropWithTax(itemID, "2", muster.getLatestPrice());
					//dropsKeeper.add(date, itemID);
					//logger.info("dropsKeeper add " + itemID);
				}*/
				
				if(bomb) {
					account.dropWithTax(itemID, "9", muster.getLatestPrice());
				}
			}
		}
		//dropsKeeper.dailySet(date);
		if(bomb) {
			account.reSetHighestAmount();
			breaksKeeper.removeAll();
			//up21Keeper.removeAll();
		}		
		
		//买入清单
		if(buyList!=null && buyList.size()>0) {
			breaksKeeper.addAll(date,new HashSet<String>(buyList));
		}else{
			breaksKeeper.dailySet(date);
		}
		
		Set<String> breaks = breaksKeeper.getIDs();
		Set<Muster> dds = new HashSet<Muster>();  
		for(String id : breaks) {
			muster = musters.get(id); 
			if(muster!=null 
					&& muster.isJustBreaker()
					&& muster.isAboveAveragePrice(21)
					&& muster.isAboveAveragePrice(89)
					) {
				dds.add(muster);
			}
		}

		//logger.info(date.toString() + up21Keeper.getIDs().toString());
		
		breakers_sb.append(date.toString() + ",");
		for(Muster m : dds) {
			breakers_sb.append(m.getItemID());
			breakers_sb.append(",");
		}				
		breakers_sb.deleteCharAt(breakers_sb.length()-1);
		breakers_sb.append("\n");

		//logger.info("dds before ave " + dds.size());
		if(dds.size()>0 && account.isAve(dds.size())) {
//		if(isAveValue) {
			Set<Integer> holdOrderIDs;
			for(String itemID: holdItemIDs) {
				holdOrderIDs = 	account.getHoldOrderIDs(itemID);
				muster = musters.get(itemID);
				if(muster!=null && !muster.isUpLimited() && !muster.isDownLimited()) {
					for(Integer holdOrderID : holdOrderIDs) {
						account.dropByOrderID(holdOrderID, "0", muster.getLatestPrice());   //先卖
						dds.add(muster);						
					}
				}
			}					
		}

		//logger.info("dds after ave " + dds.size());

		if(quantityType==0) {
			account.openAll(dds);			//后买
		}else if(quantityType==1) {
			account.openAllWithFixAmount(dds);
		}else {
			account.openAllWithFixQuantity(dds);
		}
			
		dailyAmount_sb.append(account.getDailyAmount() + "\n");
		

	}
	
	private Map<String,String> result(Account account) {
		if(account == null) return null;
		
		Map<String,String> result = new HashMap<String,String>();
		result.put("CSV", account.getCSV());
		result.put("initCash", account.getInitCash().toString());
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
	
	class Keeper{
		private TreeMap<LocalDate,Set<String>> items = new TreeMap<LocalDate,Set<String>>();
		private Integer top;
		public Keeper(Integer top) {
			this.top = top;
		}
		
		public void dailySet(LocalDate date) {
			if(!items.containsKey(date)) {
				this.add(date, null);
			}
		}
		
		public void remove(String id) {
			Iterator<String> it;
			String str;
			for(Map.Entry<LocalDate, Set<String>> entry : items.entrySet()) {
				it = entry.getValue().iterator();
				while(it.hasNext()) {
					str = it.next();
					if(str.equals(id)) {
						it.remove();
					}
				}
			}
		}
		public void removeAll() {
			Iterator<String> it;
			for(Map.Entry<LocalDate, Set<String>> entry : items.entrySet()) {
				entry.setValue(new HashSet<String>());
			}
		}		
		public void addAll(LocalDate date, Set<String> ids) {
			items.put(date, ids);
			if(items.size()>top) {
				items.remove(items.firstKey());
			}
		}
		
		public void add(LocalDate date, String id) {
			Set<String> ids = items.get(date);
			if(ids==null) {
				ids = new HashSet<String>();
			}
			if(id!=null) {
				ids.add(id);
			}
			items.put(date, ids);
			if(items.size()>top) {
				items.remove(items.firstKey());
			}
		}
		
		public Set<String> getIDs(){
			Set<String> tmp = new HashSet<String>();
			for(Set<String> ids : items.values()) {
				for(String id : ids) {
					if(id != null) {
						tmp.add(id);
					}
				}
			}
			return tmp;
		}
		
		public Set<String> getIDs(Map<String,Muster> musters, List<String> drums){
			Set<String> tmp = new HashSet<String>();
			for(Set<String> ids : items.values()) {
				for(String id : ids) {
					if(id != null) {
						tmp.add(id);
					}
				}
			}
			
			Set<String> results = new HashSet<String>();
			Muster muster;
			for(String id : drums) {
				if(tmp.contains(id)) {
					muster = musters.get(id);
					if(muster!=null && !muster.isUpLimited() && muster.getN21Gap()<=8) {
						results.add(id);
					}
				}
			}

			for(String id : tmp){
				muster = musters.get(id);
				if(muster!=null && !muster.isUpLimited() && muster.isJustBreaker()) {
					results.add(id);
				}
			}

			//System.out.println(drops);
			return results;
		}

		@Override
		public String toString() {
			return "Keeper [items=" + items + ", top=" + top + "]";
		}
		
	}
	
}
