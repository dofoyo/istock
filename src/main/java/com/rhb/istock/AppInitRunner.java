package com.rhb.istock;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.rhb.istock.item.spider.ItemSpider;
import com.rhb.istock.trade.turtle.service.TurtleOperationService;


@Component
public class AppInitRunner implements CommandLineRunner {
	@Autowired
	@Qualifier("itemSpiderTushare")
	ItemSpider itemSpider;
	
	@Autowired
	@Qualifier("turtleOperationServiceImp")
	TurtleOperationService turtleOperationService;
	
	
    @Override
    public void run(String... args) throws Exception {
		itemSpider.download();
    	turtleOperationService.init();
		turtleOperationService.generatePreys();
    }

}