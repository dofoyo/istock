package com.rhb.istock;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.rhb.istock.item.spider.ItemSpider;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.selector.SelectorService;
import com.rhb.istock.trade.turtle.operation.TurtleOperationService;

@Component
public class AppInitRunner implements CommandLineRunner {
	@Autowired
	@Qualifier("itemSpiderTushare")
	ItemSpider itemSpider;
	
	@Autowired
	@Qualifier("turtleOperationServiceImp")
	TurtleOperationService turtleOperationService;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("selectorServiceImp")
	SelectorService selectorService;
	
    @Override
    public void run(String... args) throws Exception {
		itemSpider.download();
    	kdataService.downKdatas();
    	turtleOperationService.init();
    	selectorService.generateLatestAverageAmountTops();
    	selectorService.generateLatestHighLowTops();
    }

}