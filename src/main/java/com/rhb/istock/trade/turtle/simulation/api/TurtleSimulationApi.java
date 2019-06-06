package com.rhb.istock.trade.turtle.simulation.api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.istock.comm.api.ResponseContent;
import com.rhb.istock.comm.api.ResponseEnum;
import com.rhb.istock.trade.turtle.simulation.TurtleSimulation;

@RestController
public class TurtleSimulationApi {
	@Autowired
	@Qualifier("turtleStaticSimulation")
	TurtleSimulation turtleSimulation;
	
	@GetMapping("/turtle/simulation/breakers/{type}/{year}/{date}")
	public ResponseContent<List<BreakerView>> getBreakers(
			@PathVariable(value="type") String type,
			@PathVariable(value="year") String year,
			@PathVariable(value="date") String date
			) {
		
		//System.out.println("type=" + type + ", year=" + year + ", date=" + date);
		List<BreakerView> views = turtleSimulation.getBreakers(type,year,LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		return new ResponseContent<List<BreakerView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/simulation/holds/{type}/{year}/{date}")
	public ResponseContent<List<HoldView>> getHolds(
			@PathVariable(value="type") String type,
			@PathVariable(value="year") String year,
			@PathVariable(value="date") String date
			) {
		
		//System.out.println("type=" + type + ", year=" + year + ", date=" + date);
		List<HoldView> views = turtleSimulation.getHolds(type,year,LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		return new ResponseContent<List<HoldView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/simulation/amount/{type}/{year}/{date}")
	public ResponseContent<AmountView> getAmount(
			@PathVariable(value="type") String type,
			@PathVariable(value="year") String year,
			@PathVariable(value="date") String date
			) {
		
		AmountView view = turtleSimulation.getAmount(type,year,LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		return new ResponseContent<AmountView>(ResponseEnum.SUCCESS, view);
	}
	
	@GetMapping("/turtle/simulation/dates/{type}/{year}")
	public ResponseContent<List<String>> getDays(
			@PathVariable(value="type") String type,
			@PathVariable(value="year") String year
			) {
		
		List<String> dates = turtleSimulation.getDates(type,year);
		return new ResponseContent<List<String>>(ResponseEnum.SUCCESS, dates);
	}
	
	@GetMapping("/turtle/simulation/evictCache")
	public ResponseContent<String> evictCache() {
		turtleSimulation.evictCache();
		return new ResponseContent<String>(ResponseEnum.SUCCESS, null);
	}
	
}
