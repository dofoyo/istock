package com.rhb.istock.trade.hunt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

@Service("huntSimulation")
public class HuntSimulation {
	protected static final Logger logger = LoggerFactory.getLogger(HuntSimulation.class);

	@Value("${musterPath}")
	private String musterPath;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;
	
	Integer pool = 21;
	Integer top = 1;
	BigDecimal initCash = new BigDecimal(100000);
	BigDecimal quota = new BigDecimal(20000); //买入每只股票的定额
	
	
	/*
	 * 根据输入起止日期，系统模拟买入和卖出
	 */
	public void simulate(LocalDate beginDate, LocalDate endDate) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("hunt simulate from " + beginDate + " to " + endDate +" ......");

		Hunting hunting = new Hunting(initCash, quota);
		
		Map<String,Muster> musters;
		Set<Muster> hls;
		
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, date.toString());
			
			musters = kdataService.getMusters(date);
			
			//logger.info(date.toString());
			
			hls = this.getHLs(new ArrayList<Muster>(musters.values()));

			if(musters!=null && musters.size()>0) {
				hunting.doIt_plus(musters, hls, date);
				
			}
		}
		
		Map<String, String> resutl = hunting.result();
		
		turtleSimulationRepository.save("dtb", resutl.get("breakers"), resutl.get("CSV"), resutl.get("dailyAmount"));

		//System.out.println("simulate done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
	}
	
	
	private Set<Muster> getHLs(List<Muster> musters){
		Set<Muster> hls = new HashSet<Muster>();

		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				if(o1.getHLGap().compareTo(o2.getHLGap())==0) {
					return o1.getLNGap().compareTo(o2.getLNGap());
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				}
			}
		});

		Muster m;
/*		StringBuffer sb = new StringBuffer();
		for(int i=0; i<musters.size() && i<pool; i++) {
			m = musters.get(i);
			if(m.isPotential() && !m.isUpLimited() &&!m.isDownLimited()){
				sb.append(m.getItemName() + "(" + m.getHLGap() + "/" + m.getLNGap() + "),");
			}
		}
		
		logger.info(sb.toString());*/
		
		for(int i=0; i<musters.size() && i<pool; i++) {
			m = musters.get(i);
			if(m.isPotential() && !m.isUpLimited() && !m.isDownLimited() && m.getLNGap()<8){
				hls.add(m);
			}
			if(hls.size()>=top) {
				break;
			}
		}
		
		return hls;
	}
	
	
}