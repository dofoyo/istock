package com.rhb.istock.operation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.rhb.istock.account.Account;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.producer.Producer;
import com.rhb.istock.selector.fina.FinaService;


/*
 * 低吸高抛
 * 
 * 买入：跌破最点
 * 卖出：跌破21日线,且有盈利
 */
@Scope("prototype")
@Service("huntingOperation")
public class HuntingOperation implements Operation {
	protected static final Logger logger = LoggerFactory.getLogger(HuntingOperation.class);

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("finaService")
	FinaService finaService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	private StringBuffer dailyAmount_sb;
	private StringBuffer dailyHolds_sb;
	private StringBuffer breakers_sb;
	//private Integer rate = -8;
	private Set<String> up21s;
	
	public Map<String,String> run(Account account, Map<LocalDate, List<String>> buyList,Map<LocalDate, List<String>> sellList,LocalDate beginDate, LocalDate endDate, String label, int top, boolean isAveValue, Integer quantityType) {
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		
		dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
		dailyHolds_sb = new StringBuffer("date,itemID,itemName,open,close,quantity,profit,days\n");
		breakers_sb = new StringBuffer();
		up21s = new HashSet<String>();
		
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++," " + label +  " huntingOperation run:" + date.toString());
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
		dailyHolds_sb.append(account.getHoldStateString());
		
		//卖出
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null && muster.isJustBreaker()) {
				up21s.add(itemID);
			}
		
		}
		Iterator<String> iterator = up21s.iterator();
		String theid;
		while(iterator.hasNext()) {
			theid = iterator.next();
			muster = musters.get(theid);
			if(muster!=null && !muster.isDownLimited()) {
				if(muster.isDropAve(21)   //跌破21日均线且有盈利
						&& account.isGain(theid, 8)//有盈利
						) { 		
					account.dropWithTax(theid, "1", muster.getLatestPrice());
					iterator.remove();
				}
			}
		}
		
		//买入清单
		Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
		holdItemIDs = account.getItemIDsOfHolds();

		if(buyList!=null && buyList.size()>0) {
			String id;
			for(int i=0, j=0; i<buyList.size() && j<top; i++) {
				id = buyList.get(i);
				if(!holdItemIDs.contains(id)) {
					muster = musters.get(id); 
					if(muster!=null
							) {
						dds.add(muster);
					}
				}
			}
		}
		
		breakers_sb.append(date.toString() + ",");
		for(Muster m : dds) {
			breakers_sb.append(m.getItemID());
			breakers_sb.append(",");
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
		result.put("dailyHolds", dailyHolds_sb.toString());
		return result;
	}
	
}
