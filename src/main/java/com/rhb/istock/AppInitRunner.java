package com.rhb.istock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.rhb.istock.fdata.eastmoney.FdataSpiderEastmoney;
import com.rhb.istock.item.ItemService;
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
	@Qualifier("fdataSpiderEastmoney")
	FdataSpiderEastmoney fdataSpiderEastmoney;
	
/*	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("selectorServiceImp")
	SelectorService selectorService;

	@Autowired
	@Qualifier("kdataRealtimeSpiderImp")
	KdataRealtimeSpider kdataRealtimeSpider;
*/
	//protected static final Logger logger = LoggerFactory.getLogger(AppInitRunner.class);
	
    @Override
    public void run(String... args) throws Exception {
    	//long beginTime=System.currentTimeMillis(); 
    	//logger.info("AppInitRunner ......");
		
		itemService.init();
/*     	itemService.download();
    	
		if(this.isTradeDate()) {
	    	kdataService.downSSEI();
	    	kdataService.downFactors();  //上一交易日的收盘数据下载完成后，执行generateMuster，下载并整理上一交易日的收盘数据2分钟，生成muster需要3分钟，合计大概需要5分钟
		}
*/
    	turtleOperationService.init();
		//fdataSpiderEastmoney.downProfitForecasts();
		//fdataSpiderEastmoney.downRecommendations();

/*    	logger.info("AppInitRunner done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");          
*/    }

/*	private boolean isTradeDate() {
		LocalDate now = LocalDate.now();
		LocalDate theDay = kdataRealtimeSpider.getLatestMarketDate("sh000001");
		
		//boolean flag = kdataRealtimeSpider.isTradeDate(now);
		boolean flag = now.equals(theDay) ? true : false;
		
		if(flag) {
	    	logger.info("today is a trade date. Good Luck!");
		}else {
	    	logger.info("today is NOT a trade date. Have Fun!");
		}
		
		return flag;
	}
    */
}