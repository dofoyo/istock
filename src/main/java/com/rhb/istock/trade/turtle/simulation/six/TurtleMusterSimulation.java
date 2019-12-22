package com.rhb.istock.trade.turtle.simulation.six;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.bluechip.BluechipService;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Service("turtleMusterSimulation")
public class TurtleMusterSimulation {
	protected static final Logger logger = LoggerFactory.getLogger(TurtleMusterSimulation.class);

	@Value("${musterPath}")
	private String musterPath;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("bluechipServiceImp")
	BluechipService bluechipService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	BigDecimal initCash = new BigDecimal(1000000);
	
	/*
	 * 根据输入起止日期，系统模拟买入和卖出
	 */
	public void simulate(LocalDate beginDate, LocalDate endDate) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("simulate from " + beginDate + " to " + endDate +" ......");

		BAV bav = new BAV(initCash);
		Blue bhl = new Blue(initCash);
		SHB bdt = new SHB(initCash);  //股本

		//MVB dtb = new MVB(initCash);  //市值
		AVB avb = new AVB(initCash);
		HLB hlb = new HLB(initCash);

		Map<String,Muster> musters;

		Integer sseiFlag;
		
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, "  simulate: " + date.toString());

			musters = kdataService.getMusters(date);
			
			if(musters!=null && musters.size()>0) {
				sseiFlag = kdataService.getSseiFlag(date);
				
				//System.out.println("\n sseiFlag=" + sseiFlag);
				
				bav.doIt(musters, itemService.getSz50(date), date,sseiFlag);
				bhl.doIt(musters, bluechipService.getBluechipIDs(date), date,sseiFlag);
				bdt.doIt(musters, date,sseiFlag);
				//dtb.doIt(musters, date,sseiFlag);
				hlb.doIt(musters, date,sseiFlag);
				avb.doIt(musters, date,sseiFlag);
			}
		}
		
		Map<String, String> bavResult = bav.result();
		Map<String, String> bhlResult = bhl.result();
		Map<String, String> bdtResult = bdt.result();
		//Map<String, String> dtbResult = dtb.result();

		Map<String, String> avbResult = avb.result();
		Map<String, String> hlbResult = hlb.result();
		
		turtleSimulationRepository.save("bav", bavResult.get("breakers"), bavResult.get("CSV"), bavResult.get("dailyAmount"));
		turtleSimulationRepository.save("bhl", bhlResult.get("breakers"), bhlResult.get("CSV"), bhlResult.get("dailyAmount"));
		turtleSimulationRepository.save("bdt", bdtResult.get("breakers"), bdtResult.get("CSV"), bdtResult.get("dailyAmount"));
		
		//turtleSimulationRepository.save("dtb", dtbResult.get("breakers"), dtbResult.get("CSV"), dtbResult.get("dailyAmount"));
		turtleSimulationRepository.save("hlb", hlbResult.get("breakers"), hlbResult.get("CSV"), hlbResult.get("dailyAmount"));
		turtleSimulationRepository.save("avb", avbResult.get("breakers"), avbResult.get("CSV"), avbResult.get("dailyAmount"));
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
}