package com.rhb.istock.trade.turtle.simulation.six;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.bluechip.BluechipService;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Service("turtleMusterSimulation_avb_plus")
public class TurtleMusterSimulation_avb_plus {
	protected static final Logger logger = LoggerFactory.getLogger(TurtleMusterSimulation_avb_plus.class);

	@Value("${musterPath}")
	private String musterPath;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("bluechipServiceImp")
	BluechipService bluechipService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	BigDecimal initCash = new BigDecimal(1000000);

	/*
	 * 根据输入起止日期，系统模拟买入和卖出
	 */
	public void simulate(LocalDate beginDate, LocalDate endDate) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("simulate hunt from " + beginDate + " to " + endDate +" ......");

		boolean isEvaluation = false;
		Hunt bdt = new Hunt(initCash, true);

		TreeMap<LocalDate,List<String>> sells = turtleSimulationRepository.getSells("avb");
		TreeMap<LocalDate,List<String>> buys = turtleSimulationRepository.getBuys("avb"); // itemID, itemName, profit
		
		List<LocalDate> dates = kdataService.getMusterDates();
		int fromIndex = 0;
		int toIndex = 0;
		int max_period = 34;

		logger.info(String.format("sells.size=%d, buys.size=%d, dates.size=%d", sells.size(),buys.size(), dates.size()));
		
		Map<String,Muster> musters;

		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, "  simulate hunt: " + date.toString());

			musters = kdataService.getMusters(date);
			
			toIndex = dates.indexOf(date);
			fromIndex = toIndex<max_period ? 0 : toIndex-max_period;
			
			if(musters!=null && musters.size()>0) {
				bdt.doIt(musters, this.getIDs(buys,sells, dates.subList(fromIndex, toIndex)), date);
			}
		}
		
		Map<String, String> dtbResult = bdt.result();
		
		turtleSimulationRepository.save("bdt", dtbResult.get("breakers"), dtbResult.get("CSV"), dtbResult.get("dailyAmount"), isEvaluation);

		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	private Set<String> getIDs(TreeMap<LocalDate,List<String>> buys, TreeMap<LocalDate,List<String>> sells,List<LocalDate> dates){
		Set<String> ids = new HashSet<String>();
		Set<String> holds = this.generateHolds(buys, sells, dates.get(dates.size()-1));
		List<String> ls;
		for(LocalDate d : dates) {
			if(sells.containsKey(d)){
				ls = sells.get(d);
				for(String id : ls) {
					if(!holds.contains(id)) {
						ids.add(id);
					}
				}
			}
		}		
		return ids;
	}
	
	private Set<String> generateHolds(TreeMap<LocalDate,List<String>> buys, TreeMap<LocalDate,List<String>> sells, LocalDate date){
		Holds holds = new Holds();
		
		for(Map.Entry<LocalDate,List<String>> entry : buys.entrySet()) {
			if(entry.getKey().isBefore(date)) {
				for(String str : entry.getValue()) {
					holds.buy(str);
				}
			}
		}
		
		for(Map.Entry<LocalDate,List<String>> entry : sells.entrySet()) {
			if(entry.getKey().isBefore(date)) {
				for(String id : entry.getValue()) {
					holds.sell(id);
				}
			}
		}
		
		return holds.getResult();
	}
	
	class Holds{
		private Map<String, Hold> hs = new HashMap<String,Hold>();
		Hold hold;
		public void buy(String str) {
			String[] ss = str.split(",");
			hold = hs.get(ss[0]);
			if(hold == null) {
				hs.put(ss[0], new Hold(str));
			}else {
				hold.update(str);
			}
		}
		
		public void sell(String id) {
			hold = hs.get(id);
			if(hold != null) {
				hold.deCount();
			}else {
				System.err.println("There is ERROR: no buy, but sell!");
			}
		}
		
		public Set<String> getResult(){
			Set<String> ids = new HashSet<String>();
			for(Map.Entry<String, Hold> entry : hs.entrySet()) {
				if(entry.getValue().isHold()) {
					ids.add(entry.getKey());
				}
			}
			return ids;
		}		
	}
	class Hold{
		private String itemID;
		private String itemName;
		private Integer count;
		private BigDecimal profit;
		private LocalDate date;
		
		public Hold(String str) {
			String[] ss = str.split(",");
			this.itemID = ss[0];
			this.itemName = ss[1];
			this.profit = new BigDecimal(ss[2]);
			this.date = LocalDate.parse(ss[3],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			this.count = 1;
		}
		
		public LocalDate getDate() {
			return date;
		}

		public void setDate(LocalDate date) {
			this.date = date;
		}

		public void update(String str) {
			String[] ss = str.split(",");
			this.profit = this.profit.add(new BigDecimal(ss[2]));
			this.count++;
		}
		
		public boolean isHold() {
			return count==0 ? false : true;
		}
		
		public String getItemID() {
			return itemID;
		}
		public void setItemID(String itemID) {
			this.itemID = itemID;
		}
		public String getItemName() {
			return itemName;
		}
		public void setItemName(String itemName) {
			this.itemName = itemName;
		}
		public Integer getCount() {
			return count;
		}
		public void deCount() {
			this.count--;
		}
		public BigDecimal getProfit() {
			return profit;
		}
		public void setProfit(BigDecimal profit) {
			this.profit = profit;
		}
	}
}