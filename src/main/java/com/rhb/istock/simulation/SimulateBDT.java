package com.rhb.istock.simulation;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import com.rhb.istock.operation.AggressiveOperation;
import com.rhb.istock.operation.ConservativeOperation;
import com.rhb.istock.producer.Producer;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Scope("prototype")
@Component("bdt")
public class SimulateBDT {
	protected static final Logger logger = LoggerFactory.getLogger(SimulateBDT.class);

	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("newbPlusL21")
	Producer producer;

	@Autowired
	@Qualifier("aggressiveOperation")
	AggressiveOperation aggressiveOperation;
	
	@Autowired
	@Qualifier("conservativeOperation")
	ConservativeOperation conservativeOperation;
	
	@Async("taskExecutor")
	public Future<String> run(LocalDate beginDate, LocalDate endDate, BigDecimal initCash, Integer top, boolean isAveValue, Integer quantityType, boolean isEvaluation)  throws Exception {
		Account account = new Account(initCash);
		Map<LocalDate, List<String>> operationList = producer.getResults(beginDate, endDate);
		Map<String, String> operateResult = conservativeOperation.run(account, operationList, beginDate, endDate, "bdt", top, isAveValue,quantityType);
		turtleSimulationRepository.save("bdt", operateResult.get("breakers"), operateResult.get("CSV"), operateResult.get("dailyAmount"), isEvaluation);
		return new AsyncResult<String>("bdt执行完毕");
	}
}