package com.rhb.istock.operation;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.rhb.istock.account.Account;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;


/*
 * 进取型操作模式　aggressive
 * 
 * 买入：满仓　＋　市值平均　＋　单只股票不加仓
 * 卖出：跌破21日线
 */
@Scope("prototype")
@Service("aggressiveOperation")
public class AggressiveOperation implements Operation {
	protected static final Logger logger = LoggerFactory.getLogger(AggressiveOperation.class);

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	private StringBuffer dailyAmount_sb;
	private StringBuffer breakers_sb;
	Integer previous_period  = 13; //历史纪录区间，主要用于后面判断
	
	public Map<String,String> run(Account account, Map<LocalDate, List<String>> buyList,LocalDate beginDate, LocalDate endDate, String label, int top, boolean isAveValue, Integer quantityType) {
		dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
		breakers_sb = new StringBuffer();

		
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {

			Progress.show((int)days, i++," " + label +  " run:" + date.toString());
			
			this.doIt(date, account, buyList, top, isAveValue,quantityType);
		}
		
		return this.result(account);
	}
	
	private void doIt(LocalDate date,Account account, Map<LocalDate, List<String>> buyList, int top, boolean isAveValue, Integer quantityType) {
		Map<String,Muster> musters = kdataService.getMusters(date);
		if(musters==null || musters.size()==0) return;

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
		
		//卖出
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null && !muster.isDownLimited()) {
				if(muster.isDropAve(21)) { 		//跌破21日均线就卖
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
				}
				
/*				//高位回落超过8%
				if(account.isFallOrder(itemID, -8)) {
					account.dropWithTax(itemID, "2", muster.getLatestPrice());
					//dropsKeeper.put(date, itemID, muster.getAveragePrice21());
				}
*/			}
		}
		
		//买入清单
		Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
		//List<Muster> dds = new ArrayList<Muster>();  //用list，有重复，表示可以加仓
		//if(sseiFlag==1 && sseiTrend>=0) {　　//行情好，才买入
			holdItemIDs = account.getItemIDsOfHolds();
			
			//选择股票
			List<String> ids = buyList.get(date);
			if(ids!=null && ids.size()>0) {
				breakers_sb.append(date.toString() + ",");
				String id;
				for(int i=0, j=0; i<ids.size() && j<top; i++) {
					id = ids.get(i);
					if(!holdItemIDs.contains(id)) {
						muster = musters.get(id); 
						if(muster!=null && !muster.isUpLimited()
								) {
							dds.add(muster);
							j++;
							breakers_sb.append(id);
							breakers_sb.append(",");
						}
					}
				}
				breakers_sb.deleteCharAt(breakers_sb.length()-1);
				breakers_sb.append("\n");
			}
		//}
			
		//市值平均
		//if(dds.size()>0  //买入新股
				//|| (sseiFlag==1 && sseiTrend>=0 && account.getHLRatio()>=21)  //行情好
				//|| account.getHLRatio()>=34 //市值差异大
				//) {  
			if(isAveValue) {
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
		//}

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
	
}
