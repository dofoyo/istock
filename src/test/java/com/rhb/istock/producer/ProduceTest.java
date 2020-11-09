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
	@Qualifier("newbDime")
	Producer produce;

	@Test
	public void produce1() {  //做收盘
		LocalDate date = LocalDate.parse("2020-11-09");
		
		produce.produce(date, true);
		System.out.println("收盘 Test");
	}
	
	//@Test
	public void produce() {
		LocalDate bDate = LocalDate.parse("2010-01-01");
		LocalDate eDate = LocalDate.parse("2020-11-07");
		
		produce.produce(bDate, eDate);
		System.out.println("produce Test");
	}
	
	//@Test
	public void getResult() {
		LocalDate bDate = LocalDate.parse("2020-01-25");
		LocalDate eDate = LocalDate.parse("2020-11-07");
		
		Map<LocalDate, List<String>> results = produce.getResults(bDate, eDate);
		System.out.println(results);
		
	}
}
