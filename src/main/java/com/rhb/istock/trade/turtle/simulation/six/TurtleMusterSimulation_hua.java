package com.rhb.istock.trade.turtle.simulation.six;

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

import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.hua.HuaService;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Service("turtleMusterSimulation_hua")
public class TurtleMusterSimulation_hua {
	protected static final Logger logger = LoggerFactory.getLogger(TurtleMusterSimulation_hua.class);

	@Value("${musterPath}")
	private String musterPath;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("huaService")
	HuaService huaService;
	
	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;
	
	BigDecimal initCash = new BigDecimal(1000000);
	Integer tops = 2;
	Integer pools = 21;
	/*
	 * 根据输入起止日期，系统模拟买入和卖出
	 */
	public void simulate(LocalDate beginDate, LocalDate endDate) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("simulate hua from " + beginDate + " to " + endDate +" ......");
		boolean isEvaluation = false;

		Hua dtb = new Hua(initCash);

		//List<LocalDate> dates = kdataService.getMusterDates();

		Map<String,Muster> musters;
		Integer sseiFlag;

		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, "  simulate hua: " + date.toString());

			musters = kdataService.getMusters(date);
			if(musters!=null && musters.size()>0) {
				sseiFlag = kdataService.getSseiFlag(date);
				dtb.doIt(musters, this.getIDs(musters, date), date, sseiFlag);
			}
		}
		
		Map<String, String> dtbResult = dtb.result();
		
		turtleSimulationRepository.save("dtb", dtbResult.get("breakers"), dtbResult.get("CSV"), dtbResult.get("dailyAmount"), dtbResult.get("dailyHolds"), isEvaluation);

		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	private Set<String> getIDs(Map<String,Muster> musters, LocalDate date){
		Set<String> ids = new HashSet<String>();
		List<String> hua_ids = huaService.getHua(date);
		Muster muster;
		if(hua_ids!=null && !hua_ids.isEmpty()) {
			List<Muster> ls = new ArrayList<Muster>();
			for(String id : hua_ids) {
				muster = musters.get(id);
				if(muster!=null) {
					ls.add(muster);
				}
			}
			
			Collections.sort(ls, new Comparator<Muster>() {
				@Override
				public int compare(Muster o1, Muster o2) {
					return o2.getVolume_ratio().compareTo(o1.getVolume_ratio());
				}
			});
			
			for(int i=0,j=0; j<tops && i<pools && i<ls.size(); i++) {
				muster = ls.get(i);
				//logger.info(String.format("\n%tF,%s,%.2f,%.2f", date,muster.getItemName(),muster.getClose(), muster.getVolume_ratio()));
				if(!muster.isUpLimited() 
						&& muster.isUp(21) 
						&& muster.getLatestPrice().compareTo(muster.getClose())==1  //上涨
						&& Functions.between(muster.getVolume_ratio(), 3, 13)
						&& Functions.growthRate(muster.getLatestPrice(), muster.getAveragePrice21())<=13
						) {
					//logger.info(" ok\n");
					ids.add(muster.getItemID());
					j++;
				}else {
					//logger.info("\n");
				}
			}
		}
		
		return ids;
	}
	
	
}