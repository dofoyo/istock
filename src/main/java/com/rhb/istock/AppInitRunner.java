package com.rhb.istock;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.selector.SelectorService;
import com.rhb.istock.trade.turtle.operation.TurtleOperationService;

@Component
public class AppInitRunner implements CommandLineRunner {
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
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
    	itemService.init();
    	
     	itemService.download();
    	kdataService.downKdatas();  //上一交易日的收盘数据下载完成后，执行generateMuster
    	kdataService.downLatestFactors(); // 下载最新交易日的除权因子后，执行generateLatestFactors

    	turtleOperationService.init();
    }

}