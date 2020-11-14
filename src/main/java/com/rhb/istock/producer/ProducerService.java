package com.rhb.istock.producer;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("producerService")
public class ProducerService {
	@Autowired
	@Qualifier("b21Reco")
	Producer b21Reco;
	
	@Autowired
	@Qualifier("drumReco")
	Producer drumReco;
	
	@Autowired
	@Qualifier("newbPlus")
	Producer newbPlus;
	
	@Autowired
	@Qualifier("b21plus")
	Producer b21plus;
	
	@Autowired
	@Qualifier("drumPlus")
	Producer drumPlus;
	
	@Autowired
	@Qualifier("newbReco")
	Producer newbReco;
	
	@Autowired
	@Qualifier("b21Favor")
	Producer b21Favor;

	@Autowired
	@Qualifier("drumFavor")
	Producer drumFavor;

	@Autowired
	@Qualifier("newbFavor")
	Producer newbFavor;

	
	public void produce(LocalDate date) {
		System.out.println("收盘后生成买入清单");
		long beginTime=System.currentTimeMillis(); 
		
		List<String> results;
		
		results  = b21Reco.produce(date, true);
		System.out.println("b21Reco: " + results);
		
		results  = drumReco.produce(date, true);
		System.out.println("drumReco: " + results);

		results  = newbReco.produce(date, true);
		System.out.println("newbReco: " + results);

		results  = newbPlus.produce(date, true);
		System.out.println("newbPlus: " + results);
		
		results  = b21plus.produce(date, true);
		System.out.println("b21plus: " + results);
		
		results  = drumPlus.produce(date, true);
		System.out.println("drumPlus: " + results);
		
		results  = newbFavor.produce(date, true);
		System.out.println("newbFavor: " + results);
		
		results  = b21Favor.produce(date, true);
		System.out.println("b21Favor: " + results);
		
		results  = drumFavor.produce(date, true);
		System.out.println("drumFavor: " + results);
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("执行“收盘后生成买入清单”完成，用时：" + used + "秒");          
	}
	
	public void produce(LocalDate bDate, LocalDate eDate) {
		System.out.println("重新生成买入清单");
		long beginTime=System.currentTimeMillis(); 

		b21Reco.produce(bDate, eDate);
		drumReco.produce(bDate, eDate);
		newbReco.produce(bDate, eDate);

		newbPlus.produce(bDate, eDate);
		b21plus.produce(bDate, eDate);
		drumPlus.produce(bDate, eDate);

		//b21Favor.produce(bDate, eDate);
		//drumFavor.produce(bDate, eDate);
		//newbFavor.produce(bDate, eDate);

		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("执行“重新生成买入清单”完成，用时：" + used + "秒");          
	}
}
