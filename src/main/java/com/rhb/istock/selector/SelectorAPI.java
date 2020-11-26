package com.rhb.istock.selector;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.istock.comm.api.ResponseContent;
import com.rhb.istock.comm.api.ResponseEnum;
import com.rhb.istock.producer.Producer;
import com.rhb.istock.selector.drum.DrumService;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@RestController
public class SelectorAPI {
	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;
	
	@Autowired
	@Qualifier("drumService")
	DrumService drumService;
	
	@Autowired
	@Qualifier("newb")
	Producer newb;
	
	@GetMapping("/selector/dimension/{date}")
	public ResponseContent<List<DimensionView>> getDiemension(
			@PathVariable(value="date") String endDate
			) {
		
		//System.out.println(endDate);
		
		LocalDate theEndDate = null;
		if(endDate!=null && !endDate.isEmpty()) {
			theEndDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}

		
		//Set<String> holds = this.generateHolds("manual",theEndDate);
		Set<String> newbs = new HashSet<String>(newb.getResults(theEndDate));
		Integer ratio = 21;
		List<DimensionView> views = drumService.getDimensionView(theEndDate, newbs, ratio);
		
		return new ResponseContent<List<DimensionView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/selector/dimension/datas/{name}/{date}")
	public ResponseContent<DdatasView> getDdatas(
			@PathVariable(value="name") String name,
			@PathVariable(value="date") String endDate
			) {
		
		//System.out.println(endDate);
		//System.out.println(name);
		
		LocalDate theEndDate = null;
		if(endDate!=null && !endDate.isEmpty()) {
			theEndDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}
		
		DdatasView views = drumService.getDdatasView(name, theEndDate);
		
		return new ResponseContent<DdatasView>(ResponseEnum.SUCCESS, views);
	}
	
	private Set<String> generateHolds(String type, LocalDate date){
		Holds holds = new Holds();
		
		Map<LocalDate,List<String>> buys = turtleSimulationRepository.getBuys(type); // itemID, itemName, profit
		
		Map<LocalDate,List<String>> sells = turtleSimulationRepository.getSells(type);
		
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
