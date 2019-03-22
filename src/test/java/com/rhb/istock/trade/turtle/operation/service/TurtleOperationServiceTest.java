package com.rhb.istock.trade.turtle.operation.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.trade.turtle.operation.TurtleOperationService;
import com.rhb.istock.trade.turtle.operation.api.HoldView;
import com.rhb.istock.trade.turtle.operation.api.KdatasView;
import com.rhb.istock.trade.turtle.operation.api.TurtleView;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TurtleOperationServiceTest {
	@Autowired
	@Qualifier("turtleOperationServiceImp")
	TurtleOperationService turtleService;

	
	//@Test
	public void getAvTops() {
		List<TurtleView> views = turtleService.getAvTops(50);
		for(TurtleView view : views) {
			System.out.println(view);
		}		
	}

	//@Test
	public void testGetHolds() {
		List<HoldView> views = turtleService.getHolds();
		for(HoldView view : views) {
			System.out.println(view);
		}
	}
	
	//@Test
	public void testGetKdatas() {
		String itemID = "sh600919";
		KdatasView view = turtleService.getKdatas(itemID);
		System.out.println(view);
	}

}
