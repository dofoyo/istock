package com.rhb.istock.trade.balloon.operation.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.trade.balloon.operation.BalloonOperationService;
import com.rhb.istock.trade.balloon.operation.api.BluechipView;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class BalloonOperationServiceTest {
	@Autowired
	@Qualifier("balloonOperationServiceImp")
	BalloonOperationService balloonOperationService;
	
	@Test
	public void test() {
		List<BluechipView> views = balloonOperationService.getBluechips();
		for(BluechipView view : views) {
			System.out.println(view);
		}
	}
}
