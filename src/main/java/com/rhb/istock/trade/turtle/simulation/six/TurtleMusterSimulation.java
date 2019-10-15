package com.rhb.istock.trade.turtle.simulation.six;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	
	Integer industry_potential_count = 21;
	Integer industry_hot = 8;
	Integer pool = 21;
	Integer top = 5;
	BigDecimal initCash = new BigDecimal(100000);
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
		Paul avbPaul = new Paul(initCash, quota);
		//Paul dtbPaul = new Paul(initCash, quota);
		Paul hlbPaul = new Paul(initCash, quota);
		
		Map<String,Muster> musters;
		List<Muster> breakers;
		Map<String,Integer> industryHots;
		Integer industry_hot;
		//IndustryPotentials ips;
		
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, date.toString());
			
			//ips = new IndustryPotentials();

			musters = kdataService.getMusters(date);
			breakers = new ArrayList<Muster>();
			industryHots = new HashMap<String,Integer>();
			for(Muster m : musters.values()) {
				if(m.isPotential()) {
					industry_hot = industryHots.get(m.getIndustry());
					if(industry_hot == null) {
						industryHots.put(m.getIndustry(), 1);
					}else {
						industry_hot = industry_hot + 1;
						industryHots.put(m.getIndustry(), industry_hot);
					}						
				}
				
				//ips.put(m);
				
				if(m.isBreaker()) {
					breakers.add(m);
				}
			}
			
			//logger.info(date.toString());
			
			if(musters!=null && musters.size()>0) {
				bavPaul.doIt_plus(musters, this.getBxxTops(breakers, "bav"), date);
				bhlPaul.doIt_plus(musters, this.getBxxTops(breakers, "bhl"), date);
				bdtPaul.doIt_plus(musters, this.getBxxTops(breakers, "bdt"), date);
				
				avbPaul.doIt_plus(musters, this.getxxBTops(new ArrayList<Muster>(musters.values()), "avb"), date);
				//dtbPaul.doIt_plus(musters, this.getxxBTops(new ArrayList<Muster>(musters.values()), "dtb"), date);
				hlbPaul.doIt_plus(musters, this.getHLBTops(new ArrayList<Muster>(musters.values()),industryHots), date);
				
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
		
		if(breakers.size()>top) {
			//System.out.println("breakers.size() = " + breakers.size());
			return breakers.subList(0, top);
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
		
		if(breakers.size()>top) {
			return breakers.subList(0, top);
		}else {
			return breakers;
		}
	}
	
	private List<Muster> getHLBTops(List<Muster> musters,Map<String,Integer> industryHots){
		List<Muster> breakers = new ArrayList<Muster>();

		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
/*				if(o1.getHLGap().compareTo(o2.getHLGap())==0) {
					return o2.getHNGap().compareTo(o1.getHNGap());
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				}	*/
				return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
			}
		});

		Muster m;
		for(int i=0; i<musters.size() && i<pool; i++) {
			m = musters.get(i);
			if(m.isBreaker()
					//&& industryHots.get(m.getIndustry())>industry_hot
					){
				m.setIndustry_hot(industryHots.get(m.getIndustry()));
				breakers.add(m);
				//logger.info(m.toString());
			}
		}

/*		Collections.sort(breakers, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o2.getIndustry_hot().compareTo(o1.getIndustry_hot());
			}
		});*/
		
		
		if(breakers.size()>top) {
			return breakers.subList(0, top);
		}else {
			return breakers;
		}
	}
	
	class IndustryPotentials {
		Map<String,List<Muster>> potentials = new HashMap<String,List<Muster>>();
		List tmp;
		public void put(Muster muster) {
			if(muster.isPotential()) {
				tmp = potentials.get(muster.getIndustry()); 
				if(tmp == null) {
					tmp = new ArrayList<Muster>();
					potentials.put(muster.getIndustry(), tmp);
				}
				tmp.add(muster);
			}
		}
		
		public List<Muster> getBreakers(){
			List<Muster> breakers = new ArrayList<Muster>();
			for(Map.Entry<String, List<Muster>> entry : potentials.entrySet()) {
				if(entry.getValue().size()>industry_potential_count) {
					StringBuffer sb = new StringBuffer();
					for(Muster m : entry.getValue()) {
						if(m.isBreaker()) {
							breakers.add(m);
							sb.append(m.getItemName());
							sb.append(",");
						}
					}
					if(sb.length()>0) {
						//logger.info(String.format("%s: %d potentials, breakers:%s",  entry.getKey(),entry.getValue().size(),sb.toString()));
					}
				}
			}
			
			Collections.sort(breakers, new Comparator<Muster>() {
				@Override
				public int compare(Muster o1, Muster o2) {
					return o1.getHLGap().compareTo(o2.getHLGap());
				}
				
			});
			
			return breakers.subList(0, breakers.size()>top ? top : breakers.size());
		}
	}
	
}