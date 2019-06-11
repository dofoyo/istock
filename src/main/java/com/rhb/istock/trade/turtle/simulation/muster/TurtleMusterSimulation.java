package com.rhb.istock.trade.turtle.simulation.muster;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.trade.turtle.simulation.repository.TurtleSimulationRepository;

@Service("turtleMusterSimulation")
public class TurtleMusterSimulation {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;
	
	Integer top = 3;
	BigDecimal initCash = new BigDecimal(2000000);
	BigDecimal quota = new BigDecimal(20000); //买入每只股票的定额
	
	
	/*
	 * 根据输入起止日期，系统模拟买入和卖出
	 */
	public void simulate(LocalDate beginDate, LocalDate endDate) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("simulate from " + beginDate + " to " + endDate +" ......");

		Paul bavPaul = new Paul(initCash, quota);
		Paul bhlPaul = new Paul(initCash, quota);
		Paul bdtPaul = new Paul(initCash, quota);
		
		List<Muster> musters;
		Map<String,Muster> musterMap;
		
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		for(LocalDate date = beginDate; date.isBefore(endDate); date = date.plusDays(1)) {
			Progress.show((int)days, i++, date.toString());
			
			musters = kdataService.getMusters(date);
			if(musters!=null && musters.size()>0) {
				musterMap = musters.stream().collect(Collectors.toMap(Muster::getItemID, Function.identity()));
				
				bavPaul.doIt(musterMap, this.getBxxTops(musters, "bav"), date);
				bhlPaul.doIt(musterMap, this.getBxxTops(musters, "bhl"), date);
				bdtPaul.doIt(musterMap, this.getBxxTops(musters, "bdt"), date);
			}
		}
		
		Map<String, String> bavResult = bavPaul.result();
		Map<String, String> bhlResult = bhlPaul.result();
		Map<String, String> bdtResult = bdtPaul.result();
		
		turtleSimulationRepository.save("bav", bavResult.get("breakers"), bavResult.get("CSV"), bavResult.get("dailyAmount"));
		turtleSimulationRepository.save("bhl", bhlResult.get("breakers"), bhlResult.get("CSV"), bhlResult.get("dailyAmount"));
		turtleSimulationRepository.save("bdt", bdtResult.get("breakers"), bdtResult.get("CSV"), bdtResult.get("dailyAmount"));
		
		System.out.println("generateLastMusters done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
	}
	
	private List<Muster> getBxxTops(List<Muster> musters,String type){
		//要根据最新收盘价，判断是否是breaker，还是买入、卖出的价格
		List<Muster> breakers = new ArrayList<Muster>();
		for(Muster m : musters) {
			if(m.isBreaker()) {
				breakers.add(m);
			}
		}
		
		Collections.sort(breakers, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				if(type.equals("bav")) return o2.getAverageAmount().compareTo(o1.getAverageAmount()); //Z-A
				if(type.equals("bdt")) return o2.getAmount().compareTo(o1.getAmount());//Z-A
				if(type.equals("bhl")) return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				return 0;
			}
		});
		
		if(breakers.size()>top) {
			//System.out.println("breakers.size() = " + breakers.size());
			return breakers.subList(0, top);
		}else {
			return breakers;
		}
		
	}
}
