package com.rhb.istock.trade.turtle.simulation.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.rhb.istock.trade.turtle.simulation.muster.TurtleMusterSimulation;
import com.rhb.istock.trade.turtle.simulation.repository.AmountEntity;
import com.rhb.istock.trade.turtle.simulation.repository.TurtleSimulationRepository;

@RestController
public class TurtleSimulationApi {
	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("turtleMusterSimulation")
	TurtleMusterSimulation turtleMusterSimulation;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@GetMapping("/turtle/simulation/kdatas/{itemID}")
	public ResponseContent<KdatasView> getKdatas(@PathVariable(value="itemID") String itemID,
			@RequestParam(value="endDate") String endDate
			) {

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
			
			LocalDate latestDate = kdataService.getLatestMarketDate();
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
			
			kdatas.addBuys(turtleSimulationRepository.getAllBuys(itemID));
			kdatas.addSells(turtleSimulationRepository.getAllSells(itemID));
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
		
		turtleMusterSimulation.simulate(theBeginDate, theEndDate);
		turtleMusterSimulation.generateDailyRatios(theBeginDate, theEndDate);

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

		BigDecimal min = null;
		
		Map<LocalDate, AmountEntity> bhl = turtleSimulationRepository.getAmounts("bhl");
		Map<LocalDate, AmountEntity> bav = turtleSimulationRepository.getAmounts("bav");
		Map<LocalDate, AmountEntity> bdt = turtleSimulationRepository.getAmounts("bdt");
		Map<LocalDate, AmountEntity> hlb = turtleSimulationRepository.getAmounts("hlb");
		Map<LocalDate, AmountEntity> avb = turtleSimulationRepository.getAmounts("avb");
		Map<LocalDate, AmountEntity> dtb = turtleSimulationRepository.getAmounts("dtb");

		for(LocalDate theDate : bhl.keySet()) {
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

		try{
			LocalDate theDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			new ResponseContent<List<BreakerView>>(ResponseEnum.ERROR, views);
		}
		
		
		Map<LocalDate,List<String>> breakers = turtleSimulationRepository.getBreakers(type);
		List<String> ids = breakers.get(LocalDate.parse(date));
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

		
		Map<LocalDate,List<String>> buys = turtleSimulationRepository.getBuys(type);
		Map<LocalDate,List<String>> sells = turtleSimulationRepository.getSells(type);
		
		List<String> ids = this.generateHolds(type,theDate);
		//System.out.println("holds: " + ids);
		if(ids!=null && !ids.isEmpty()) {
			for(String id : ids) {
				views.add(new HoldView(id,itemService.getItem(id).getName(),0));
			}
		}
		
		ids = buys.get(theDate);
		//System.out.println("buys: " + ids);
		if(ids!=null && !ids.isEmpty()) {
			for(String id : ids) {
				views.add(new HoldView(id,itemService.getItem(id).getName(),1));
			}
		}
		
		ids = sells.get(theDate);
		if(ids!=null && !ids.isEmpty()) {
			for(String id : ids) {
				for(HoldView view : views) {
					if(view.getItemID().equals(id)) {
						view.setStatus(-1);
					}
				}
			}
		}	
		
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
		return new ResponseContent<List<String>>(ResponseEnum.SUCCESS, dates);
	}
	
	private List<String> generateHolds(String type, LocalDate date){
		List<String> ids = new ArrayList<String>();
		
		Map<LocalDate,List<String>> buys = turtleSimulationRepository.getBuys(type);
		Map<LocalDate,List<String>> sells = turtleSimulationRepository.getSells(type);
		
		for(Map.Entry<LocalDate,List<String>> entry : buys.entrySet()) {
			if(entry.getKey().isBefore(date)) {
				ids.addAll(entry.getValue());
			}
		}
		
		for(Map.Entry<LocalDate,List<String>> entry : sells.entrySet()) {
			if(entry.getKey().isBefore(date)) {
				ids.removeAll(entry.getValue());
			}
		}
		
		return ids;
	}
	
}
