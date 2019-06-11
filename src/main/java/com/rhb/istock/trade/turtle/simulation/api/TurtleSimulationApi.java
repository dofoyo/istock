package com.rhb.istock.trade.turtle.simulation.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.istock.comm.api.ResponseContent;
import com.rhb.istock.comm.api.ResponseEnum;
import com.rhb.istock.item.ItemService;
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
	
	@GetMapping("/turtle/simulate/{bdate}/{edate}")
	public ResponseContent<String> simulate(
			@PathVariable(value="bdate") String bdate,
			@PathVariable(value="edate") String edate){
		
		//System.out.println(bdate);
		//System.out.println(edate);
		
		turtleMusterSimulation.simulate(LocalDate.parse(bdate), LocalDate.parse(edate));

		turtleSimulationRepository.evictAmountsCache();
		turtleSimulationRepository.evictBreakersCache();
		turtleSimulationRepository.evictBuysCache();
		turtleSimulationRepository.evictSellsCache();

		
		String msg = "just finished simulate!";
		
		return new ResponseContent<String>(ResponseEnum.SUCCESS, msg);
	}	
	
	@GetMapping("/turtle/simulation/allamounts/{date}")
	public ResponseContent<AllAmountView> getAmounts(
			@PathVariable(value="date") String date){
		BigDecimal min = null;
		AllAmountView view = new AllAmountView();
		Map<LocalDate, AmountEntity> bhl = turtleSimulationRepository.getAmounts("bhl");
		Map<LocalDate, AmountEntity> bav = turtleSimulationRepository.getAmounts("bav");
		Map<LocalDate, AmountEntity> bdt = turtleSimulationRepository.getAmounts("bdt");
		for(LocalDate theDate : bhl.keySet()) {
			if(theDate.isBefore(LocalDate.parse(date)) || theDate.equals(LocalDate.parse(date))) {
				view.add(theDate, bhl.get(theDate).getTotal(), bav.get(theDate).getTotal(), bdt.get(theDate).getTotal());
				if(min==null) {
					min = bhl.get(theDate).getTotal();
				}
				min = min.compareTo(bhl.get(theDate).getTotal())==1 ? bhl.get(theDate).getTotal() : min;
				min = min.compareTo(bav.get(theDate).getTotal())==1 ? bav.get(theDate).getTotal() : min;
				min = min.compareTo(bdt.get(theDate).getTotal())==1 ? bdt.get(theDate).getTotal() : min;
			}
		}
		view.setMin(min);
		
		return new ResponseContent<AllAmountView>(ResponseEnum.SUCCESS, view);
	}

	
	@GetMapping("/turtle/simulation/breakers/{type}/{date}")
	public ResponseContent<List<BreakerView>> getBreakers(
			@PathVariable(value="type") String type,
			@PathVariable(value="date") String date
			) {
		
		List<BreakerView> views = new ArrayList<BreakerView>();
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
		
		Map<LocalDate,List<String>> buys = turtleSimulationRepository.getBuys(type);
		Map<LocalDate,List<String>> sells = turtleSimulationRepository.getSells(type);
		
		List<String> ids = this.generateHolds(type,LocalDate.parse(date));
		//System.out.println("holds: " + ids);
		if(ids!=null && !ids.isEmpty()) {
			for(String id : ids) {
				views.add(new HoldView(id,itemService.getItem(id).getName(),0));
			}
		}
		
		ids = buys.get(LocalDate.parse(date));
		//System.out.println("buys: " + ids);
		if(ids!=null && !ids.isEmpty()) {
			for(String id : ids) {
				views.add(new HoldView(id,itemService.getItem(id).getName(),1));
			}
		}
		
		ids = sells.get(LocalDate.parse(date));
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
		
		Map<LocalDate, AmountEntity> amounts = turtleSimulationRepository.getAmounts(type);
		AmountEntity entity = amounts.get(LocalDate.parse(date));
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
