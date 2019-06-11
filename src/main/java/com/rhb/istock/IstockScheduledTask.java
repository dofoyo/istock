package com.rhb.istock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.rhb.istock.fdata.FinancialStatementService;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.selector.SelectorService;

@Component
public class IstockScheduledTask {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("financialStatementServiceImp")
	FinancialStatementService financialStatementService;

	@Autowired
	@Qualifier("selectorServiceImp")
	SelectorService selectorService;
	
	
	/*
	 * 每周1至5，9:30,开盘后，
	 * 1、下载最新股票代码
	 * 2、下载上一交易日收盘后的K线数据
	 * 3、初始化: 即把日K线读入内存
	 */
	@Scheduled(cron="0 30 9 ? * 1-5") 
	public void dailyInit() throws Exception {
		itemService.download();		//下载最新股票代码
		kdataService.downKdatasAndFactors(); //上一交易日的收盘数据要等开盘前才能下载到
	}

	@Scheduled(cron="0 30 15 ? * 1-5") 
	public void dailyClose() throws Exception {
	}
	
	@Scheduled(cron="0 45 0 ? * 1-5") //每日凌晨0点
	public void downloadReports() {
		//financialStatementService.downloadReports();  //下载最新年报
		//selectorService.generateBluechip();  //并生成bluechip
		kdataService.generateMusters();   //生成muster，需要192分，即3个多小时
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

