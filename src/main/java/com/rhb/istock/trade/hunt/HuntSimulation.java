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
	
	Integer maxGap = 8;
	Integer top = 1;
	BigDecimal initCash = new BigDecimal(1000000);
	
	
	/*
	 * 根据输入起止日期，系统模拟买入和卖出
	 */
	public void simulate(LocalDate beginDate, LocalDate endDate) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("hunt simulate from " + beginDate + " to " + endDate +" ......");

		Hunting hunting = new Hunting(initCash);
		
/*		Set<String> includeIndustrys = new HashSet<String>();
		includeIndustrys.add("银行");
		includeIndustrys.add("机场");
*/		//includeIndustrys.add("供气供热");
		//includeIndustrys.add("水力发电");
		//includeIndustrys.add("白酒");
		
		Map<String,Muster> musters;
		Set<Muster> hls;
		
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, date.toString());
			
			musters = kdataService.getMusters(date);
			
			hls = this.getHLs(new ArrayList<Muster>(musters.values()));

			if(musters!=null && musters.size()>0) {
				hunting.doIt_plus(musters, hls, date);
				
			}
		}
		
		Map<String, String> resutl = hunting.result();
		
		turtleSimulationRepository.save("dtb", resutl.get("breakers"), resutl.get("CSV"), resutl.get("dailyAmount"));

		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("hunt simulate 用时：" + used + "秒");          
		
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
		for(int i=0; i<musters.size(); i++) {
			m = musters.get(i);
			if(!m.isNewLowest() && !m.isUpLimited() && !m.isDownLimited() && m.getLNGap()<maxGap && m.isUp()){
				hls.add(m);
			}
			if(hls.size()>=top) {
				break;
			}
		}
		
		return hls;
	}
	
	private Set<String> includeItems(){
		String[] ids = new String[]{"sh600011",
				"sh600019",
				"sh600023",
				"sh600028",
				"sh600036",
				"sh600048",
				"sh600066",
				"sh600104",
				"sh600170",
				"sh600177",
				"sh600383",
				"sh600398",
				"sh600516",
				"sh600585",
				"sh600606",
				"sh600660",
				"sh600674",
				"sh600688",
				"sh600704",
				"sh600741",
				"sh600795",
				"sh600816",
				"sh600886",
				"sh600887",
				"sh600900",
				"sh601006",
				"sh601009",
				"sh601088",
				"sh601166",
				"sh601169",
				"sh601225",
				"sh601288",
				"sh601328",
				"sh601398",
				"sh601668",
				"sh601818",
				"sh601939",
				"sh601988",
				"sh601998",
				"sz000002",
				"sz000157",
				"sz000333",
				"sz000338",
				"sz000402",
				"sz000625",
				"sz000876",
				"sz000895",
				"sz002146",
				"sz002304",
				"sz002601"};
		
		Set<String> ss = new HashSet<String>();
		for(String id : ids) {
			ss.add(id);
		}
		
		return ss;
	}
}