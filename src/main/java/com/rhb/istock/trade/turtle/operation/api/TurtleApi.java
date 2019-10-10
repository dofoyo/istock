package com.rhb.istock.trade.turtle.operation.api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.istock.comm.api.ResponseContent;
import com.rhb.istock.comm.api.ResponseEnum;
import com.rhb.istock.trade.turtle.operation.TurtleOperationService;

@RestController
public class TurtleApi{
	@Autowired
	@Qualifier("turtleOperationServiceImp")
	TurtleOperationService ts;

	@GetMapping("/turtle/topics")
	public ResponseContent<String[]> getTopics() {
		String[] topis = ts.getTopics();
		return new ResponseContent<String[]>(ResponseEnum.SUCCESS, topis);
	}

	@GetMapping("/turtle/powers")
	public ResponseContent<List<TurtleView>> getPowers() {
		List<TurtleView> views = ts.getPowers();
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/potentials/{type}/{date}")
	public ResponseContent<List<PotentialView>> getPotentials(
			@PathVariable(value="type") String type,
			@PathVariable(value="date") String date
			) {

		LocalDate theDate = null;
		try{
			theDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			System.out.println("the date '" + date + "' is ERROR!");
		}
		
		//System.out.println("the date is " + theDate);
		
		List<PotentialView> views = ts.getPotentials(type, theDate);
		
		return new ResponseContent<List<PotentialView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/potentials/hlb")
	public ResponseContent<List<TurtleView>> getPotentialsOfHlb() {
		List<TurtleView> views = ts.getPotentials("hlb");
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, views);
	}

	@GetMapping("/turtle/potentials/avb")
	public ResponseContent<List<TurtleView>> getPotentialsOfAvb() {
		List<TurtleView> views = ts.getPotentials("avb");
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/potentials/redo")
	public ResponseContent<String> redoPotentials() {
		ts.redoPotentials();
		return new ResponseContent<String>(ResponseEnum.SUCCESS, "");
	}
	
	@GetMapping("/turtle/favors")
	public ResponseContent<List<TurtleView>> getFavors() {
		List<TurtleView> favors = ts.getFavors();
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, favors);
	}
	
	@GetMapping("/turtle/holds")
	public ResponseContent<List<HoldView>> getHolds() {
		List<HoldView> holds = ts.getHolds();
		Collections.sort(holds, new Comparator<HoldView>() {
			@Override
			public int compare(HoldView o1, HoldView o2) {
				return (o2.getStatus()).compareTo(o1.getStatus());
			}
		});	
		return new ResponseContent<List<HoldView>>(ResponseEnum.SUCCESS, holds);
	}
	
	class TopicView{
		private String text;
		private String value;
		
		
	}

}
