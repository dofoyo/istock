package com.rhb.istock.trade.turtle.operation.api;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.istock.comm.api.ResponseContent;
import com.rhb.istock.comm.api.ResponseEnum;
import com.rhb.istock.trade.turtle.operation.TurtleOperationService;

@RestController
public class TurtleApi{
	@Autowired
	@Qualifier("turtleOperationServiceImp")
	TurtleOperationService ts;

	@GetMapping("/turtle/potentials")
	public ResponseContent<List<TurtleView>> getPotentials() {
		List<TurtleView> views = ts.getPotentials();
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, views);
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

}
