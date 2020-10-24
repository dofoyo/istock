package com.rhb.istock.trade.turtle.manual;

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
import java.util.TreeMap;

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
import com.rhb.istock.selector.drum.DrumService;
import com.rhb.istock.selector.fina.FinaService;
import com.rhb.istock.selector.newb.NewbService;
import com.rhb.istock.trade.turtle.simulation.six.TurtleMusterSimulation;
import com.rhb.istock.trade.turtle.simulation.six.TurtleMusterSimulation_avb_plus;
import com.rhb.istock.trade.turtle.simulation.six.TurtleMusterSimulation_hua;
import com.rhb.istock.trade.turtle.simulation.six.repository.AmountEntity;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@RestController
public class ManualApi {
	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("turtleMusterSimulation")
	TurtleMusterSimulation turtleMusterSimulation;

	@Autowired
	@Qualifier("turtleMusterSimulation_hua")
	TurtleMusterSimulation_hua turtleMusterSimulation_hua;
	
	@Autowired
	@Qualifier("turtleMusterSimulation_avb_plus")
	TurtleMusterSimulation_avb_plus turtleMusterSimulation_avb_plus;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("newbService")
	NewbService newbService;

	@Autowired
	@Qualifier("drumService")
	DrumService drumService;
	
	@Autowired
	@Qualifier("manualService")
	ManualService manualService;

	@Autowired
	@Qualifier("finaService")
	FinaService finaService;
	
	@GetMapping("/turtle/manual/kdatas/{itemID}")
	public ResponseContent<KdatasView> getKdatas(@PathVariable(value="itemID") String itemID,
			@RequestParam(value="endDate") String endDate
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
			
			List<LocalDate> dates = kdataService.getKdata(itemID, theEndDate, true).getDates();
			
			Kbar bar=null;
			for(LocalDate date : dates) {
				bar = kdataService.getKbar(itemID, date, true);
				kdatas.addKdata(date, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose());
			}
			
			//System.out.println(bar);
			
			if(bar==null || !bar.getDate().equals(theEndDate)) {
				Kbar latestBar = kdataService.getLatestMarketData(itemID);
				if(latestBar!=null) {
					kdatas.addKdata(theEndDate, latestBar.getOpen(), latestBar.getHigh(), latestBar.getLow(), latestBar.getClose());
				}
			}
			
			kdatas.addBuys(turtleSimulationRepository.getBuys(itemID,"manual"));
			kdatas.addSells(turtleSimulationRepository.getSells(itemID,"manual"));
		}
		
		return new ResponseContent<KdatasView>(ResponseEnum.SUCCESS, kdatas);
	}
	
	
	
	@GetMapping("/turtle/manual/run/{simulateType}")
	public ResponseContent<String> simulate(@PathVariable(value="simulateType") String simulateType){
		
		turtleSimulationRepository.evictAmountsCache();
		turtleSimulationRepository.evictBreakersCache();
		turtleSimulationRepository.evictBuysCache();
		turtleSimulationRepository.evictSellsCache();

		
		manualService.simulate(simulateType); 
		
		kdataService.evictKDataCache();
		turtleSimulationRepository.evictAmountsCache();
		turtleSimulationRepository.evictBreakersCache();
		turtleSimulationRepository.evictBuysCache();
		turtleSimulationRepository.evictSellsCache();

		return new ResponseContent<String>(ResponseEnum.SUCCESS, "");
		
	}	
	
	@GetMapping("/turtle/manual/allamounts/{date}")
	public ResponseContent<AllAmountView> getAmounts(
			@PathVariable(value="date") String endDate){

		AllAmountView view = new AllAmountView();

		LocalDate theEndDate = null;
		try{
			theEndDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			return new ResponseContent<AllAmountView>(ResponseEnum.ERROR, view);
		}

		Map<LocalDate, AmountEntity> manual = turtleSimulationRepository.getAmounts("manual");

		for(LocalDate theDate : manual.keySet()) {
			if(theDate.isBefore(theEndDate) || theDate.equals(theEndDate)) {
				view.add(theDate, 
						manual.get(theDate).getTotal(),
						manual.get(theDate).getTotal(),
						manual.get(theDate).getTotal(),
						manual.get(theDate).getTotal(), 
						manual.get(theDate).getTotal(), 
						manual.get(theDate).getTotal());
			}
		}
	
		return new ResponseContent<AllAmountView>(ResponseEnum.SUCCESS, view);
	}

	@GetMapping("/turtle/manual/potentials/{type}/{date}")
	public ResponseContent<List<ItemView>> getPotentials(
			@PathVariable(value="type") String type,
			@PathVariable(value="date") String date
			) {

		List<ItemView> views = new ArrayList<ItemView>();
		
		LocalDate theDate = null;
		try{
			theDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			new ResponseContent<List<ItemView>>(ResponseEnum.ERROR, views);
		}

		Set<String> holds = this.getHoldIDs(this.generateHolds("manual",theDate));

		List<String> ids = null;
		if("newb".equals(type)) {
			ids = newbService.getNewbs(theDate);
		}else if("drum".equals(type)) {
			ids = drumService.getDrumsOfTopDimensions(theDate, holds);
		}else if("cagr".equals(type)) {
			ids = drumService.getDrumsOfCAGR(theDate, 100);
		}
		
		if(ids!=null && !ids.isEmpty()) {
			Item item;
			for(String id : ids) {
				item = itemService.getItem(id);
				if(item.getCagr()!=null && item.getCagr()>20) {
					views.add(new ItemView(id,item.getName(),holds.contains(id)? "danger" : "info",item.getCagr()));
				}
			}
		}
		/*
		Collections.sort(views, new Comparator<ItemView>() {

			@Override
			public int compare(ItemView o1, ItemView o2) {
				Integer a = o2.getCagr()==null ? 0 : o2.getCagr();
				Integer b = o1.getCagr()==null ? 0 : o1.getCagr();
				return a.compareTo(b);
			}
			
		});*/
		
		return new ResponseContent<List<ItemView>>(ResponseEnum.SUCCESS, views);
	}
	
	private Set<String> getHoldIDs(Set<Hold> holds){
		Set<String> ids = new HashSet<String>();
		for(Hold h : holds) {
			ids.add(h.getItemID());
		}
		return ids;
	}
	
	@GetMapping("/turtle/manual/selected/{date}/{itemID}")
	public ResponseContent<List<ItemView>> doSelects(
			@PathVariable(value="itemID") String itemID,
			@PathVariable(value="date") String date
			) {

		//System.out.println(date + "--" + itemID);
		List<ItemView> views = new ArrayList<ItemView>();
		
		LocalDate theDate = null;
		try{
			theDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			new ResponseContent<List<ItemView>>(ResponseEnum.ERROR, views);
		}
		
		manualService.addSelects(theDate, itemID);		
		
		return new ResponseContent<List<ItemView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/manual/selected/delete/{date}")
	public ResponseContent<List<ItemView>> deleteSelects(
			@PathVariable(value="date") String date
			) {

		//System.out.println("delete " + date);
		List<ItemView> views = new ArrayList<ItemView>();
		
		LocalDate theDate = null;
		try{
			theDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			new ResponseContent<List<ItemView>>(ResponseEnum.ERROR, views);
		}
		
		manualService.deleteSelects(theDate);

		return new ResponseContent<List<ItemView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/manual/selects/{date}")
	public ResponseContent<List<SelectView>> getSelects(
			@PathVariable(value="date") String date
			) {
		List<SelectView> views = new ArrayList<SelectView>();

		LocalDate theDate = null;
		try{
			theDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			new ResponseContent<List<SelectView>>(ResponseEnum.ERROR, views);
		}
		
		//System.out.println(theDate);
		
		Map<LocalDate, String> selects = manualService.getSelects(theDate);
		//System.out.println(selects);
		if(selects != null) {
			Set<String> holds = this.getHoldIDs(this.generateHolds("manual",theDate));
			for(Map.Entry<LocalDate, String> entry : selects.entrySet()) {
				views.add(new SelectView(entry.getValue(),itemService.getItem(entry.getValue()).getName(),entry.getKey(),holds.contains(entry.getValue())?"danger":""));
			}			
		}
		return new ResponseContent<List<SelectView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/manual/reselect")
	public ResponseContent<List<SelectView>> getReselect() {
		List<SelectView> views = new ArrayList<SelectView>();
		
		Map<LocalDate, String> selects = manualService.getReselect();
		if(selects != null) {
			for(Map.Entry<LocalDate, String> entry : selects.entrySet()) {
				views.add(new SelectView(entry.getValue(),itemService.getItem(entry.getValue()).getName(),entry.getKey(),"warning"));
			}
		}		

		
		return new ResponseContent<List<SelectView>>(ResponseEnum.SUCCESS, views);
	}
	
	
	@GetMapping("/turtle/manual/breakers/{type}/{date}")
	public ResponseContent<List<ItemView>> getBreakers(
			@PathVariable(value="type") String type,
			@PathVariable(value="date") String date
			) {

		//System.out.println("breakers: " + type + " - " + date.toString());
		
		List<ItemView> views = new ArrayList<ItemView>();
		
		LocalDate theDate = null;
		try{
			theDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			new ResponseContent<List<ItemView>>(ResponseEnum.ERROR, views);
		}
		
		Map<LocalDate,List<String>> breakers = turtleSimulationRepository.getBreakers(type);
		
		List<String> ids = breakers.get(theDate);
		Item item;
		if(ids!=null && !ids.isEmpty()) {
			Set<String> holds = this.getHoldIDs(this.generateHolds("manual",theDate));
			for(String id : ids) {
				item = itemService.getItem(id);
				views.add(new ItemView(id,item.getName(),holds.contains(id)? "danger" : "warning",item.getCagr()));
			}
		}
		
		return new ResponseContent<List<ItemView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/manual/holds/{type}/{date}")
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
	
	@GetMapping("/turtle/manual/amount/{type}/{date}")
	public ResponseContent<AmountView> getAmount(
			@PathVariable(value="type") String type,
			@PathVariable(value="date") String date
			) {
		
		BigDecimal initCash = new BigDecimal(1000000);
		
		AmountView view = new AmountView(BigDecimal.ZERO,BigDecimal.ZERO,initCash);

		LocalDate theDate = null;
		try{
			theDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			new ResponseContent<AmountView>(ResponseEnum.ERROR, view);
		}

		
		Map<LocalDate, AmountEntity> amounts = turtleSimulationRepository.getAmounts(type);
		AmountEntity entity = amounts.get(theDate);
		if(entity!=null) {
			view = new AmountView(entity.getCash(), entity.getValue(),initCash);
		}
		
		return new ResponseContent<AmountView>(ResponseEnum.SUCCESS, view);
	}
	
	@GetMapping("/turtle/manual/dates/{type}")
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
