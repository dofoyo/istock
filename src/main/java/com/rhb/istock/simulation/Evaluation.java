package com.rhb.istock.simulation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

//@Scope("prototype")
@Service("evaluation")
public class Evaluation {
	protected static final Logger logger = LoggerFactory.getLogger(Evaluation.class);

	@Autowired
	@Qualifier("avb")
	SimulateAVB avb;

	@Autowired
	@Qualifier("bav")
	SimulateBAV bav;

	@Autowired
	@Qualifier("bdt")
	SimulateBDT bdt;

	@Autowired
	@Qualifier("bhl")
	SimulateBHL bhl;

	@Autowired
	@Qualifier("dtb")
	SimulateDTB dtb;

	@Autowired
	@Qualifier("hlb")
	SimulateHLB hlb;

	@Value("${initCash}")
	private BigDecimal initCash;
	
	public void evaluate(LocalDate beginDate, LocalDate endDate){
		long beginTime=System.currentTimeMillis(); 
		
		System.out.println("simulate from " + beginDate + " to " + endDate +" ......");
		
		//BigDecimal ic = new BigDecimal(initCash);
		Integer top = 1000;  //买入所有符合的股票
		boolean isAveValue = false;  //买入前不做市值平均
		Integer quantityType = 3;  //按固定数量买入
		try {
			Future<String> fhlb = hlb.run(beginDate, endDate, initCash, top, isAveValue, quantityType);
			Future<String> fbdt = bdt.run(beginDate, endDate, initCash, top, isAveValue, quantityType);
			Future<String> favb = avb.run(beginDate, endDate, initCash, top, isAveValue, quantityType);
			Future<String> fbhl = bhl.run(beginDate, endDate, initCash, top, isAveValue, quantityType);
			Future<String> fbav = bav.run(beginDate, endDate, initCash, top, isAveValue, quantityType);
			Future<String> fdtb = dtb.run(beginDate, endDate, initCash, top, isAveValue, quantityType);
			
			while(true) {
				if(true 
					&& fhlb.isDone() 
					&& fbdt.isDone()
					&& favb.isDone()
					&& fbhl.isDone()
					&& fbav.isDone()
					&& fdtb.isDone()
						) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("\nsimulate over, 用时：" + used + "秒");          
	}
	
}