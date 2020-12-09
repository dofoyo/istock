package com.rhb.istock.producer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.producer.Producer;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ProduceTest {
	@Autowired
	@Qualifier("producerService")
	ProducerService producerService;
	
	@Autowired
	@Qualifier("b21Favor")
	Producer b21Favor;

	@Autowired
	@Qualifier("drumFavor")
	Producer drumFavor;

	@Autowired
	@Qualifier("newbFavor")
	Producer newbFavor;

	@Autowired
	@Qualifier("b21")
	Producer b21;

	@Autowired
	@Qualifier("drum")
	Producer drum;

	@Autowired
	@Qualifier("newb")
	Producer newb;

	@Autowired
	@Qualifier("dimeReco")
	Producer dimeReco;

	@Autowired
	@Qualifier("dimeNewbReco")
	Producer dimeNewbReco;
	
	@Autowired
	@Qualifier("b21Reco")
	Producer b21Reco;
	
	@Autowired
	@Qualifier("b21plusH21")
	Producer b21plusH21;

	@Autowired
	@Qualifier("b21plusL21")
	Producer b21plusL21;

	@Autowired
	@Qualifier("b21RecoH21")
	Producer b21RecoH21;

	@Autowired
	@Qualifier("drumRecoH21")
	Producer drumRecoH21;
	
	@Autowired
	@Qualifier("newbRecoH21")
	Producer newbRecoH21;
	
	@Test
	public void produce4() {  
		LocalDate date = LocalDate.parse("2020-04-13");
		
		List<String> results = newbFavor.getResults(date);
		System.out.println("there are " + results.size() + " stocks.");
		System.out.println(results);
	}
	
	//@Test
	public void produce2() {  //做收盘
		LocalDate bDate = LocalDate.parse("2010-01-01");
		LocalDate eDate = LocalDate.parse("2020-11-27");
		
		producerService.produce(bDate, eDate);
		System.out.println("收盘 Test");
	}
	
	//@Test
	public void produce1() {  //做收盘
		LocalDate date = LocalDate.parse("2020-11-17");
		
		drumFavor.produce(date, true);
		System.out.println("收盘 Test");
	}
	
	//@Test
	public void produce() {
		LocalDate bDate = LocalDate.parse("2018-02-16");
		LocalDate eDate = LocalDate.parse("2020-11-23");
		
		dimeReco.produce(bDate, eDate);
		dimeNewbReco.produce(bDate, eDate);
		System.out.println("produce Test");
	}
	
	//@Test
	public void getResult() {
		LocalDate bDate = LocalDate.parse("2020-01-25");
		LocalDate eDate = LocalDate.parse("2020-11-07");
		
		Map<LocalDate, List<String>> results = b21Favor.getResults(bDate, eDate);
		System.out.println(results);
		
	}
}
