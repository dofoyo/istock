package com.rhb.istock.trade.turtle.simulation.six.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.istock.comm.api.ResponseContent;
import com.rhb.istock.comm.api.ResponseEnum;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.api.KdatasView;
import com.rhb.istock.trade.turtle.simulation.six.TurtleMusterSimulation;
import com.rhb.istock.trade.turtle.simulation.six.TurtleMusterSimulation_avb_plus;
import com.rhb.istock.trade.turtle.simulation.six.TurtleMusterSimulation_hunt;
import com.rhb.istock.trade.turtle.simulation.six.repository.AmountEntity;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@RestController
public class TurtleSimulationApi {
	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("turtleMusterSimulation")
	TurtleMusterSimulation turtleMusterSimulation;

	@Autowired
	@Qualifier("turtleMusterSimulation_hunt")
	TurtleMusterSimulation_hunt turtleMusterSimulation_hunt;
	
	@Autowired
	@Qualifier("turtleMusterSimulation_avb_plus")
	TurtleMusterSimulation_avb_plus turtleMusterSimulation_avb_plus;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@GetMapping("/turtle/simulation/kdatas/{itemID}")
	public ResponseContent<KdatasView> getKdatas(@PathVariable(value="itemID") String itemID,
			@RequestParam(value="endDate") String endDate,
			@RequestParam(value="theType") String type
			) {

		//System.out.println(endDate);
		//System.out.println(type);
		
		BSKdatasView kdatas = new BSKdatasView();

		LocalDate theEndDate = null;
		try{
			theEndDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			return new ResponseContent<KdatasView>(ResponseEnum.ERROR, kdatas);
		}
		
		if(theEndDate != null) {
			Item item = itemService.getItem(itemID);
			kdatas.setItemID(itemID);
			kdatas.setCode(item.getCode());
			kdatas.setName(item.getName());
			
			LocalDate latestDate = kdataService.getLatestMarketDate("sh000001");
			List<LocalDate> dates = kdataService.getKdata(itemID, theEndDate, true).getDates();
			Kbar bar;
			for(LocalDate date : dates) {
				bar = kdataService.getKbar(itemID, date, true);
				kdatas.addKdata(date, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose());
			}
			
			if(theEndDate.equals(latestDate)) {
				bar = kdataService.getLatestMarketData(itemID);
				kdatas.addKdata(theEndDate, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose());
			}			
			
			kdatas.addBuys(turtleSimulationRepository.getBuys(itemID,type));
			kdatas.addSells(turtleSimulationRepository.getSells(itemID,type));
		}
		
		return new ResponseContent<KdatasView>(ResponseEnum.SUCCESS, kdatas);
	}
	
	
	
	@GetMapping("/turtle/simulation/run/{bdate}/{edate}")
	public ResponseContent<String> simulate(
			@PathVariable(value="bdate") String bdate,
			@PathVariable(value="edate") String edate){
		
		LocalDate theBeginDate = null;
		LocalDate theEndDate = null;
		try{
			theBeginDate = LocalDate.parse(bdate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			theEndDate = LocalDate.parse(edate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			return new ResponseContent<String>(ResponseEnum.ERROR, " NO date!");
		}

		turtleSimulationRepository.evictAmountsCache();
		turtleSimulationRepository.evictBreakersCache();
		turtleSimulationRepository.evictBuysCache();
		turtleSimulationRepository.evictSellsCache();

		
		turtleMusterSimulation.simulate(theBeginDate, theEndDate); 
		turtleMusterSimulation_hunt.simulate(theBeginDate, theEndDate);
		turtleMusterSimulation_avb_plus.simulate(theBeginDate, theEndDate);
		
		turtleSimulationRepository.evictAmountsCache();
		turtleSimulationRepository.evictBreakersCache();
		turtleSimulationRepository.evictBuysCache();
		turtleSimulationRepository.evictSellsCache();

		return new ResponseContent<String>(ResponseEnum.SUCCESS, "");
		
	}	
	
	@GetMapping("/turtle/simulation/means/{date}")
	public ResponseContent<MeanView> getDailyMeans(
			@PathVariable(value="date") String endDate){

		MeanView view = new MeanView();

		LocalDate theEndDate = null;
		try{
			theEndDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			return new ResponseContent<MeanView>(ResponseEnum.ERROR, view);
		}
		
		LocalDate theDate;
		Map<String,Map<String,String>> results = turtleSimulationRepository.getDailyMeans();
		for(Map.Entry<String, Map<String,String>> entry : results.entrySet()) {
			theDate = LocalDate.parse(entry.getKey());
			if(theDate.isBefore(theEndDate) || theDate.equals(theEndDate)) {
				view.add(entry.getKey(), 
						entry.getValue().get("bhl"), 
						entry.getValue().get("bav"), 
						entry.getValue().get("bdt"), 
						entry.getValue().get("hlb"), 
						entry.getValue().get("avb"), 
						entry.getValue().get("dtb"));
			}
		}
	
		return new ResponseContent<MeanView>(ResponseEnum.SUCCESS, view);
	}

	
	@GetMapping("/turtle/simulation/allamounts/{date}")
	public ResponseContent<AllAmountView> getAmounts(
			@PathVariable(value="date") String endDate){

		AllAmountView view = new AllAmountView();

		LocalDate theEndDate = null;
		try{
			theEndDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			return new ResponseContent<AllAmountView>(ResponseEnum.ERROR, view);
		}

		Map<LocalDate, AmountEntity> bhl = turtleSimulationRepository.getAmounts("bhl");
		Map<LocalDate, AmountEntity> bav = turtleSimulationRepository.getAmounts("bav");
		Map<LocalDate, AmountEntity> bdt = turtleSimulationRepository.getAmounts("bdt");
		Map<LocalDate, AmountEntity> hlb = turtleSimulationRepository.getAmounts("hlb");
		Map<LocalDate, AmountEntity> avb = turtleSimulationRepository.getAmounts("avb");
		Map<LocalDate, AmountEntity> dtb = turtleSimulationRepository.getAmounts("dtb");

		for(LocalDate theDate : hlb.keySet()) {
			if(theDate.isBefore(theEndDate) || theDate.equals(theEndDate)) {
				view.add(theDate, 
						bhl.get(theDate).getTotal(),
						bav.get(theDate).getTotal(),
						bdt.get(theDate).getTotal(),
						hlb.get(theDate).getTotal(), 
						avb.get(theDate).getTotal(), 
						dtb.get(theDate).getTotal());
			}
		}
	
		return new ResponseContent<AllAmountView>(ResponseEnum.SUCCESS, view);
	}
	
	@GetMapping("/turtle/simulation/breakers/{type}/{date}")
	public ResponseContent<List<BreakerView>> getBreakers(
			@PathVariable(value="type") String type,
			@PathVariable(value="date") String date
			) {

		List<BreakerView> views = new ArrayList<BreakerView>();
		
		LocalDate theDate = null;
		try{
			theDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			new ResponseContent<List<BreakerView>>(ResponseEnum.ERROR, views);
		}
		
		
		Map<LocalDate,List<String>> breakers = turtleSimulationRepository.getBreakers(type);
		List<String> ids = breakers.get(theDate);
		if(ids!=null && !ids.isEmpty()) {
			for(String id : ids) {
				views.add(new BreakerView(id,itemService.getItem(id).getName()));
			}
		}
		
		return new ResponseContent<List<BreakerView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/simulation/holds/{type}/{date}")
	public ResponseContent<List<HoldView>> getHolds(
			@PathVariable(value="type") String type,
			@PathVariable(value="date") String date
			) {
		
		List<HoldView> views = new ArrayList<HoldView>();
		LocalDate theDate = null;
		try{
			theDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			new ResponseContent<List<HoldView>>(ResponseEnum.ERROR, views);
		}

		
		Set<Hold> holds = this.generateHolds(type,theDate);
		if(holds!=null && !holds.isEmpty()) {
			for(Hold hold : holds) {
				views.add(new HoldView(hold.getItemID(),hold.getItemName(),hold.getProfit().intValue(),hold.getDate()));
			}
		}
		
		Collections.sort(views, new Comparator<HoldView>() {
			@Override
			public int compare(HoldView o1, HoldView o2) {
				if(o1.getDate().compareTo(o2.getDate()) == 0) {
					return o2.getStatus().compareTo(o1.getStatus());
				}else {
					return o1.getDate().compareTo(o2.getDate());
				}
			}
		});
		
		return new ResponseContent<List<HoldView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/simulation/amount/{type}/{date}")
	public ResponseContent<AmountView> getAmount(
			@PathVariable(value="type") String type,
			@PathVariable(value="date") String date
			) {
		
		AmountView view = new AmountView(0,0);

		LocalDate theDate = null;
		try{
			theDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			new ResponseContent<AmountView>(ResponseEnum.ERROR, view);
		}

		
		Map<LocalDate, AmountEntity> amounts = turtleSimulationRepository.getAmounts(type);
		AmountEntity entity = amounts.get(theDate);
		if(entity!=null) {
			view = new AmountView(entity.getCash(), entity.getValue());
		}
		
		return new ResponseContent<AmountView>(ResponseEnum.SUCCESS, view);
	}
	
	@GetMapping("/turtle/simulation/dates/{type}")
	public ResponseContent<List<String>> getDays(
			@PathVariable(value="type") String type
			) {
		
		List<String> dates = new ArrayList<String>();
		
		Map<LocalDate, AmountEntity> amounts = turtleSimulationRepository.getAmounts(type);
		for(LocalDate date : amounts.keySet()) {
			dates.add(date.toString());
		}
		Collections.sort(dates, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return o2.compareTo(o1);
			}
			
		});
		
		
		return new ResponseContent<List<String>>(ResponseEnum.SUCCESS, dates);
	}
	
	private Set<Hold> generateHolds(String type, LocalDate date){
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
		
		public Set<Hold> getResult(){
			Set<Hold> ids = new HashSet<Hold>();
			for(Map.Entry<String, Hold> entry : hs.entrySet()) {
				if(entry.getValue().isHold()) {
					ids.add(entry.getValue());
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
