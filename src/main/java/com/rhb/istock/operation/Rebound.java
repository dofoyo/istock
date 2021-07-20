package com.rhb.istock.operation;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.rhb.istock.account.Account;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;


/*
 *  买2模式
 *  
 * 买入：买2机会出现
 *
 * 卖出：有盈利就卖；跌破21日线。
 */
@Scope("prototype")
@Service("rebound")
public class Rebound implements Operation {
	protected static final Logger logger = LoggerFactory.getLogger(Rebound.class);

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	private StringBuffer dailyHolds_sb;
	private StringBuffer dailyAmount_sb;
	private StringBuffer breakers_sb;
	//private Integer previous_period  = 21; //历史纪录区间，主要用于后面判断
	private Keeper breaksKeeper;  //等待买2信号
	//private Keeper up21Keeper; //等待买3信号
	private Keeper dropsKeeper; //已卖出的票
	//private boolean bombing = false;
	//private Integer previous_sseiFlag;
	//private Set<LocalDate> bombingDates;
	private Integer n21Gap = 5;
	private Integer hnGap = 5;
	private Integer gain = 0;
	
	public Map<String,String> run(Account account, Map<LocalDate, List<String>> buyList,Map<LocalDate, List<String>> sellList,LocalDate beginDate, LocalDate endDate, String label, int top, boolean isAveValue, Integer quantityType) throws Exception{
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		
		//logger.info(buyList.toString());
		
		dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
		dailyHolds_sb = new StringBuffer("date,itemID,itemName,open,close,quantity,profit,days\n");
		breakers_sb = new StringBuffer();
		breaksKeeper = new Keeper(13); 
		//up21Keeper = new Keeper(13); 
		dropsKeeper = new Keeper(13); 
		//bomb = false;
		//this.previous_sseiFlag = 1;
		//bombingDates = kdataService.getBombingDates();
		
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

/*		Integer sseiFlag = kdataService.getSseiFlag(date);
		Integer sseiTrend = kdataService.getSseiTrend(date, previous_period);
		Integer gain = sseiFlag==-1 && sseiTrend==-1 ? 2 : 5;
*/		
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
		dailyHolds_sb.append(account.getHoldStateString());
		
		//logger.info("sseiFlag =  " + sseiFlag.toString());
		//boolean bomb = account.getAmountRatio()<=-8 ? true : false;
		
		//卖出
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null && !muster.isDownLimited()) {
				if(muster.isDropAve(21)) { 		//跌破21日均线就卖
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
					dropsKeeper.add(date, itemID);
					/*if(dropsKeeper.getTimes(itemID)>=2) {
						breaksKeeper.remove(itemID);
						up21Keeper.remove(itemID);
						dropsKeeper.remove(itemID);
					}*/
					
				}
				
/*				if(account.isFallOrder(itemID, -8) && muster.getN21Gap()>8) {  //高位快速回落超过5%，止盈
					account.dropWithTax(itemID, "2", muster.getLatestPrice());
				}*/
				
				if(account.isGain(itemID, gain)) {  //有盈利就跑
					account.dropWithTax(itemID, "3", muster.getLatestPrice());
					dropsKeeper.add(date, itemID);
				}
				
/*				if(account.isFallOrder(itemID, -8)) {   //高位回落超过8%
					account.dropWithTax(itemID, "2", muster.getLatestPrice());
				}
*/				
				
/*				if(bomb) {  //市值跌8%
					account.dropWithTax(itemID, "9", muster.getLatestPrice());
					dropsKeeper.add(date, itemID);
				}*/
			}
		}
/*		if(bomb) {
			account.reSetHighestAmount();
			breaksKeeper.removeAll();
			up21Keeper.removeAll();
			dropsKeeper.removeAll();
		}	*/	
		
		//dropsKeeper.dailySet(date);
		
		if(buyList!=null && buyList.size()>0) {
			breaksKeeper.addAll(date,new HashSet<String>(buyList));
			//up21Keeper.addAll(date, new HashSet<String>(buyList));
		}else{
			breaksKeeper.dailySet(date);
			//up21Keeper.dailySet(date);
		}

		List<Muster> xxs = new ArrayList<Muster>();  
		Set<String> breaks = breaksKeeper.getIDs();
		for(String id : breaks) {
			muster = musters.get(id); 
			if(muster!=null
					&& muster.isAboveAveragePrice(21)
					&& muster.isAboveAveragePrice(89)
					//&& !account.isHold(id)
					&& muster.getN21Gap()<=n21Gap   //买2信号出现
					&& muster.getN21Gap()>=0
					&& muster.getHNGap()>hnGap
					) {
				//if(!bombingDates.contains(date)) {
					xxs.add(muster);
				//}
				//breaksKeeper.remove(id);
			}else if(muster!=null && muster.isDropAve(21)) { 		//跌破21日均线
				breaksKeeper.remove(id);
			}
		}
		
/*		Set<String> up21s = up21Keeper.getIDs();
		for(String id : up21s) {
			muster = musters.get(id); 
			if(muster!=null 
					&& muster.isAboveAveragePrice(21)
					&& muster.isAboveAveragePrice(89)
					&& muster.isJustBreaker()   //买3信号出现
					//&& !account.isHold(id)
					) {
				//if(!bombingDates.contains(date)) {
					xxs.add(muster);
				//}
				//up21Keeper.remove(id);
				//logger.info("remove " + id + " for is just breaker!");
			}
		}
		
		Set<String> drops = dropsKeeper.getIDs();
		for(String id : drops) {
			muster = musters.get(id); 
			if(muster!=null 
					&& muster.isAboveAveragePrice(21)
					&& muster.isAboveAveragePrice(89)
					//&& !account.isHold(id)
				&& (muster.isJustBreaker()   //买3信号出现
							|| (muster.getN21Gap()<=n21Gap && muster.getN21Gap()>=0))   //买2信号出现
				
					) {
				//if(!bombingDates.contains(date)) {
					xxs.add(muster);
				//}				
				//dropsKeeper.remove(id);
			}
		}
*/
		Collections.sort(xxs, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				//return o1.getLatestPrice().compareTo(o2.getLatestPrice());
				return o1.getHLGap().compareTo(o2.getHLGap());
				//return o1.getHNGap().compareTo(o2.getHNGap());
			}
		});
		
		Set<Muster> dds = new HashSet<Muster>();  
		for(int i=0; i<xxs.size() && i<top; i++) {
			muster = xxs.get(i);
			if(muster!=null && !muster.isUpLimited() && !account.isHold(muster.getItemID()) && !dropsKeeper.isContains(muster.getItemID())) {
				dds.add(muster);
			}
		}
		
		Set<Muster> mms = new HashSet<Muster>();
		breakers_sb.append(date.toString() + ",");
		for(Muster m : xxs) {
			if(!mms.contains(m)) {
				breakers_sb.append(m.getItemID());
				breakers_sb.append(",");
				mms.add(m);
			}
		}				
		breakers_sb.deleteCharAt(breakers_sb.length()-1);
		breakers_sb.append("\n");

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

		//logger.info(date.toString() + " up21Keeper" + up21Keeper.getIDs());

		if(quantityType==0) {
			account.openAll(dds);			//后买
		}else if(quantityType==1) {
			account.openAllWithFixAmount(dds);
		}else {
			account.openAllWithFixQuantity(dds);
		}
			
		dailyAmount_sb.append(account.getDailyAmount() + "\n");
		//logger.info(dropsKeeper.toString());
		
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
		result.put("dailyHolds", dailyHolds_sb.toString());
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
		
		public Integer getTimes(String id) {
			Integer times = 0;
			for(Map.Entry<LocalDate, Set<String>> entry : items.entrySet()) {
				if(entry.getValue().contains(id)) {
					times ++;
					//logger.info(entry.getKey() + " drop " + id + ", times:" + times);
				}
			}
			return times;
		}
		
		public boolean isContains(String id) {
			for(Map.Entry<LocalDate, Set<String>> entry : items.entrySet()) {
				if(entry.getValue().contains(id)) {
					return true;
				}
			}
			return false;
			
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
