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
import com.rhb.istock.trade.turtle.operation.service.TurtleOperationService;

@RestController
public class TurtleApi{
	@Autowired
	@Qualifier("turtleOperationServiceImp")
	TurtleOperationService ts;

	@GetMapping("/tops")
	public ResponseContent<List<PreyView>> getTops() {
		List<PreyView> tops = ts.getTops(100);
		Collections.sort(tops, new Comparator<PreyView>() {
			@Override
			public int compare(PreyView o1, PreyView o2) {
				BigDecimal hl1 = new BigDecimal(o1.getHlgap());
				BigDecimal hl2 = new BigDecimal(o2.getHlgap());
				BigDecimal nh1 = new BigDecimal(o1.getNhgap());
				BigDecimal nh2 = new BigDecimal(o2.getNhgap());
				
				if(hl1.equals(hl2)) {
					return (nh2).compareTo(nh1);
				}else {
					return (hl1).compareTo(hl2);
				}
			}
		});	
		return new ResponseContent<List<PreyView>>(ResponseEnum.SUCCESS, tops);
	}
	
	@GetMapping("/favors")
	public ResponseContent<List<PreyView>> getFavors() {
		List<PreyView> favors = ts.getFavors();
		Collections.sort(favors, new Comparator<PreyView>() {
			@Override
			public int compare(PreyView o1, PreyView o2) {
				BigDecimal hl1 = new BigDecimal(o1.getHlgap());
				BigDecimal hl2 = new BigDecimal(o2.getHlgap());
				BigDecimal nh1 = new BigDecimal(o1.getNhgap());
				BigDecimal nh2 = new BigDecimal(o2.getNhgap());
				
				if(hl1.equals(hl2)) {
					return (nh2).compareTo(nh1);
				}else {
					return (hl1).compareTo(hl2);
				}
			}
		});	
		return new ResponseContent<List<PreyView>>(ResponseEnum.SUCCESS, favors);
	}
	
	@GetMapping("/holds")
	public ResponseContent<List<HoldView>> getHolds() {
		List<HoldView> holds = ts.getHolds();
		return new ResponseContent<List<HoldView>>(ResponseEnum.SUCCESS, holds);
	}

	/*
	 * 突破时(status=2)，hlgap最小的50个
	 * 蓄势时(status=1)，nhgap最小的50个
	 */
	@GetMapping("/preys")
	public ResponseContent<List<PreyView>> getPreys() {
		//System.out.println("status" + status);
		List<PreyView> preys = ts.getPreys();
		
		Collections.sort(preys, new Comparator<PreyView>() {
			@Override
			public int compare(PreyView o1, PreyView o2) {
				BigDecimal hl1 = new BigDecimal(o1.getHlgap());
				BigDecimal hl2 = new BigDecimal(o2.getHlgap());
				BigDecimal nh1 = new BigDecimal(o1.getNhgap());
				BigDecimal nh2 = new BigDecimal(o2.getNhgap());
				
				if(hl1.equals(hl2)) {
					return (nh2).compareTo(nh1);
				}else {
					return (hl1).compareTo(hl2);
				}
			}
		});	
		
		int count = Math.min(preys.size(), 50);
		
		return new ResponseContent<List<PreyView>>(ResponseEnum.SUCCESS, preys.subList(0, count));
	}
	
	@GetMapping("/kdatas/{itemID}")
	public ResponseContent<KdatasView> getKdatas(@PathVariable(value="itemID") String itemID) {
		KdatasView kdatas = ts.getKdatas(itemID);
		return new ResponseContent<KdatasView>(ResponseEnum.SUCCESS, kdatas);
	}
}
