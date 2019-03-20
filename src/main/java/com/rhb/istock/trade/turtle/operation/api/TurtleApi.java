package com.rhb.istock.trade.turtle.operation.api;

import java.math.BigDecimal;
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

	@GetMapping("/turtle/avtops")
	public ResponseContent<List<TurtleView>> getAvTops() {
		List<TurtleView> tops = ts.getAvTops(55);
		Collections.sort(tops, new Comparator<TurtleView>() {
			@Override
			public int compare(TurtleView o1, TurtleView o2) {
				BigDecimal hl1 = new BigDecimal(o1.getHlgap());
				BigDecimal hl2 = new BigDecimal(o2.getHlgap());
				
				if(o1.getStatus().equals(o2.getStatus())) {
					return (hl1).compareTo(hl2);
				}else {
					return (o2.getStatus()).compareTo(o1.getStatus());
				}
			}
		});	
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, tops);
	}
	
	@GetMapping("/turtle/dailytops")
	public ResponseContent<List<TurtleView>> getDailyTops() {
		List<TurtleView> tops = ts.getDailyTops(55);
		Collections.sort(tops, new Comparator<TurtleView>() {
			@Override
			public int compare(TurtleView o1, TurtleView o2) {
				BigDecimal hl1 = new BigDecimal(o1.getHlgap());
				BigDecimal hl2 = new BigDecimal(o2.getHlgap());
				if(o1.getStatus().equals(o2.getStatus())) {
					return (hl1).compareTo(hl2);
				}else {
					return (o2.getStatus()).compareTo(o1.getStatus());
				}
			}
		});	
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, tops);
	}
	
	@GetMapping("/turtle/bluechips")
	public ResponseContent<List<TurtleView>> getBluechips() {
		List<TurtleView> favors = ts.getBluechips();
		Collections.sort(favors, new Comparator<TurtleView>() {
			@Override
			public int compare(TurtleView o1, TurtleView o2) {
				BigDecimal hl1 = new BigDecimal(o1.getHlgap());
				BigDecimal hl2 = new BigDecimal(o2.getHlgap());
				if(o1.getStatus().equals(o2.getStatus())) {
					return (hl1).compareTo(hl2);
				}else {
					return (o2.getStatus()).compareTo(o1.getStatus());
				}
			}
		});	
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, favors);
	}
	
	@GetMapping("/turtle/favors")
	public ResponseContent<List<TurtleView>> getFavors() {
		List<TurtleView> favors = ts.getFavors();
		Collections.sort(favors, new Comparator<TurtleView>() {
			@Override
			public int compare(TurtleView o1, TurtleView o2) {
				BigDecimal hl1 = new BigDecimal(o1.getHlgap());
				BigDecimal hl2 = new BigDecimal(o2.getHlgap());
				if(o1.getStatus().equals(o2.getStatus())) {
					return (hl1).compareTo(hl2);
				}else {
					return (o2.getStatus()).compareTo(o1.getStatus());
				}
			}
		});	
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

	/*
	 * 突破时(status=2)，hlgap最小的50个
	 * 蓄势时(status=1)，nhgap最小的50个
	 */
	@GetMapping("/turtle/preys")
	public ResponseContent<List<TurtleView>> getPreys() {
		//System.out.println("status" + status);
		List<TurtleView> preys = ts.getPreys();
		
		Collections.sort(preys, new Comparator<TurtleView>() {
			@Override
			public int compare(TurtleView o1, TurtleView o2) {
				BigDecimal hl1 = new BigDecimal(o1.getHlgap());
				BigDecimal hl2 = new BigDecimal(o2.getHlgap());
				if(o1.getStatus().equals(o2.getStatus())) {
					return (hl1).compareTo(hl2);
				}else {
					return (o2.getStatus()).compareTo(o1.getStatus());
				}
			}
		});	
		
		int count = Math.min(preys.size(), 50);
		
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, preys.subList(0, count));
	}
	
	@GetMapping("/kdatas/{itemID}")
	public ResponseContent<KdatasView> getKdatas(@PathVariable(value="itemID") String itemID) {
		KdatasView kdatas = ts.getKdatas(itemID);
		return new ResponseContent<KdatasView>(ResponseEnum.SUCCESS, kdatas);
	}
}
