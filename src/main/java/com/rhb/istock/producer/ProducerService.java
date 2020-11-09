package com.rhb.istock.producer;

import java.time.LocalDate;

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
	
	public void produce(LocalDate date) {
		System.out.println("收盘后生成买入清单");
		long beginTime=System.currentTimeMillis(); 

		b21Reco.produce(date, true);
		drumReco.produce(date, true);
		newbPlus.produce(date, true);
		b21plus.produce(date, true);
		drumPlus.produce(date, true);
		newbReco.produce(date, true);
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("执行“收盘后生成买入清单”完成，用时：" + used + "秒");          
	}
	
	public void produce(LocalDate bDate, LocalDate eDate) {
		System.out.println("重新生成买入清单");
		long beginTime=System.currentTimeMillis(); 

		b21Reco.produce(bDate, eDate);
		drumReco.produce(bDate, eDate);
		newbPlus.produce(bDate, eDate);
		b21plus.produce(bDate, eDate);
		drumPlus.produce(bDate, eDate);
		newbReco.produce(bDate, eDate);
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("执行“重新生成买入清单”完成，用时：" + used + "秒");          
	}
}
