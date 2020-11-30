package com.rhb.istock.trade.turtle.simulation.power;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.potential.Potential;
import com.rhb.istock.selector.potential.PotentialService;

@Service("powerSimulation")
public class PowerSimulation {
	@Value("${simulationPath}")
	private String simulationPath;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("potentialService")
	PotentialService potentialService;
	
	/*
	 * 买入：涨停突破，第二天继续上涨
	 * 卖出：跌破dropDuration均线
	 */
	public void simulate() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("power simulate begin......");

		//List<LocalDate> dates = kdataService.getLastMusterDates();
		List<LocalDate> dates = kdataService.getMusterDates();
		
		LocalDate beginDate = LocalDate.parse("2010-01-01");

		Map<String,Potential> potentials;
		Map<String,Muster> musters;
		Muster muster;

		PowerDomain pd = new PowerDomain();
		Set<String> powersID;
		
		int i=1;
		for(LocalDate date : dates) {
			Progress.show(dates.size(), i++, date.toString());
			if(date.isAfter(beginDate)) {
				musters = kdataService.getMusters(date);
				
				//买入或卖出
				powersID = new HashSet<String>(pd.getIDs());
				for(String itemID : powersID) {
					muster = musters.get(itemID);
					if(muster!=null) {
						pd.put(itemID, date, muster.getLatestPrice(),muster.isUpLimited(),muster.isDownLimited(), muster.isDropAve(21),muster.isDown());
					}
				}
				
				//加入候选
				potentials = potentialService.getPotentials(date);
				for(Map.Entry<String, Potential> entry : potentials.entrySet()) {
					if(entry.getValue().isBreaker() && entry.getValue().isUpLimited()) {
						pd.add(entry.getKey(),date,entry.getValue().getLatestPrice());
					}
				}					
			}
		}
		
		FileTools.writeTextFile(simulationPath + "/simulation_power.csv", pd.getResult(), false);
		
		System.out.println("power simulate done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
	}
	
	
}
