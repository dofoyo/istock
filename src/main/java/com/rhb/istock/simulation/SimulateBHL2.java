package com.rhb.istock.simulation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.rhb.istock.account.Account;
import com.rhb.istock.operation.Operation;
import com.rhb.istock.producer.Producer;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Scope("prototype")
@Component("bhl2")
public class SimulateBHL2 {
	protected static final Logger logger = LoggerFactory.getLogger(SimulateBHL2.class);

	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("newb")
	//@Qualifier("power")
	//@Qualifier("dimeNewbReco")
	//@Qualifier("sab89Favor")
	Producer producer;
	
	@Autowired
	@Qualifier("newbRup")
	Producer newbRup;	
	
	@Autowired
	@Qualifier("fourOperation")
	//@Qualifier("huntingOperationPlus")
	Operation operation;
	
	@Async("taskExecutor")
	public Future<String> run(LocalDate beginDate, LocalDate endDate, BigDecimal initCash, Integer top, boolean isAveValue, Integer quantityType, boolean isEvaluation)  throws Exception {
		//top = 1;
		Account account = new Account(initCash);
		Map<LocalDate, List<String>> operationList = this.getOperationList(beginDate, endDate);
		Map<String, String> operateResult = operation.run(account, operationList, beginDate, endDate, "bhl2", top, isAveValue,quantityType);
		turtleSimulationRepository.save("bhl2", operateResult.get("breakers"), operateResult.get("CSV"), operateResult.get("dailyAmount"), isEvaluation);
		return new AsyncResult<String>("bhl执行完毕");
	}
	
	private Map<LocalDate, List<String>> getOperationList(LocalDate beginDate, LocalDate endDate){
		Map<LocalDate, List<String>> results = new HashMap<LocalDate, List<String>>();
		Map<LocalDate, List<String>> newbs = producer.getResults(beginDate, endDate);
		Map<LocalDate, List<String>> newbRups = newbRup.getResults(beginDate, endDate);
		LocalDate date;
		List<String> ids, newbRups_ids;
		for(Map.Entry<LocalDate, List<String>> entry : newbs.entrySet()) {
			date = entry.getKey();
			ids = new ArrayList<String>();
			newbRups_ids = newbRups.get(date);
			for(String id : entry.getValue()) {
				if(newbRups_ids==null || !newbRups_ids.contains(id)) {
					ids.add(id);
				}
			}				

			results.put(date, ids);
		}
		return results;
	}
}