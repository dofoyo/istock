package com.rhb.istock.simulation;

import java.time.LocalDate;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("simulation")
public class Simulation {
	protected static final Logger logger = LoggerFactory.getLogger(Simulation.class);

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

	
	public void simulate(LocalDate beginDate, LocalDate endDate) throws InterruptedException {
		long beginTime=System.currentTimeMillis(); 
		
/*		Future<String> fhlb = hlb.run(beginDate, endDate);
		Future<String> fbdt = bdt.run(beginDate, endDate);
		Future<String> favb = avb.run(beginDate, endDate);
		Future<String> fbhl = bhl.run(beginDate, endDate);
		Future<String> fbav = bav.run(beginDate, endDate);
		Future<String> fdtb = dtb.run(beginDate, endDate);
*/		
		Future<String> fhlb = hlb.run(beginDate, endDate);
		Future<String> fbdt = bdt.run(beginDate, endDate);
		Future<String> favb = avb.run(beginDate, endDate);
		Future<String> fbhl = bhl.run(beginDate, endDate);
		Future<String> fbav = bav.run(beginDate, endDate);
		Future<String> fdtb = dtb.run(beginDate, endDate);
		
		while(true) {
			if(fhlb.isDone() 
				&& fbdt.isDone()
				&& favb.isDone()
				&& fbhl.isDone()
				&& fbav.isDone()
				&& fdtb.isDone()
					) {
				break;
			}
		}
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("simulate 用时：" + used + "秒");          
	}
	
}