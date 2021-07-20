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
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.evaluation.EvaluationRepository;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.kdata.api.KdatasView;
import com.rhb.istock.producer.Producer;
import com.rhb.istock.selector.drum.DrumService;
import com.rhb.istock.selector.fina.FinaService;
import com.rhb.istock.selector.newb.NewbService;
import com.rhb.istock.simulation.Brief;
import com.rhb.istock.simulation.SimulationService;
import com.rhb.istock.trade.turtle.simulation.six.TurtleMusterSimulation;
import com.rhb.istock.trade.turtle.simulation.six.TurtleMusterSimulation_avb_plus;
import com.rhb.istock.trade.turtle.simulation.six.TurtleMusterSimulation_hua;
import com.rhb.istock.trade.turtle.simulation.six.repository.AmountEntity;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@RestController
public class ManualApi {
	@Autowired
	@Qualifier("simulationService")
	SimulationService simulationService;
	
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
	
	@Autowired
	@Qualifier("newbRecoH21")
	Producer newbRecoH21;

	@Autowired
	@Qualifier("newbPlusL21")
	Producer newbPlusL21;

	@Autowired
	@Qualifier("drumRecoH21")
	Producer drumRecoH21;
	
	@Autowired
	@Qualifier("drumPlusL21")
	Producer drumPlusL21;

	@Autowired
	@Qualifier("drumFavor")
	Producer drumFavor;
	
	@Autowired
	@Qualifier("newbFavor")
	Producer newbFavor;

	@Autowired
	@Qualifier("newbRup")
	Producer newbRup;

	@Autowired
	@Qualifier("newbRupStart")
	Producer newbRupStart;
	
	@Autowired
	@Qualifier("horizon")
	Producer horizon;
	
	@Autowired
	@Qualifier("eva")
	Producer eva;
	
	@Autowired
	@Qualifier("sab21Favor")
	Producer sab21Favor;

	@Autowired
	@Qualifier("power")
	Producer power;

	@Autowired
	@Qualifier("evaluationRepository")
	EvaluationRepository evaluationRepository;
	
	private List<String> previous = null;
	
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
	
	
	
	@GetMapping("/turtle/manual/run/{simulateType}/{date}")
	public ResponseContent<String> simulate(
			@PathVariable(value="simulateType") String simulateType,
			@PathVariable(value="date") String date
			){
		
		LocalDate theEndDate = null;
		try{
			theEndDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			return new ResponseContent<String>(ResponseEnum.SUCCESS, "");
		}
		
		turtleSimulationRepository.evictAmountsCache();
		turtleSimulationRepository.evictBreakersCache();
		turtleSimulationRepository.evictBuysCache();
		turtleSimulationRepository.evictSellsCache();
		turtleSimulationRepository.evictHoldsCache();

		try {
			manualService.simulate(simulateType, theEndDate);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		kdataService.evictKDataCache();
		turtleSimulationRepository.evictAmountsCache();
		turtleSimulationRepository.evictBreakersCache();
		turtleSimulationRepository.evictBuysCache();
		turtleSimulationRepository.evictSellsCache();
		turtleSimulationRepository.evictHoldsCache();

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
	
	@GetMapping("/turtle/manual/buylist/{type}/{date}")
	public ResponseContent<List<ItemView>> getBuyList(
			@PathVariable(value="type") String type,
			@PathVariable(value="date") String date
			) {
		List<ItemView> views = new ArrayList<ItemView>();
		LocalDate theDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		List<String> ids = this.getBuyList(type, theDate);
		if(ids!=null && !ids.isEmpty()) {
			Item item;
			Integer recommendationCount;
			ItemView iv;
			Set<String> holds = this.getHoldIDs(this.generateHolds("manual",theDate));
			for(String id : ids) {
				item = itemService.getItem(id);
				recommendationCount = finaService.getRecommendationCount(id, theDate);
				item.setRecommendations(recommendationCount);
				//if(recommendationCount!=null && recommendationCount>0) {
					iv = new ItemView(id,item.getNameWithCAGR(),holds.contains(id)? "danger" : "info");
					if(previous!=null && !previous.contains(id) && !holds.contains(id)) {
						iv.setType("warning");
					}
					views.add(iv);
				//}
			}
		}
		
		previous = ids;

		return new ResponseContent<List<ItemView>>(ResponseEnum.SUCCESS, views);
	}
	
	private List<String> getBuyList(String type, LocalDate theDate){
		List<String> ids = new ArrayList<String>();
		
		String theType = "avb";
		if(type.equals("buy2")) {
			theType = "bav";
		}else if(type.equals("buy3")) {
			theType = "bdt";
		}else if(type.equals("buy4")) {
			theType = "bhl";
		}

		//System.out.println(theType + "," + theDate.toString());

		Map<LocalDate, List<String>> breakers = evaluationRepository.getBreakers(theType);
		if(breakers!=null && breakers.get(theDate)!=null) {
			ids.addAll(breakers.get(theDate));
		}
		
		return ids;
	}

	@GetMapping("/turtle/manual/potentials/{type}/{date}")
	public ResponseContent<List<ItemView>> getPotentials(
			@PathVariable(value="type") String type,
			@PathVariable(value="date") String date
			) {
		
		//System.out.println(type);
		//System.out.println(date);
		List<ItemView> views = new ArrayList<ItemView>();
		
		LocalDate theDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		List<String> ids = this.getPotentials(type, theDate);
		if(ids!=null && !ids.isEmpty()) {
			Item item;
			Integer recommendationCount;
			ItemView iv;
			Set<String> holds = this.getHoldIDs(this.generateHolds("manual",theDate));
			for(String id : ids) {
				item = itemService.getItem(id);
				recommendationCount = finaService.getRecommendationCount(id, theDate);
				item.setRecommendations(recommendationCount);
				//if(recommendationCount!=null && recommendationCount>0) {
					iv = new ItemView(id,item.getNameWithCAGR(),holds.contains(id)? "danger" : "info");
					if(previous!=null && !previous.contains(id) && !holds.contains(id)) {
						iv.setType("warning");
					}
					views.add(iv);
				//}
			}
		}
		
		previous = ids;
		
		return new ResponseContent<List<ItemView>>(ResponseEnum.SUCCESS, views);
	}
	
	private List<String> getPotentials(String type, LocalDate theDate){
		List<String> ids = new ArrayList<String>();
		if("high".equals(type)) { 
			ids = newbRecoH21.getResults(theDate);
		}else if("newbRup".equals(type)) { 
			ids = newbRup.getResults(theDate);
		}else if("newbRupStart".equals(type)) { 
			ids = newbRupStart.getResults(theDate);
		}else if("eva".equals(type)) { 
			ids = horizon.getResults(theDate);
		}else if("low".equals(type)) { 
			List<String> tmp = drumPlusL21.getResults(theDate);
			if(tmp!=null && tmp.size()>0) {
				ids.addAll(tmp);
			}
			tmp = newbPlusL21.getResults(theDate);
			if(tmp!=null && tmp.size()>0) {
				for(String id : tmp) {
					if(!ids.contains(id)) {
						ids.add(id);
					}
				}
			}
		}else if("newbFavor".equals(type)) { 
			ids = newbFavor.getResults(theDate);
		}else if("sabFavor".equals(type)) { 
			ids = sab21Favor.getResults(theDate);
		}else if("power".equals(type)) {
			List<String> tmp = power.getResults(theDate);
			Map<String, Muster> musters = kdataService.getMusters(theDate);
			Map<String, Item> items = itemService.getItems();
			Muster muster;
			Item item;
			Integer recommendationCount;
			for(String id : tmp) {
				muster = musters.get(id);
				item = items.get(id);
				recommendationCount = finaService.getRecommendationCount(id, theDate);
				if(muster!=null && muster.getN21Gap()<=8 && muster.getN21Gap()>=0
						&& muster.isUp(8)
						&& muster.isRed()
						&& item!=null && item.getCagr()!=null && item.getCagr()>0 && recommendationCount!=null && recommendationCount>0
						) {
					ids.add(id);
				}
			}
		}
		
		return ids;
	}
	
	private Set<String> getHoldIDs(Set<Brief> holds){
		Set<String> ids = new HashSet<String>();
		for(Brief h : holds) {
			ids.add(h.getItemID());
		}
		return ids;
	}
//---- buys	
	@GetMapping("/turtle/manual/buys/add/{date}/{itemID}")
	public ResponseContent<List<ItemView>> addBuys(
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
		
		manualService.addBuys(theDate, itemID);		
		
		return new ResponseContent<List<ItemView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/manual/buys/delete/{date}/{itemID}")
	public ResponseContent<List<ItemView>> deleteBuys(
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
		
		manualService.deleteBuys(theDate,itemID);

		return new ResponseContent<List<ItemView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/manual/buys/{date}")
	public ResponseContent<List<SelectView>> getBuys(
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
		
		Map<LocalDate, List<String>> selects = manualService.getBuys(theDate);
		
		//System.out.println(selects);
		if(selects != null) {
			List<String> ids;
			for(Map.Entry<LocalDate, List<String>> entry : selects.entrySet()) {
				ids = entry.getValue();
				for(String id : ids) {
					views.add(new SelectView(id,itemService.getItem(id).getName(),entry.getKey(),theDate.equals(entry.getKey())?"danger":"info"));
				}
			}			
		}
		return new ResponseContent<List<SelectView>>(ResponseEnum.SUCCESS, views);
	}
//----------- sells
	@GetMapping("/turtle/manual/sells/add/{date}/{itemID}")
	public ResponseContent<List<ItemView>> addSells(
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
		
		manualService.addSells(theDate, itemID);		
		
		return new ResponseContent<List<ItemView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/manual/sells/delete/{date}/{itemID}")
	public ResponseContent<List<ItemView>> deleteSells(
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
		
		manualService.deleteSells(theDate,itemID);

		return new ResponseContent<List<ItemView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/manual/sells/{date}")
	public ResponseContent<List<SelectView>> getSells(
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
		
		Map<LocalDate, List<String>> selects = manualService.getSells(theDate);
		
		//System.out.println(selects);
		if(selects != null) {
			List<String> ids;
			for(Map.Entry<LocalDate, List<String>> entry : selects.entrySet()) {
				ids = entry.getValue();
				for(String id : ids) {
					views.add(new SelectView(id,itemService.getItem(id).getName(),entry.getKey(),theDate.equals(entry.getKey())?"danger":"info"));
				}
			}			
		}
		return new ResponseContent<List<SelectView>>(ResponseEnum.SUCCESS, views);
	}
	//----------------------
	@GetMapping("/turtle/manual/selects/add/{date}/{itemID}")
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
	
	@GetMapping("/turtle/manual/selects/delete/{date}/{itemID}")
	public ResponseContent<List<ItemView>> deleteSelects(
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
		
		manualService.deleteSelects(theDate,itemID);

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
		
		Map<LocalDate, List<String>> selects = manualService.getSelects(theDate);
		
		//System.out.println(selects);
		if(selects != null) {
			Set<String> holds = this.getHoldIDs(this.generateHolds("manual",theDate));
			List<String> ids;
			Set<String> ids_tmp = new HashSet<String>();
			for(Map.Entry<LocalDate, List<String>> entry : selects.entrySet()) {
				ids = entry.getValue();
				for(String id : ids) {
					if(!ids_tmp.contains(id)) {
						views.add(new SelectView(id,itemService.getItem(id).getName(),entry.getKey(),holds.contains(id)?"danger":""));
						ids_tmp.add(id);
					}
				}
			}			
		}
		return new ResponseContent<List<SelectView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/manual/reselect/{type}")
	public ResponseContent<List<SelectView>> getReselect(
			@PathVariable(value="type") String type
			) {
		List<SelectView> views = new ArrayList<SelectView>();
		
		Map<LocalDate, List<String>> selects = new TreeMap<LocalDate, List<String>>();
		Map<LocalDate, AmountEntity> amounts = turtleSimulationRepository.getAmounts("bav");
		int i=1;
		for(LocalDate date : amounts.keySet()) {
			Progress.show(amounts.size(), i++, date.toString() + "," + type);
			selects.put(date, this.getPotentials(type, date));
		}

		manualService.saveReselect(selects);
		
		return new ResponseContent<List<SelectView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/manual/buylist/reselect/{type}")
	public ResponseContent<List<SelectView>> getReselect1(
			@PathVariable(value="type") String type
			) {
		List<SelectView> views = new ArrayList<SelectView>();
		
		Map<LocalDate, List<String>> selects = new TreeMap<LocalDate, List<String>>();
		Map<LocalDate, AmountEntity> amounts = turtleSimulationRepository.getAmounts("bav");
		int i=1;
		for(LocalDate date : amounts.keySet()) {
			Progress.show(amounts.size(), i++, date.toString() + "," + type);
			selects.put(date, this.getBuyList(type, date));
		}

		manualService.saveReselect(selects);
		
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
		List<String> breaker_ids = breakers.get(theDate);
		Item item;
		if(breaker_ids!=null && !breaker_ids.isEmpty()) {
			Set<String> holds = this.getHoldIDs(this.generateHolds("manual",theDate));
			for(String id : breaker_ids) {
				item = itemService.getItem(id);
				views.add(new ItemView(id,item.getName(),holds.contains(id)? "danger" : "warning"));
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

		
		//Set<Hold> holds = this.generateHolds(type,theDate);
		Set<Brief> holds = simulationService.getHolds(type, theDate, false);
		if(holds!=null && !holds.isEmpty()) {
			for(Brief hold : holds) {
				views.add(new HoldView(hold.getItemID(),hold.getItemName(),hold.getProfit().intValue(),hold.getDate()));
			}
		}
		
		Collections.sort(views, new Comparator<HoldView>() {
			@Override
			public int compare(HoldView o1, HoldView o2) {
				if(o1.getDate().compareTo(o2.getDate()) == 0) {
					return o2.getProfit().compareTo(o1.getProfit());
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
	
	private Set<Brief> generateHolds(String type, LocalDate date){
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
		private Map<String, Brief> hs = new HashMap<String,Brief>();
		Brief hold;
		public void buy(String str) {
			String[] ss = str.split(",");
			hold = hs.get(ss[0]);
			if(hold == null) {
				hs.put(ss[0], new Brief(str));
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
		
		public Set<Brief> getResult(){
			Set<Brief> ids = new HashSet<Brief>();
			for(Map.Entry<String, Brief> entry : hs.entrySet()) {
				if(entry.getValue().isHold()) {
					ids.add(entry.getValue());
				}
			}
			return ids;
		}		
	}
}
