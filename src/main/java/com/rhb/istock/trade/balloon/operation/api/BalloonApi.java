package com.rhb.istock.trade.balloon.operation.api;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.istock.comm.api.ResponseContent;
import com.rhb.istock.comm.api.ResponseEnum;
import com.rhb.istock.trade.balloon.operation.BalloonOperationService;

@RestController
public class BalloonApi{
	@Autowired
	@Qualifier("balloonOperationServiceImp")
	BalloonOperationService ts;

	@GetMapping("/balloon/bluechips")
	public ResponseContent<List<BluechipView>> getBluechips() {
		List<BluechipView> bluechips = ts.getBluechips();
		Collections.sort(bluechips, new Comparator<BluechipView>() {
			@Override
			public int compare(BluechipView o1, BluechipView o2) {
				if(o2.getStatus().equals(o1.getStatus())) {
					return o2.getUpPower().compareTo(o1.getUpPower());
				}else {
					return o2.getStatus().compareTo(o1.getStatus());
				}
			}
		});	
		return new ResponseContent<List<BluechipView>>(ResponseEnum.SUCCESS, bluechips);
	}
}
