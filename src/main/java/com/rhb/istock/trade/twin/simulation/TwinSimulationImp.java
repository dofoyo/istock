package com.rhb.istock.trade.twin.simulation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.trade.twin.Twin;
import com.rhb.istock.trade.twin.repository.TwinRepository;

@Service("twinSimulationImp")
public class TwinSimulationImp implements TwinSimulation {
	@Value("${reportPath}")
	private String reportPath;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("twinRepositoryImp")
	TwinRepository twinRepository;
	
	Twin twin = null;

	boolean byCache = true;

	@Override
	public Map<String, String> simulate() {
		twin = new Twin();
		
		LocalDate beginDate = LocalDate.parse("2010-01-01");
		LocalDate endDate = LocalDate.parse("2019-01-01");
		
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		
		List<String> itemIDs;
		int i = 1;
		for(LocalDate date = beginDate; date.isBefore(endDate); date = date.plusDays(1)){
			Progress.show((int)days, i++, date.toString()+ "," );

			twin.clearDatas();
			for(String itemID : twin.getItemIDsOfHolds()) {
				//System.out.println("do drop..." + itemID);
				setDailyKdata(itemID, date); 	//放入beginDate之前的历史记录
				setLatestKdata(itemID, date); 	//放入当前记录
				twin.doDrop(itemID);
				twin.doStop(itemID);
			}
			
			itemIDs = twinRepository.getOpens().get(date);
			if(itemIDs!=null) {
				for(String itemID : itemIDs) {
					//System.out.println("do open..." + itemID);
					if(twin.isFull()) {
						//System.out.println("is full, break!");
						break;
					}else {
						twin.doOpen(itemID,date,kdataService.getKbar(itemID, date, byCache).getClose());
					}
				}			
			}
		}
		
		Map<String, String> result = twin.result();
		
		return result;
	}
	
	private void setDailyKdata(String itemID, LocalDate theDate) {
		Kbar kbar;
		Kdata kdata = kdataService.getDailyKdata(itemID, theDate, twin.getLongLine(), byCache);
		
		List<LocalDate> dates = kdata.getDates();
		for(LocalDate date : dates) {
			kbar = kdata.getBar(date);
			twin.addDailyData(itemID,date,kbar.getClose());
		}
	}
	
	private Kbar setLatestKdata(String itemID, LocalDate theDate) {
		Kbar kbar = kdataService.getKbar(itemID, theDate, byCache);
		if(kbar!=null) {
			twin.addDailyData(itemID, theDate, kbar.getClose());
		}
		return kbar;
	}

}
