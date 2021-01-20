package com.rhb.istock;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.rhb.istock.evaluation.Evaluation;
import com.rhb.istock.fdata.eastmoney.FdataSpiderEastmoney;
import com.rhb.istock.index.tushare.IndexSpiderTushare;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.producer.ProducerService;
import com.rhb.istock.selector.drum.DrumService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IstockApplicationTests {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("producerService")
	ProducerService producerService;

	@Autowired
	@Qualifier("indexSpiderTushare")
	IndexSpiderTushare indexSpiderTushare;

	@Autowired
	@Qualifier("fdataSpiderEastmoney")
	FdataSpiderEastmoney fdataSpiderEastmoney;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("drumService")
	DrumService drumService;

	@Autowired
	@Qualifier("evaluation")
	Evaluation evaluation;

	//执行收盘
	@Test
	public void doClose() {
		System.out.println("doClose .....");
		long beginTime=System.currentTimeMillis(); 

			LocalDate date = LocalDate.parse("2020-11-15");
			//LocalDate date = LocalDate.now();
			try {
				//kdataService.downClosedDatas(date);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//kdataService.generateMusters(LocalDate.parse("2009-01-01"));   //生成muster，需要192分钟，即3个多小时
			//producerService.produce(date);
			//fdataSpiderTushare.downAll();  //财务报告、预告、股东信息等下载
			//indexSpiderTushare.downIndex_Daily("000001.SH");
			//indexSpiderTushare.downIndex_weight();
			//indexSpiderTushare.downIndex_basic();
			//indexServiceTushare.generateIndex();
			//fdataSpiderEastmoney.downProfitForecasts();
			//fdataSpiderEastmoney.downRecommendations();
			itemService.downTopics();
			//drumService.generateDimensions();
			
			//evaluation.evaluate(LocalDate.parse("2017-01-01"), date);


		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("执行收盘完成，用时：" + used + "秒");          
	}

}
