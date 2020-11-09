package com.rhb.istock.trade.turtle.simulation.hua;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.selector.SelectorService;

@Service("huaSimulation")
public class HuaSimulation {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("selectorServiceImp")
	SelectorService selectorService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	/*
	 * ratio: mcst与现价的比率，越小，说明股价低于成本价越远
	 * beginDate： 开始日期
	 * endDate： 结束日期
	 * period: 持有天数。即买入信号发出后，经历多少天，统计出最高价和最低价，进而计算出盈亏比
	 */
	public Integer[] simulate(BigDecimal ratio, LocalDate beginDate, LocalDate endDate, Integer period) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("hua simulate begin......");

		Integer[] wins_total = new Integer[] {0,0};
		Integer[] wt;
		List<String> ids = itemService.getItemIDs();
		int i=1;
		for(String id : ids) {
			Progress.show(ids.size(), i++, "  simulate: " + id);
			
			wt = this.simulate(id, ratio, beginDate, endDate, period);
			wins_total[0] = wins_total[0] + wt[0];
			wins_total[1] = wins_total[1] + wt[1];
		}
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
		return wins_total;
	}
	
	public Integer[] simulate(String itemID, BigDecimal ratio, LocalDate beginDate, LocalDate endDate, Integer period) {
		Map<LocalDate, BigDecimal[]> bolls = selectorService.getBOLLs(itemID, true);
		Map<LocalDate, BigDecimal> mcsts = selectorService.getMCSTs(itemID, true);
		Map<LocalDate, BigDecimal[]> macds = selectorService.getMACDs(itemID, true);

		Kdata kdata = kdataService.getKdata(itemID, true);
		Kbar bar;
		BigDecimal mcst, dn, volume_ratio, macd;
		LocalDate theDate = null; 

		Accounts as = new Accounts(period);
		
		List<LocalDate> dates = kdata.getDates();
		for(LocalDate date : dates) {
			if(date.isAfter(beginDate) && date.isBefore(endDate)) {
				bar = kdata.getBar(date);
				
				as.setPrice(bar.getClose());
				
				mcst = mcsts.get(date);
				dn = bolls.get(date)==null ? null : bolls.get(date)[2];
				volume_ratio = bar.getVolume_ratio();
				if(mcst!=null && this.isDown(bar.getClose(), mcst, ratio) 
						&& dn!=null && dn.compareTo(bar.getLow()) == 1
						&& volume_ratio.compareTo(new BigDecimal(2)) == 1
						) {
					theDate = date;
				}
				
				macd = macds.get(date)==null ? null : macds.get(date)[2];
				if(theDate!=null && macd.compareTo(BigDecimal.ZERO)==1) {
					as.buy(date, bar.getClose());
					theDate = null;
				}
			}
		}
		
		kdataService.evictKDataCache();
		
		Map<LocalDate, BigDecimal[]> results = as.getResults();
		BigDecimal win_ratio, lose_ratio;
		Integer wins = 0;
		Integer total = 0;
		for(Map.Entry<LocalDate, BigDecimal[]> entry : results.entrySet()) {
			total ++;
			win_ratio = entry.getValue()[0];
			lose_ratio = entry.getValue()[1];
			if(lose_ratio.compareTo(BigDecimal.ZERO)>=0) wins++; //没跌就算赢
			System.out.printf("\n%tF: %.2f %.2f   %s\n", entry.getKey(), entry.getValue()[0], entry.getValue()[1],lose_ratio.compareTo(BigDecimal.ZERO)>=0?"win":"");
		}
		
		return new Integer[] {wins,total};
	}
	
	private boolean isDown(BigDecimal b1, BigDecimal b2, BigDecimal ratio) {
		BigDecimal r = b1.subtract(b2).divide(b2, BigDecimal.ROUND_HALF_UP);
		return r.compareTo(ratio)==-1 ? true : false;
	}
	
	class Accounts{
		Map<LocalDate,Account> ss = null;
		Integer period = null;
		
		public Accounts(Integer period) {
			this.period = period;
			ss = new TreeMap<LocalDate, Account>();	
		}
		
		public void buy(LocalDate date, BigDecimal price) {
			Account a = new Account(period);
			a.buy(date, price);
			ss.put(date, a);
		}
		
		public void setPrice(BigDecimal price) {
			for(Map.Entry<LocalDate, Account> entry : ss.entrySet()) {
				entry.getValue().setPrice(price);
			}
		}
		
		public Map<LocalDate, BigDecimal[]> getResults(){
			Map<LocalDate, BigDecimal[]> results = new TreeMap<LocalDate, BigDecimal[]>();
			for(Map.Entry<LocalDate, Account> entry : ss.entrySet()) {
				results.put(entry.getKey(), entry.getValue().getResult());
			}
			
			return results;
		}
	}
	
	class Account{
		Integer DAYS = null;
		LocalDate buyDate = null;
		BigDecimal buyPrice = null;
		BigDecimal highest = null;
		BigDecimal lowest = null;
		Integer i = null;
		
		public Account(Integer days) {
			this.DAYS = days;
		}
		
		public void buy(LocalDate date, BigDecimal price) {
			this.buyDate = date;
			this.buyPrice = price;
			this.highest = price;
			this.lowest = price;
			this.i = 0;
		}
		
		public void setPrice(BigDecimal price) {
			if(this.i++ < 5) {
				highest = price.compareTo(this.highest)==1 ? price : highest;
				lowest  = price.compareTo(this.lowest) ==1 ? lowest : price;
			}
		}
		
		public BigDecimal[] getResult() {
			BigDecimal upRatio = highest.subtract(buyPrice).divide(buyPrice, BigDecimal.ROUND_HALF_UP);
			BigDecimal dnRatio = lowest.subtract(buyPrice).divide(buyPrice, BigDecimal.ROUND_HALF_UP);
			return new BigDecimal[] {upRatio, dnRatio};
		}
	
	}
}
