package com.rhb.istock.selector.lpb;

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

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;

@Service("lpbService")
public class LpbService {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Value("${lpbFile}")
	private String lpbFile;	
	
	protected static final Logger logger = LoggerFactory.getLogger(LpbService.class);
	
	public void generateLPB(LocalDate endDate, Integer days) {
		List<LocalDate> dates = kdataService.getMusterDates(days, endDate);
		StringBuffer sb = new StringBuffer();
		for(LocalDate date : dates) {
			sb.append(this.generateLPB(date));
		}
		FileTools.writeTextFile(lpbFile, sb.toString(), false);
	}
	
	public String getLpb() {
		List<LocalDate> dates = kdataService.getLastMusterDates();
		LocalDate date = dates.get(dates.size()-1);
		return this.generateLPB(date);
	}
	
	public String generateLPB(LocalDate endDate) {
		Map<String,Muster> musters = kdataService.getMusters(endDate);
		Integer previous_period = 8;
		StringBuffer sb = new StringBuffer(endDate.toString() + ":");
		if(musters!=null) {
			List<LocalDate> previousDates = kdataService.getMusterDates(previous_period, endDate);
			if(previousDates!=null) {
				Map<String,Muster> previous = kdataService.getMusters(previousDates.get(0));
				//Integer sseiFlag = kdataService.getSseiFlag(endDate);
				//List<String> hs300 = itemService.getHs300(endDate);
				
				List<Muster>  ms = new ArrayList<Muster>(musters.values());
				
				Collections.sort(ms, new Comparator<Muster>() {
					@Override
					public int compare(Muster o1, Muster o2) {
						return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //Z-A
					}
				});
				
				Muster m,p;
				for(int i=0; i<ms.size(); i++) {
					m = ms.get(i);
					p = previous.get(m.getItemID());
					if(m!=null && p!=null
							&& m.getPe().compareTo(BigDecimal.ZERO)>0 && m.getPe().compareTo(new BigDecimal(233))<0
							&& !m.isUpLimited() 
							&& !m.isDownLimited() 
							&& m.isJustBreaker(8)
							//&& m.getHLGap()<=55
							&& m.getAverageAmount().compareTo(p.getAverageAmount())==1
							) {
						sb.append(m.getItemID() + "(" + String.format("%04d",i) + ")" +",");
					}
				}
				
				sb.append("\n");
				
			}else {
				sb.append(" No previous!");
			}
		}else {
			sb.append(" No muster!");
		}
		
		//logger.info(sb.toString());
		
		return sb.toString();
	}
}
