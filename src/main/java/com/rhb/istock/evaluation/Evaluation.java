package com.rhb.istock.evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.simulation.Simulate;
import com.rhb.istock.simulation.SimulateAVB2;
import com.rhb.istock.simulation.SimulateBAV;
import com.rhb.istock.simulation.SimulateBAV2;
import com.rhb.istock.simulation.SimulateBDT;
import com.rhb.istock.simulation.SimulateBDT2;
import com.rhb.istock.simulation.SimulateBHL;
import com.rhb.istock.simulation.SimulateBHL2;

//@Scope("prototype")
@Service("evaluation")
public class Evaluation {
	protected static final Logger logger = LoggerFactory.getLogger(Evaluation.class);
	@Autowired
	@Qualifier("evaluationRepository")
	EvaluationRepository evaluationRepository;
	
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

/*	@Autowired
	@Qualifier("dtb")
	SimulateDTB dtb;

	@Autowired
	@Qualifier("hlb")
	SimulateHLB hlb;
	
	@Autowired
	@Qualifier("bnew")
	SimulateNEWB bnew;
	
	@Autowired
	@Qualifier("hunt")
	SimulateHunt hunt;*/

	@Autowired
	@Qualifier("avb2")
	Simulate avb2;

	@Autowired
	@Qualifier("bav2")
	Simulate bav2;

	@Autowired
	@Qualifier("bdt2")
	Simulate bdt2;

	@Autowired
	@Qualifier("bhl2")
	Simulate bhl2;

	
	@Value("${initCash}")
	private BigDecimal initCash;
	
	public void evaluate(LocalDate beginDate, LocalDate endDate){
		long beginTime=System.currentTimeMillis(); 
		
		System.out.println("evaluate from " + beginDate + " to " + endDate +" ......");
		
		//BigDecimal ic = new BigDecimal(initCash);
		Integer top = 1000;  //买入所有符合的股票
		boolean isAveValue = false;  //买入前不做市值平均
		//Integer quantityType = 3;  //按固定数量买入
		Integer quantityType = 1;  //按固定金额买入
		boolean isEvaluation = true; //不是模拟，是评估
		try {
			Future<String> favb = avb.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			Future<String> fbav = bav.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			Future<String> fbdt = bdt.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			Future<String> fbhl = bhl.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			//Future<String> fhlb = hlb.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			//Future<String> fdtb = dtb.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			//Future<String> fbnew = bnew.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			//Future<String> fhunt = hunt.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);

			Future<String> favb2 = avb2.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			Future<String> fbav2 = bav2.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			Future<String> fbdt2 = bdt2.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
			Future<String> fbhl2 = bhl2.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);

			while(true) {
				if(true 
					&& favb.isDone()
					&& fbav.isDone()
					&& fbdt.isDone()
					&& fbhl.isDone()
					//&& fhlb.isDone() 
					//&& fdtb.isDone()
					//&& fbnew.isDone()
					//&& fhunt.isDone()
					&& favb2.isDone()
					&& fbav2.isDone()
					&& fbdt2.isDone()
					&& fbhl2.isDone()
						) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		evaluationRepository.evictBusisCache();
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("\nsimulate over, 用时：" + used + "秒");          
	}
	
}