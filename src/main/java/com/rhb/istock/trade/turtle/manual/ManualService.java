package com.rhb.istock.trade.turtle.manual;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.account.Account;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.index.tushare.IndexServiceTushare;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.operation.AggressiveOperation;
import com.rhb.istock.operation.ConservativeOperation;
import com.rhb.istock.trade.turtle.simulation.six.TurtleMusterSimulation;
import com.rhb.istock.trade.turtle.simulation.six.repository.AmountEntity;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Service("manualService")
public class ManualService {
	protected static final Logger logger = LoggerFactory.getLogger(TurtleMusterSimulation.class);

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("indexServiceTushare")
	IndexServiceTushare indexServiceTushare;

	@Autowired
	@Qualifier("aggressiveOperation")
	AggressiveOperation aggressiveOperation;
	
	@Autowired
	@Qualifier("conservativeOperation")
	ConservativeOperation conservativeOperation;
	
	private Map<LocalDate, List<String>> selects = new TreeMap<LocalDate, List<String>>();

	public void addSelects(LocalDate date, String itemID) {
		List<String> ids = this.selects.get(date);
		if(ids==null) {
			ids = new ArrayList<String>();
			ids.add(itemID);
			this.selects.put(date, ids);
		}else if(ids!=null && !ids.contains(itemID)) {
			ids .add(itemID);
		}
	}
	
	public void deleteSelects(LocalDate date) {
		this.selects.remove(date);
	}
	
	public Map<LocalDate, String> getSelects(LocalDate date){
		Map<LocalDate, String> ss = new TreeMap<LocalDate, String>();
		List<String> ids;
		for(Map.Entry<LocalDate, List<String>> entry : this.selects.entrySet()) {
			if(entry.getKey().isBefore(date) || entry.getKey().isEqual(date)) {
				ids = entry.getValue();
				if(ids!=null && ids.size()>0) {
					ss.put(entry.getKey(), ids.get(0));
				}
			}
		}
		
		return ss;
	}
	
	public Map<LocalDate, List<String>> getReselect(){
		this.selects  = turtleSimulationRepository.getBreakers("manual");
		return this.selects;
	}
	
	public void simulate(String simulateType) {
		long beginTime=System.currentTimeMillis(); 
		String label = "manual " + simulateType;
		BigDecimal initCash = new BigDecimal(1000000);
		Account account = new Account(initCash);
		Map<String, String> operateResult;
		if("aggressive".equals(simulateType)) {
			operateResult = aggressiveOperation.run(account, this.selects, this.getBeginDate(), this.getEndDate(), label);
		}else {
			operateResult = conservativeOperation.run(account, this.selects, this.getBeginDate(), this.getEndDate(), label);
		}
		turtleSimulationRepository.save("manual", operateResult.get("breakers"), operateResult.get("CSV"), operateResult.get("dailyAmount"));
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("manual simulate 用时：" + used + "秒");          
	}
	
	private LocalDate getBeginDate() {
		TreeMap<LocalDate, AmountEntity> amounts = turtleSimulationRepository.getAmounts("avb");
		return amounts.firstKey();
	}
	private LocalDate getEndDate() {
		TreeMap<LocalDate, AmountEntity> amounts = turtleSimulationRepository.getAmounts("avb");
		return amounts.lastKey();
	}
}
