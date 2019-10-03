package com.rhb.istock;


import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.spider.KdataRealtimeSpider;
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

	@Autowired
	@Qualifier("kdataRealtimeSpiderImp")
	KdataRealtimeSpider kdataRealtimeSpider;

	protected static final Logger logger = LoggerFactory.getLogger(AppInitRunner.class);
	
    @Override
    public void run(String... args) throws Exception {
    	long beginTime=System.currentTimeMillis(); 
    	logger.info("AppInitRunner ......");
		
		itemService.init();
    	
		if(this.isTradeDate()) {
	     	itemService.download();
	    	kdataService.downKdatasAndFactors();  //上一交易日的收盘数据下载完成后，执行generateMuster，下载并整理上一交易日的收盘数据2分钟，生成muster需要3分钟，合计大概需要5分钟
	    	kdataService.downSSEI();
		}

    	turtleOperationService.init();

    	logger.info("AppInitRunner done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");          
    }

	private boolean isTradeDate() {
		LocalDate now = LocalDate.now();
		boolean flag = kdataRealtimeSpider.isTradeDate(now);
		
		if(flag) {
	    	logger.info("today is a trade date. Good Luck!");
		}else {
	    	logger.info("today is NOT a trade date. Have Fun!");
		}
		
		return flag;
	}
    
}