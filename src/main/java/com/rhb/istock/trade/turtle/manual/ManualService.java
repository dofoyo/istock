package com.rhb.istock.trade.turtle.manual;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.account.Account;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.index.tushare.IndexServiceTushare;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.trade.turtle.simulation.six.TurtleMusterSimulation;
import com.rhb.istock.trade.turtle.simulation.six.repository.AmountEntity;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Service("manualService")
public class ManualService {
	protected static final Logger logger = LoggerFactory.getLogger(TurtleMusterSimulation.class);

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("indexServiceTushare")
	IndexServiceTushare indexServiceTushare;
	
	private Map<LocalDate, String> selects = new TreeMap<LocalDate, String>();
	BigDecimal initCash = new BigDecimal(1000000);

	public void addSelects(LocalDate date, String itemID) {
		this.selects.put(date, itemID);
	}
	
	public void deleteSelects(LocalDate date) {
		this.selects.remove(date);
	}
	
	public Map<LocalDate, String> getSelects(LocalDate date){
		//System.out.println(this.selects);
		Map<LocalDate, String> ss = new TreeMap<LocalDate, String>();
		for(Map.Entry<LocalDate, String> entry : this.selects.entrySet()) {
			if(entry.getKey().isBefore(date) || entry.getKey().isEqual(date)) {
				ss.put(entry.getKey(), entry.getValue());
			}
		}
		
		return ss;
	}
	
	public Map<LocalDate, String> getReselect(){
		this.selects = new TreeMap<LocalDate, String>();
		LocalDate date;
		String itemID;
		Map<LocalDate,List<String>> breakers = turtleSimulationRepository.getBreakers("manual");
		for(Map.Entry<LocalDate, List<String>> entry : breakers.entrySet()) {
			date = entry.getKey();
			itemID = entry.getValue().get(0);
			this.selects.put(date, itemID);
		}
		//System.out.println(this.selects);
		return this.selects;
	}
	
	public void simulate(String simulateType) {
		long beginTime=System.currentTimeMillis(); 
		
		Account account = new Account(initCash);
		StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
		StringBuffer breakers_sb = new StringBuffer();
		Map<String,Muster> musters;
		Muster muster;
		String id;

		Integer sseiFlag,  sseiTrend;
		
		Map<LocalDate, AmountEntity> amounts = turtleSimulationRepository.getAmounts("avb");
		List<LocalDate> dates = new ArrayList<LocalDate>(amounts.keySet());

		int i=1;
		for(LocalDate date : dates) {
			Progress.show(dates.size(), i++, date.toString());

			musters = kdataService.getMusters(date);

			sseiFlag = kdataService.getSseiFlag(date);
			sseiTrend = kdataService.getSseiTrend(date, 21);

			if(musters!=null && musters.size()>0) {
				account.setLatestDate(date);
				//更新价格
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
					if(muster!=null && !muster.isDownLimited() && !muster.isUpLimited()) {
						//System.out.println(simulateType);
/*						if("newb".equals(simulateType)) {
							//有赚就卖
							if(account.isGain(itemID,0)) {
								account.dropWithTax(itemID, "2", muster.getLatestPrice());
							}
						}*/
							
						//大盘下降通道走坏,所持股跟随下跌
						//logger.info(String.format("sseiFlag=%d, sseiRatio", args));
						if(sseiFlag==0 && sseiTrend<0 && muster.getClose().compareTo(muster.getLatestPrice())>0) {
							account.dropWithTax(itemID, "4", muster.getLatestPrice());
						}
						
						//高位回落超过8%
						account.dropFallOrder(itemID, -8, "3");

						//跌破21日均线就卖
						if(muster.isDropAve(21)) { 		
							account.dropWithTax(itemID, "1", muster.getLatestPrice());
						}
						
					}
				}
				
				id = selects.get(date);
				if(id!=null && musters.containsKey(id)) {
					//买入
					breakers_sb.append(date + "," + id + "\n");
					Set<Muster> dds = new HashSet<Muster>();
					muster = musters.get(id);
					dds.add(muster);
					
					//固定值买入
					//account.openAllWithFixAmount(dds);
					
					//先卖后买，完成市值平均
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
					account.openAll(dds);			//后买
				}
			}
			dailyAmount_sb.append(account.getDailyAmount() + "\n");
		}
		
		turtleSimulationRepository.save("manual", breakers_sb.toString(), account.getCSV(), dailyAmount_sb.toString());
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("manual simulate 用时：" + used + "秒");          
	}
}
