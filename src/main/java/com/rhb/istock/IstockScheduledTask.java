package com.rhb.istock;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.rhb.istock.evaluation.Evaluation;
import com.rhb.istock.fdata.eastmoney.FdataSpiderEastmoney;
import com.rhb.istock.fdata.tushare.FdataSpiderTushare;
import com.rhb.istock.index.tushare.IndexServiceTushare;
import com.rhb.istock.index.tushare.IndexSpiderTushare;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.spider.KdataRealtimeSpider;
import com.rhb.istock.producer.ProducerService;
import com.rhb.istock.selector.drum.DrumService;
import com.rhb.istock.selector.fina.FinaService;
import com.rhb.istock.trade.turtle.operation.TurtleOperationService;

@Component
public class IstockScheduledTask {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("finaService")
	FinaService finaService;
	
	@Autowired
	@Qualifier("fdataSpiderTushare")
	FdataSpiderTushare fdataSpiderTushare;
	
	@Autowired
	@Qualifier("indexSpiderTushare")
	IndexSpiderTushare indexSpiderTushare;
	
	@Autowired
	@Qualifier("indexServiceTushare")
	IndexServiceTushare indexServiceTushare;
	
	@Autowired
	@Qualifier("drumService")
	DrumService drumService;

	@Autowired
	@Qualifier("evaluation")
	Evaluation evaluation;
	
	@Autowired
	@Qualifier("fdataSpiderEastmoney")
	FdataSpiderEastmoney fdataSpiderEastmoney;
	
/*	@Autowired
	@Qualifier("financialStatementServiceImp")
	FinancialStatementService financialStatementService;*/
/*
	@Autowired
	@Qualifier("selectorServiceImp")
	SelectorService selectorService;
	*/
	@Autowired
	@Qualifier("turtleOperationServiceImp")
	TurtleOperationService turtleOperationService;

	@Autowired
	@Qualifier("kdataRealtimeSpiderImp")
	KdataRealtimeSpider kdataRealtimeSpider;
	
	@Autowired
	@Qualifier("producerService")
	ProducerService producerService;
	
/*	@Autowired
	@Qualifier("huaService")
	HuaService huaService;

	@Autowired
	@Qualifier("bavService")
	BavService bavService;
	
	@Autowired
	@Qualifier("lpbService")
	LpbService lpbService;*/
	
	protected static final Logger logger = LoggerFactory.getLogger(IstockScheduledTask.class);
	
	private boolean isTradeDate = false;  
	private LocalDate theDate = null;
	
	private boolean isTradeDate() {
		LocalDate now = LocalDate.now();
		if(this.theDate==null || !now.equals(theDate)) {
			LocalDate dd = kdataRealtimeSpider.getLatestMarketDate("sh000001");

			//this.isTradeDate = kdataRealtimeSpider.isTradeDate(now);
			this.isTradeDate = now.equals(dd) ? true : false;
			
			theDate = now;
		}
		
		if(this.isTradeDate) {
	    	System.out.println("today is a trade date. Good Luck!");
		}else {
			System.out.println("today is NOT a trade date. Have Fun!");
		}
		
		return this.isTradeDate;
	}
	
	/*
	 * 每周1至5，9:45,开盘后，
	 * 1、下载最新股票代码
	 * 2、下载上一交易日收盘后的K线数据
	 * 3、初始化: 即把日K线读入内存
	 */
	@Scheduled(cron="0 39 9 ? * 1-5") 
	public void dailyInit() throws Exception {
		logger.info("开盘初始化, run scheduled of '0 35 9 ? * 1-5'");
		if(this.isTradeDate()) {   //次序很重要
			itemService.downItems();		// 1. 下载最新股票代码
			itemService.init();  // 2. 
			//kdataService.downFactors(); // 3. 上一交易日的收盘数据要等开盘前才能下载到, 大约需要15分钟
			kdataService.downSSEI();
			kdataService.generateLatestMusters(null, true);
			turtleOperationService.init();  // 4.
			indexServiceTushare.init();
			kdataService.updateLatestMusters();
			logger.info("开盘初始化, OK!");
		}
	}

/*	//@Scheduled(cron="0 45/1 9-14 ? * 1-5")  //周一至周五，每日9:45点，每1分钟刷新一次 
	public void updateLatestMusters3() throws Exception {
		System.out.println("run scheduled of '0 45/1 9-14 ? * 1-5'");
		if(this.isTradeDate()) {
			kdataService.updateLatestMustersOfFavors();
		}
	}*/
	
	@Scheduled(cron="0 0/2 10-14 ? * 1-5")  //周一至周五，每日10 - 14点，每2分钟刷新一次 
	public void updateLatestMusters1() throws Exception {
		System.out.println("run scheduled of '0 0/2 10-14 ? * 1-5'");
		if(this.isTradeDate()) {
			kdataService.updateLatestMusters();
		}
	}

	@Scheduled(cron="0 0 15 ? * 1-5")  //周一至周五，每日15点 
	public void updateLatestMusters2() throws Exception {
		System.out.println("run scheduled of '0 0 15 ? * 1-5'");
		if(this.isTradeDate()) {
			kdataService.updateLatestMusters();
		}
	}
	
/*	@Scheduled(cron="0 50 9 ? * 1-5") //周一至周五，每日9:50点
	public void downloadReports() {
		System.out.println("run scheduled of '0 50 9 ? * 1-5'");
		if(this.isTradeDate()) {
			financialStatementService.downloadReports();  //下载最新年报(包括新股)
			selectorService.generateBluechip();  //并生成bluechip
			kdataService.generateMusters();   //生成muster，需要192分，即3个多小时
		}
	}*/
	
	@Scheduled(cron="0 0 12 ? * 1-5") //周一至周五，每日12点下载除权因子
	public void downFactors()  throws Exception{
		logger.info("下载除权因子, run scheduled of '0 0 12 ? * 1-5', downFactors");
		long beginTime=System.currentTimeMillis(); 

		if(this.isTradeDate()) {
			//LocalDate date = LocalDate.parse("2020-11-12");
			LocalDate date = LocalDate.now();
			kdataService.downFactors(date);
			kdataService.generateLatestMusters(null, true);
			kdataService.updateLatestMusters();
		}

		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("下载除权因子完成，用时：" + used + "秒");          
	}
	
	@Scheduled(cron="0 0 21 ? * 1-5") //周一至周五，每日21点0执行收盘
	public void downloadKdatas()  throws Exception{
		logger.info("执行收盘, run scheduled of '0 0 21 ? * 1-5'");
		long beginTime=System.currentTimeMillis(); 

		if(this.isTradeDate()) {
			//LocalDate date = LocalDate.parse("2020-11-12");
			LocalDate date = LocalDate.now();
			kdataService.downClosedDatas(date);
			kdataService.generateMusters(LocalDate.parse("2009-01-01"));   //生成muster，需要192分钟，即3个多小时
			producerService.produce(date);
			fdataSpiderTushare.downAll();  //财务报告、预告、股东信息等下载
			indexSpiderTushare.downIndex_Daily("000001.SH");
			//indexSpiderTushare.downIndex_weight();
			//indexSpiderTushare.downIndex_basic();
			//indexServiceTushare.generateIndex();
			fdataSpiderEastmoney.downProfitForecasts();
			fdataSpiderEastmoney.downRecommendations();
			itemService.downTopics();
			drumService.generateDimensions();
			
			evaluation.evaluate(LocalDate.parse("2017-01-01"), date);

		}

		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("执行收盘完成，用时：" + used + "秒");          
	}

}

/**
 * 根据cron表达式格式触发定时任务
 *  cron表达式格式:
 *      1.Seconds Minutes Hours DayofMonth Month DayofWeek Year
 *      2.Seconds Minutes Hours DayofMonth Month DayofWeek 
 *  顺序:
 *      秒（0~59）
 *      分钟（0~59）
 *      小时（0~23）
 *      天（月）（0~31，但是你需要考虑你月的天数）
 *      月（0~11）
 *      天（星期）（1~7 1=SUN 或 SUN，MON，TUE，WED，THU，FRI，SAT）
 *      年份（1970－2099）
 * 
 *  注:其中每个元素可以是一个值(如6),一个连续区间(9-12),一个间隔时间(8-18/4)(/表示每隔4小时),一个列表(1,3,5),通配符。
 *  由于"月份中的日期"和"星期中的日期"这两个元素互斥的,必须要对其中一个设置?.
 *  
 *  
 */
// *  * 第一位，表示秒，取值0-59
//	* 第二位，表示分，取值0-59
//	* 第三位，表示小时，取值0-23
//	* 第四位，日期天/日，取值1-31
//	* 第五位，日期月份，取值1-12
//	* 第六位，星期，取值1-7，星期一，星期二...，注：不是第1周，第二周的意思
//	          另外：1表示星期天，2表示星期一。
//	* 第7为，年份，可以留空，取值1970-2099
//	*
//		(*)星号：可以理解为每的意思，每秒，每分，每天，每月，每年...
//		(?)问号：问号只能出现在日期和星期这两个位置，表示这个位置的值不确定，每天3点执行，所以第六位星期的位置，我们是不需要关注的，就是不确定的值。同时：日期和星期是两个相互排斥的元素，通过问号来表明不指定值。比如，1月10日，比如是星期1，如果在星期的位置是另指定星期二，就前后冲突矛盾了。
//		(-)减号：表达一个范围，如在小时字段中使用“10-12”，则表示从10到12点，即10,11,12
//		(,)逗号：表达一个列表值，如在星期字段中使用“1,2,4”，则表示星期一，星期二，星期四
//		(/)斜杠：如：x/y，x是开始值，y是步长，比如在第一位（秒） 0/15就是，从0秒开始，每15秒，最后就是0，15，30，45，60    另：*/y，等同于0/y

