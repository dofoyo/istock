package com.rhb.istock.trade.turtle.simulation.six;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
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
	
	Integer pool = 21;
	Integer bTop = 3;
	Integer topB = 5;
	BigDecimal initCash = new BigDecimal(1000000);
	
	/*
	 * 根据输入起止日期，系统模拟买入和卖出
	 */
	public void simulate(LocalDate beginDate, LocalDate endDate) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("simulate from " + beginDate + " to " + endDate +" ......");

		Bxx bavPaul = new Bxx(initCash);
		Bxx bhlPaul = new Bxx(initCash);
		Bxx bdtPaul = new Bxx(initCash);
		XxB avbPaul = new XxB(initCash);
		//Paul dtbPaul = new Paul(initCash, quota);
		XxB hlbPaul = new XxB(initCash);
		
		Map<String,Muster> musters;
		List<Muster> breakers;
		
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, date.toString());
			
			breakers = new ArrayList<Muster>();
			
			musters = kdataService.getMusters(date);
			for(Muster m : musters.values()) {
				if(m.isBreaker()) {
					breakers.add(m);
				}
			}
			
			if(musters!=null && musters.size()>0) {
				bavPaul.doIt(musters, this.getBxxTops(breakers, "bav"), date);
				bhlPaul.doIt(musters, this.getBxxTops(breakers, "bhl"), date);
				bdtPaul.doIt(musters, this.getBxxTops(breakers, "bdt"), date);
				
				avbPaul.doIt_plus(musters, this.getxxBTops(new ArrayList<Muster>(musters.values()), "avb"), date);
				//dtbPaul.doIt_plus(musters, this.getxxBTops(new ArrayList<Muster>(musters.values()), "dtb"), date);
				hlbPaul.doIt_plus(musters, this.getxxBTops(new ArrayList<Muster>(musters.values()), "hlb"), date);
				
			}
		}
		
		Map<String, String> bavResult = bavPaul.result();
		Map<String, String> bhlResult = bhlPaul.result();
		Map<String, String> bdtResult = bdtPaul.result();

		Map<String, String> avbResult = avbPaul.result();
		//Map<String, String> dtbResult = dtbPaul.result();
		Map<String, String> hlbResult = hlbPaul.result();

		
		turtleSimulationRepository.save("bav", bavResult.get("breakers"), bavResult.get("CSV"), bavResult.get("dailyAmount"));
		turtleSimulationRepository.save("bhl", bhlResult.get("breakers"), bhlResult.get("CSV"), bhlResult.get("dailyAmount"));
		turtleSimulationRepository.save("bdt", bdtResult.get("breakers"), bdtResult.get("CSV"), bdtResult.get("dailyAmount"));

		turtleSimulationRepository.save("avb", avbResult.get("breakers"), avbResult.get("CSV"), avbResult.get("dailyAmount"));
		//turtleSimulationRepository.save("dtb", dtbResult.get("breakers"), dtbResult.get("CSV"), dtbResult.get("dailyAmount"));
		turtleSimulationRepository.save("hlb", hlbResult.get("breakers"), hlbResult.get("CSV"), hlbResult.get("dailyAmount"));

		//System.out.println("simulate done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
	}
	
	//breakers中选av，hl，dt
	private List<Muster> getBxxTops(List<Muster> breakers,String type){
		Collections.sort(breakers, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				if(type.equals("bav")) return o2.getAverageAmount().compareTo(o1.getAverageAmount()); //Z-A
				if(type.equals("bdt")) return o2.getAmount().compareTo(o1.getAmount());//Z-A
				if(type.equals("bhl")){
					if(o1.getHLGap().compareTo(o2.getHLGap())==0) {
						return o1.getLatestPrice().compareTo(o2.getLatestPrice());
					}else {
						return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
					}
				}
				return 0;
			}
		});
		
		if(breakers.size()>bTop) {
			return breakers.subList(0, bTop);
		}else {
			return breakers;
		}
	}
	
	private List<Muster> getxxBTops(List<Muster> musters,String type){
		List<Muster> breakers = new ArrayList<Muster>();

		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				if(type.equals("avb")) return o2.getAverageAmount().compareTo(o1.getAverageAmount()); //Z-A
				if(type.equals("dtb")) return o2.getAmount().compareTo(o1.getAmount());//Z-A
				if(type.equals("hlb")) return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				return 0;
			}
		});

		Muster m;
		for(int i=0; i<musters.size() && i<pool; i++) {
			m = musters.get(i);
			if(m.isBreaker()) {
				breakers.add(m);
			}
		}
		
		if(breakers.size()>topB) {
			return breakers.subList(0, topB);
		}else {
			return breakers;
		}
	}
	
}