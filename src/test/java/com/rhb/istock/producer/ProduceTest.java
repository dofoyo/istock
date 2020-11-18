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
	
	//@Test
	public void produce4() {  
		LocalDate date = LocalDate.parse("2020-11-11");
		
		List<String> results = drumFavor.produce(date, false);
		System.out.println("there are " + results.size() + " stocks.");
		System.out.println(results);
	}
	
	@Test
	public void produce2() {  //做收盘
		LocalDate bDate = LocalDate.parse("2020-01-01");
		LocalDate eDate = LocalDate.parse("2020-11-17");
		
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
		LocalDate bDate = LocalDate.parse("2020-01-01");
		LocalDate eDate = LocalDate.parse("2020-11-17");
		
		b21.produce(bDate, eDate);
		newb.produce(bDate, eDate);
		drum.produce(bDate, eDate);
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
