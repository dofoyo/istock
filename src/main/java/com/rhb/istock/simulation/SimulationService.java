package com.rhb.istock.simulation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Service("simulationService")
public class SimulationService {
	protected static final Logger logger = LoggerFactory.getLogger(SimulationService.class);
	
	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;
	
	public Set<Hold> getHolds_x(String type, LocalDate date, boolean isAll){
		Set<Hold> results = new HashSet<Hold>();
		
		Map<String,Hold> holds = new HashMap<String,Hold>();
		Map<LocalDate, List<String>> holdsMap = turtleSimulationRepository.getHolds(type);
		List<String> holdsList = holdsMap.get(date);
		Hold hold;
		if(holdsList != null) {
			String[] holdStr;
			for(String str : holdsList) {
				holdStr = str.split(",");
				holds.put(holdStr[1], new Hold(holdStr[1] +","+ holdStr[2] +","+ holdStr[6] +","+ holdStr[0]));
			}
			for(Map.Entry<LocalDate, List<String>> entry : holdsMap.entrySet()) {
				if(entry.getKey().isBefore(date)) {
					for(String str : entry.getValue()) {
						holdStr = str.split(",");
						hold = holds.get(holdStr[1]);
						if(hold != null) {
							//System.out.println(str);
							hold.update(holdStr[1] +","+ holdStr[2] +","+ holdStr[6] +","+ holdStr[0]);
							if(!isAll && holdStr[7].equals("1")) {
								results.add(hold);
								holds.remove(holdStr[1]);
							}
						}
					}
				}
			}
		}
		
		results.addAll(holds.values());
		
		return results;
	}
	
	public Set<Hold> getHolds(String type, LocalDate date, boolean isAll){
		Set<Hold> results = new HashSet<Hold>();
		
		Map<LocalDate, List<String>> holdsFile = turtleSimulationRepository.getHolds(type);
		if(holdsFile.get(date)==null) return results;

		List<String> hold_ids = new ArrayList<String>();
		for(String str : holdsFile.get(date)) {
			hold_ids.add(str.split(",")[1]);
		}
		
		Map<String, List<String>> holdsMap = new HashMap<String,List<String>>();
		List<String> holdsMap_list;
		Hold hold;
		String[] hold_id, hs;
		for(Map.Entry<LocalDate, List<String>> entry : holdsFile.entrySet()) {
			if(entry.getKey().isBefore(date) || entry.getKey().equals(date)) {
				for(String str : entry.getValue()) {
					hs = str.split(",");
					if(hold_ids.contains(hs[1])){
						holdsMap_list = holdsMap.get(hs[1]);
						if(holdsMap_list==null) {
							holdsMap_list = new ArrayList<String>();
							holdsMap.put(hs[1], holdsMap_list);
						}else if(!isAll && hs[7].equals("1")) {
							holdsMap_list = new ArrayList<String>();
							holdsMap.put(hs[1], holdsMap_list);
						}
						holdsMap_list.add(str);
					}
				}
			}
		}
		
		//System.out.println(holdsMap);

		Map<String,Hold> holds = new HashMap<String,Hold>();
		for(Map.Entry<String, List<String>> entry : holdsMap.entrySet()) {
			for(String str : entry.getValue()) {
				hold_id = str.split(",");
				hold = holds.get(hold_id[1]);
				if(hold != null) {
					hold.update(hold_id[1] +","+ hold_id[2] +","+ hold_id[6] +","+ hold_id[0]);
				}else {
					hold = new Hold(hold_id[1] +","+ hold_id[2] +","+ hold_id[6] +","+ hold_id[0]);
					holds.put(hold_id[1], hold);
				}
			}
		}
		
		results.addAll(holds.values());
		
		return results;
	}

	
}