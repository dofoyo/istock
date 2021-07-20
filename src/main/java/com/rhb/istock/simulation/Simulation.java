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
@Service("simulation")
public class Simulation {
	protected static final Logger logger = LoggerFactory.getLogger(Simulation.class);

	@Autowired
	@Qualifier("avb")
	Simulate avb;

	@Autowired
	@Qualifier("bav")
	Simulate bav;

	@Autowired
	@Qualifier("bdt")
	Simulate bdt;

	@Autowired
	@Qualifier("bhl")
	Simulate bhl;

	@Autowired
	@Qualifier("hlb")
	Simulate hlb;
	
	@Autowired
	@Qualifier("dtb")
	Simulate dtb;

	@Value("${initCash}")
	private BigDecimal initCash;
	
	public void simulate(LocalDate beginDate, LocalDate endDate){
		long beginTime=System.currentTimeMillis(); 
		
		System.out.println("simulate from " + beginDate + " to " + endDate +" ......");
		
		//BigDecimal ic = new BigDecimal(initCash);
		Integer top = 3;
		boolean isAveValue = true;  //作市值平均
		Integer quantityType = 0;
		boolean isEvaluation = false;  //是模拟，不是评估
		try {
			Future<String> favb = avb.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			Future<String> fbav = bav.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			Future<String> fbdt = bdt.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			Future<String> fbhl = bhl.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			Future<String> fhlb = hlb.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			Future<String> fdtb = dtb.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			
			while(true) {
				if(true
					&& favb.isDone()
					&& fbav.isDone()
					&& fbdt.isDone()
					&& fbhl.isDone()
					&& fhlb.isDone() 
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