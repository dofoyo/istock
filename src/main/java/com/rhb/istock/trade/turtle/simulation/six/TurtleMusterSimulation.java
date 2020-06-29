package com.rhb.istock.trade.turtle.simulation.six;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Functions;
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
		//System.out.println("Functions.ratio(this.averagePrice21, this.averagePrice)<=13");
		System.out.println("simulate from " + beginDate + " to " + endDate +" ......");

		NEWB hlb = new NEWB(initCash,1); //高价创新高
		NEWB bdt = new NEWB(initCash,0); //低价创新高

		B21 avb = new B21(initCash,1);  //强平衡市策略：高价破21日线
		B21 bhl = new B21(initCash,0);  //弱平衡市策略：低价破21日线

		
		//LPB2 bav = new LPB2(initCash);  //牛市策略：低价均线纠缠+突破21
		Drum bav = new Drum(initCash,1);  //高价+上升趋势+强于大盘
		Drum dtb = new Drum(initCash,0);  //低价+上升趋势+强于大盘

		Map<String,Muster> musters;
		
		List<Map<String,Muster>> previous = new ArrayList<Map<String,Muster>>();
		Integer previous_period  = 8; //历史纪录区间，主要用于后面判断

		Integer sseiFlag, sseiRatio;
		
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {

			musters = kdataService.getMusters(date);

			Progress.show((int)days, i++, "  simulate: " + date.toString() + ", musters.size()=" + musters.size() + " ");
			
			if(musters!=null && musters.size()>0) {
				previous.add(musters);
				if(previous.size()>=previous_period) {
					previous.remove(0);
				}
				
				sseiFlag = kdataService.getSseiFlag(date);
				sseiRatio = kdataService.getSseiRatio(date, previous_period);

				hlb.doIt(musters, date, sseiFlag);
				bdt.doIt(musters, date, sseiFlag);

				avb.doIt(musters, previous.get(0), date, sseiRatio);
				bhl.doIt(musters, previous.get(0), date, sseiRatio);

				//bav.doIt(musters, previous.get(0), date, sseiFlag);
				bav.doIt(musters, previous.get(0), date, sseiFlag, sseiRatio);
				dtb.doIt(musters, previous.get(0), date, sseiFlag, sseiRatio);
			}
		}
		
		Map<String, String> bavResult = bav.result();
		Map<String, String> bhlResult = bhl.result();
		Map<String, String> bdtResult = bdt.result();
		Map<String, String> dtbResult = dtb.result();

		Map<String, String> avbResult = avb.result();
		Map<String, String> hlbResult = hlb.result();
		
		turtleSimulationRepository.save("bav", bavResult.get("breakers"), bavResult.get("CSV"), bavResult.get("dailyAmount"));
		turtleSimulationRepository.save("bhl", bhlResult.get("breakers"), bhlResult.get("CSV"), bhlResult.get("dailyAmount"));
		turtleSimulationRepository.save("bdt", bdtResult.get("breakers"), bdtResult.get("CSV"), bdtResult.get("dailyAmount"));
		
		turtleSimulationRepository.save("dtb", dtbResult.get("breakers"), dtbResult.get("CSV"), dtbResult.get("dailyAmount"));
		turtleSimulationRepository.save("hlb", hlbResult.get("breakers"), hlbResult.get("CSV"), hlbResult.get("dailyAmount"));
		turtleSimulationRepository.save("avb", avbResult.get("breakers"), avbResult.get("CSV"), avbResult.get("dailyAmount"));
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
}