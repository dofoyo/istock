package com.rhb.istock.producer;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("producerService")
public class ProducerService {
	@Autowired
	@Qualifier("newbRecoH21")
	Producer newbRecoH21;

	@Autowired
	@Qualifier("newbPlusL21")
	Producer newbPlusL21;

	@Autowired
	@Qualifier("drumRecoH21")
	Producer drumRecoH21;

	@Autowired
	@Qualifier("drumPlusL21")
	Producer drumPlusL21;
	
	@Autowired
	@Qualifier("dimeNewbReco")
	Producer dimeNewbReco;

	@Autowired
	@Qualifier("drum")
	Producer drum;

	@Autowired
	@Qualifier("newb")
	Producer newb;
	
	public void produce(LocalDate date) {
		System.out.println("收盘后生成买入清单");
		long beginTime=System.currentTimeMillis(); 
		
		List<String> results;

		results  = newb.produce(date, true);
		results  = newbRecoH21.produce(date, true);
		results  = newbPlusL21.produce(date, true);

		results  = drum.produce(date, true);
		results  = drumRecoH21.produce(date, true);
		results  = drumPlusL21.produce(date, true);

		results  = dimeNewbReco.produce(date, true);
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("执行“收盘后生成买入清单”完成，用时：" + used + "秒");          
	}
	
	public void produce(LocalDate bDate, LocalDate eDate) {
		System.out.println("重新生成买入清单");
		long beginTime=System.currentTimeMillis(); 

		newb.produce(bDate, eDate);
		newbRecoH21.produce(bDate, eDate);
		newbPlusL21.produce(bDate, eDate);

		drum.produce(bDate, eDate);
		drumRecoH21.produce(bDate, eDate);
		drumPlusL21.produce(bDate, eDate);

		dimeNewbReco.produce(bDate, eDate);

		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("执行“重新生成买入清单”完成，用时：" + used + "秒");          
	}
}
