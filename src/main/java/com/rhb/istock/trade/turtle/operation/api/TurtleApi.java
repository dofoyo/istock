package com.rhb.istock.trade.turtle.operation.api;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
		
		List<PotentialView> potentialviews = new ArrayList<PotentialView>();
		if("hlb".equals(type)) {
			potentialviews =  ts.getPotentials_hlb(theDate);
		}else if("avb".equals(type)) {
			potentialviews =  ts.getPotentials_avb(theDate);
		}
/*		
		//System.out.println("the date is " + theDate);
		Map<String,IndustryView> industryViews = ts.getPotentialIndustrys(theDate);
		IndustryView industryView;
		
		List<PotentialView> potentialviews = ts.getPotentials(type, theDate);		
		for(PotentialView potentialView : potentialviews) {
			industryView = industryViews.get(potentialView.getIndustry());
			if(industryView != null) {
				potentialView.setIndustry(industryView.getIndustry());
				potentialView.setCount(industryView.getCount());
			}
			//System.out.println(potentialView);
		}
*/			
/*		Collections.sort(potentialviews, new Comparator<PotentialView>() {
			@Override
			public int compare(PotentialView o1, PotentialView o2) {
				if(o1.getHnGap().compareTo(o2.getHnGap())==0) {
					return
				}
				return o1.getHnGap().compareTo(o2.getHnGap());
				if(o2.getIndustryHot().compareTo(o1.getIndustryHot())==0) {
					return o1.getHlGap().compareTo(o2.getHlGap());
				}else {
					return o2.getIndustryHot().compareTo(o1.getIndustryHot());
				}
			}
			
		});*/
	
		return new ResponseContent<List<PotentialView>>(ResponseEnum.SUCCESS, potentialviews);
	}
	
	@GetMapping("/turtle/potentials/hlb")
	public ResponseContent<List<TurtleView>> getPotentialsOfHlb() {
		List<TurtleView> views = ts.getPotentials("hlb");
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/potentials/hlb2")
	public ResponseContent<List<TurtleView>> getPotentialsOfHlb2() {
		List<TurtleView> views = ts.getPotentials("hlb2");
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, views);
	}

	@GetMapping("/turtle/potentials/lpb2")
	public ResponseContent<List<TurtleView>> getPotentialsOfLpb2() {
		List<TurtleView> views = ts.getPotentials("lpb2");
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/potentials/avb")
	public ResponseContent<List<TurtleView>> getPotentialsOfAvb() {
		List<TurtleView> views = ts.getPotentials("avb");
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/potentials/lpb")
	public ResponseContent<List<TurtleView>> getPotentialsOfLpb() {
		List<TurtleView> views = ts.getPotentials("lpb");
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, views);
	}

	@GetMapping("/turtle/potentials/drum")
	public ResponseContent<List<TurtleView>> getPotentialsOfDrum() {
		List<TurtleView> views = ts.getPotentials("drum");
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, views);
	}
	
	@GetMapping("/turtle/potentials/bav")
	public ResponseContent<List<TurtleView>> getPotentialsOfBav() {
		List<TurtleView> views = ts.getPotentials("bav");
		return new ResponseContent<List<TurtleView>>(ResponseEnum.SUCCESS, views);
	}
	
	/*	
	@GetMapping("/turtle/potentials/redo")
	public ResponseContent<String> redoPotentials() {
		ts.redoPotentials();
		return new ResponseContent<String>(ResponseEnum.SUCCESS, "");
	}*/
	
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
