package com.rhb.istock.producer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.evaluation.EvaluationService;
import com.rhb.istock.evaluation.KellyView;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.selector.fina.FinaService;

/*
 * 新高
 */

@Service("eva")
public class Eva implements Producer{
	protected static final Logger logger = LoggerFactory.getLogger(Eva.class);

	@Autowired
	@Qualifier("evaluationService")
	EvaluationService evaluationService;

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("finaService")
	FinaService finaService;
	
	@Value("${operationsPath}")
	private String operationsPath;
	
	private String fileName  = "Eva.txt";
	
	@Override
	public Map<LocalDate, List<String>> produce(LocalDate bDate, LocalDate eDate) {
		Map<LocalDate, List<String>> results = new TreeMap<LocalDate, List<String>>();
		List<String> breakers;
		
		long days = eDate.toEpochDay()- bDate.toEpochDay();
		int i=1;
		for(LocalDate date = bDate; (date.isBefore(eDate) || date.equals(eDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, fileName + ", " + date.toString());
			breakers = this.produce(date,false);
			if(breakers!=null && breakers.size()>0) {
				results.put(date, breakers);
			}
		}
		
		FileTools.writeMapFile(this.getFileName(), results, false);
		
		return results;
	}

	@Override
	public Map<LocalDate, List<String>> getResults(LocalDate bDate, LocalDate eDate) {
		Map<LocalDate, List<String>> all = FileTools.readMapFile(this.getFileName());
		
		Map<LocalDate, List<String>> results = new TreeMap<LocalDate, List<String>>();
		LocalDate date;
		for(Map.Entry<LocalDate, List<String>> entry : all.entrySet()) {
			date = entry.getKey();
			if((date.isAfter(bDate) || date.equals(bDate))
					&& (date.isBefore(eDate) || date.equals(eDate))) {
				results.put(date, entry.getValue());
			}
		}
		
		return results;
	}
	
	private String getFileName() {
		return operationsPath + fileName;
	}

	@Override
	public List<String> getResults(LocalDate date) {
		Map<LocalDate, List<String>> all = FileTools.readMapFile(this.getFileName());
		if(all.get(date)!=null) {
			return all.get(date);
		}else {
			return this.produce(date, false);
		}
	}

	@Override
	public List<String> produce(LocalDate date, boolean write) {
		List<String> breakers = new ArrayList<String>();
		
		Integer period = 8;
		KellyView avbkv = evaluationService.getKellyView("avb", period, date);
		KellyView bavkv = evaluationService.getKellyView("bav", period, date);
		KellyView bdtkv = evaluationService.getKellyView("bdt", period, date);
		if(avbkv!=null && bavkv!=null && bdtkv!=null) {
			Integer avbk = avbkv.getScore();
			Integer bavk = bavkv.getScore();
			Integer bdtk = bdtkv.getScore();

			
			if(bdtk>0 && bdtk>=avbk && bdtk>=bavk){
				breakers.addAll(evaluationService.getOpenIds("bdt", date));
			}else if(bavk>0 && bavk>=avbk && bavk>=bdtk){
				breakers.addAll(evaluationService.getOpenIds("bav", date));
			}else if(avbk>0 && avbk>=bavk && avbk>=bdtk) {
				breakers.addAll(evaluationService.getOpenIds("avb", date));
			}
			
		}
				
		return breakers;
	}
}
