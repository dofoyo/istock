package com.rhb.istock.operation;

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
import com.rhb.istock.operation.CommOperation2.Keeper;


/*
 *  买2+3+* 模式
 *  
 * 买入：根据传入的buyList清单买入，如果涨停，就在第二天买入。卖出后跟踪21日，如果又向上突破21日线再次买入
 *
 * 卖出：跌破21日线；  市值跌幅超过8%,清仓, 从头再来
 */
@Scope("prototype")
@Service("commOperation3")
public class CommOperation3 implements Operation {
	protected static final Logger logger = LoggerFactory.getLogger(CommOperation2.class);

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	private StringBuffer dailyAmount_sb;
	private StringBuffer breakers_sb;
	//private Integer previous_period  = 13; //历史纪录区间，主要用于后面判断
	private Keeper breaksKeeper;  //包含所有创新高的股票,因为当天涨停或价格过高不能买入,等待价格回落后买入
	private Keeper dropsKeeper; //包含所有跌破21日线卖出的票,在13天内如果涨回21日线,说明调整结束,可以再次买入
	private Keeper up21Keeper; //包含所有涨回21日线的票
	//private boolean bomb;
	//private Integer previous_sseiFlag;
	
	public Map<String,String> run(Account account, Map<LocalDate, List<String>> buyList,LocalDate beginDate, LocalDate endDate, String label, int top, boolean isAveValue, Integer quantityType) {
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		
		//logger.info(buyList.toString());
		
		dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
		breakers_sb = new StringBuffer();
		breaksKeeper = new Keeper(21);  //包含所有创新高的股票,因为当天涨停或价格过高不能买入,等待价格回落后买入
		dropsKeeper = new Keeper(21); //包含所有跌破21日线卖出的票,在21天内如果涨回21日线,说明调整结束,可以再次买入
		up21Keeper = new Keeper(21);  //包含所有创新高的股票,因为当天涨停或价格过高不能买入,等待价格回落后买入
		//bomb = false;
		//this.previous_sseiFlag = 1;
		
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++," " + label +  " commOperation3 run:" + date.toString());
			this.doIt(date, account, buyList.get(date), top, isAveValue,quantityType);
		}
		return this.result(account);
	}
	
	private void doIt(LocalDate date,Account account, List<String> buyList, int top, boolean isAveValue, Integer quantityType) {
		//logger.info(date.toString());

		Map<String,Muster> musters = kdataService.getMusters(date);
		if(musters==null || musters.size()==0) return;
		
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
				/*if(sseiFlag==0 && sseiTrend==0 && (muster.isDropAve(13) || account.isFallOrder(itemID, -8))
						// && sseiTrend!=1
						) { 		//大盘不好就卖
					account.dropWithTax(itemID, "2", muster.getLatestPrice());
					dropsKeeper.add(date, itemID);
					//logger.info("droped  " + itemID + " , for ssei drop!");
				}*/
				if(muster.isDropAve(21) 
						//&& muster.getLatestPrice().compareTo(muster.getClose())==1
						) { 		//跌破21日均线就卖
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
					dropsKeeper.add(date, itemID);
					//logger.info("dropsKeeper add " + itemID);
				}
				
/*				//高位回落超过8%
				if(account.isFallOrder(itemID, -8)) {
					account.dropWithTax(itemID, "2", muster.getLatestPrice());
					dropsKeeper.add(date, itemID);
					//logger.info("dropsKeeper add " + itemID);
				}*/
				
				if(bomb) {
					account.dropWithTax(itemID, "9", muster.getLatestPrice());
					dropsKeeper.add(date, itemID);
				}
			}
		}
		if(bomb) {
			account.reSetHighestAmount();
			breaksKeeper.removeAll();
			dropsKeeper.removeAll();
			up21Keeper.removeAll();
		}		
		
		//logger.info(date.toString() + ", bomb = " + (this.bomb ? " Y " : ""));
		//logger.info(date.toString() + ", sseiFlag = " + sseiFlag.toString());
		//logger.info(date.toString() + ", previous_sseiFlag = " + previous_sseiFlag.toString());
		//logger.info(date.toString() + ", breaksKeeper = " + breaksKeeper.getIDs());
		//logger.info(date.toString() + ", dropsKeeper = " + dropsKeeper.getIDs());
		//logger.info(date.toString() + ", up21Keeper = " + up21Keeper.getIDs());

		
		dropsKeeper.dailySet(date);	
		
		Set<Muster> dds = new HashSet<Muster>();  
		if(buyList!=null && buyList.size()>0) {
			breaksKeeper.addAll(date,new HashSet<String>(buyList));
			/*for(String id : buyList) {
				muster = musters.get(id); 
				if(muster!=null && !muster.isUpLimited() 
						//&& sseiFlag==1 
						//&& sseiTrend==1
						) {
					dds.add(muster);
				}else {
					breaksKeeper.add(date, id);
				}
			}*/
			
		}else{
			breaksKeeper.dailySet(date);
		}

		Set<String> breaks = breaksKeeper.getIDs();
		Set<String> drops = dropsKeeper.getIDs();
		Set<String> up21s = up21Keeper.getIDs();
		for(String id : breaks) {
			muster = musters.get(id); 
			if(muster!=null && !muster.isUpLimited() 
					&& muster.isAboveAveragePrice(21)
					&& muster.isAboveAveragePrice(89)
					&& muster.getN21Gap()<=5
					&& !drops.contains(id)
					&& !up21s.contains(id)
					) {
				dds.add(muster);
				breaksKeeper.remove(id);
			}
			
			if(muster!=null 
					&& muster.isJustBreaker()
					) {
				up21Keeper.add(date, id);
				breaksKeeper.remove(id);
			}
		}
		
		//drops = dropsKeeper.getIDs();
		for(String id : drops) {
			muster = musters.get(id); 
			if(muster!=null 
					&& muster.isJustBreaker()
					) {
				up21Keeper.add(date, id);
				dropsKeeper.remove(id);
			}
		}

		Set<String> ids = up21Keeper.getIDs();
		for(String id : ids) {
				muster = musters.get(id); 
				if(muster!=null 
						&& !muster.isUpLimited() 
						&& muster.isAboveAveragePrice(21)
						&& muster.isAboveAveragePrice(89)
						&& muster.getN21Gap()<=5
						//&& sseiFlag==1 
						//&& sseiTrend==1
						//&& !this.bomb

						) {
						dds.add(muster);
						up21Keeper.remove(id);
						//dropsKeeper.remove(id);
				}				
		}
		
		breakers_sb.append(date.toString() + ",");
		for(Muster m : dds) {
			breakers_sb.append(m.getItemID());
			breakers_sb.append(",");
		}				
		breakers_sb.deleteCharAt(breakers_sb.length()-1);
		breakers_sb.append("\n");

		//logger.info(date.toString() + " breaks: " + breaksKeeper.getIDs());
		//logger.info(date.toString() + " drops: " + dropsKeeper.getIDs());
		//logger.info(date.toString() + " up21s: " + up21Keeper.getIDs());
		
		
		//if(isAveValue) {
		//if(dds.size()>0 && account.isAve(dds.size())) {
		if(isAveValue && dds.size()>0 && account.isAve(dds.size())) {
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
		
		public void removeAll() {
			Iterator<String> it;
			for(Map.Entry<LocalDate, Set<String>> entry : items.entrySet()) {
				entry.setValue(new HashSet<String>());
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
/*			for(String id : drums) {
				if(tmp.contains(id)) {
					muster = musters.get(id);
					if(muster!=null && !muster.isUpLimited() && muster.getN21Gap()<=8) {
						results.add(id);
					}
				}
			}*/

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
