package com.rhb.istock.operation;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.rhb.istock.account.Account;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.producer.Producer;


/*
 * 进取型操作模式　aggressive
 * 
 * 买入：满仓　＋　市值平均　＋　单只股票不加仓
 * 卖出：跌破21日线
 */
@Scope("prototype")
@Service("optimizeOperation")
public class OptimizeOperation implements Operation {
	protected static final Logger logger = LoggerFactory.getLogger(OptimizeOperation.class);

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("drum")
	Producer producer;
	
	private StringBuffer dailyAmount_sb;
	private StringBuffer breakers_sb;
	Integer previous_period  = 13; //历史纪录区间，主要用于后面判断
	private Set<String> wants = new HashSet<String>();  //包含所有创新高的股票,因为当天涨停或价格过高不能买入,等待价格回落后买入
	private DropsPool drops = new DropsPool(); //包含所有跌破21日线卖出的票,在21天内如果涨回21日线,说明调整结束,可以再次买入
	
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
					drops.put(date, itemID, muster.getAveragePrice21());
				}
			}
		}
		
		//买入清单
		Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
		//List<Muster> dds = new ArrayList<Muster>();  //用list，有重复，表示可以加仓
		//if(sseiFlag==1 && sseiTrend>=0) {　　//行情好，才买入
			holdItemIDs = account.getItemIDsOfHolds();
			
			//选择股票
			List<String> ids = buyList.get(date);
			if(ids!=null && ids.size()>0) {
				wants.addAll(ids);
			}
			
			List<String> drums = producer.getResults(date);
			wants.addAll(drops.getIDs(musters, drums));
			
			if(wants.size()>0) {
				breakers_sb.append(date.toString() + ",");
				Iterator<String> it = wants.iterator();
				String id;
				while(it.hasNext()) {
					id = it.next();
					if(!holdItemIDs.contains(id)) {
						muster = musters.get(id); 
						if(muster!=null && !muster.isUpLimited() && muster.getN21Gap()<=8) {
							dds.add(muster);
							breakers_sb.append(id);
							breakers_sb.append(",");
							it.remove();
						}
					}
				}
				breakers_sb.deleteCharAt(breakers_sb.length()-1);
				breakers_sb.append("\n");
			}
			//System.out.println(wants);
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
	
	class DropsPool{
		private TreeMap<LocalDate,Map<String,BigDecimal>> drops = new TreeMap<LocalDate,Map<String,BigDecimal>>();
		private Integer top = 21;
		private Map<String, BigDecimal> ds;
		public void put(LocalDate date, String id, BigDecimal avp21) {
			ds = drops.get(date);
			if(ds==null) {
				ds = new HashMap<String, BigDecimal>();
			}
			ds.put(id,avp21);
			drops.put(date, ds);
			if(drops.size()>21) {
				drops.remove(drops.firstKey());
			}
		}
		
		public Set<String> getIDs(Map<String,Muster> musters, List<String> drums){
			Set<String> results = new HashSet<String>();
			Muster muster;
			for(Map<String,BigDecimal> ids : drops.values()) {
				for(Map.Entry<String, BigDecimal> entry : ids.entrySet()) {
					muster = musters.get(entry.getKey());
					if(muster!=null && drums.contains(entry.getKey())) {
						results.add(entry.getKey());
					}
				}
			}
			
			return results;
		}
	}
	
}
