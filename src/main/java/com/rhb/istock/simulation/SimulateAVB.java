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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import com.rhb.istock.account.Account;
import com.rhb.istock.operation.AggressiveOperation;
import com.rhb.istock.producer.Producer;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Component("avb")
public class SimulateAVB {
	protected static final Logger logger = LoggerFactory.getLogger(SimulateAVB.class);

	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("b21Reco")
	Producer b21Reco;
	
	@Autowired
	@Qualifier("aggressiveOperation")
	AggressiveOperation aggressiveOperation;
	
	@Async("taskExecutor")
	public Future<String> run(LocalDate beginDate, LocalDate endDate) throws InterruptedException {
		BigDecimal initCash = new BigDecimal(1000000);
		Account account = new Account(initCash);
		Map<LocalDate, List<String>> operationList = b21Reco.getResults(beginDate, endDate);
		Map<String, String> operateResult = aggressiveOperation.run(account, operationList, beginDate, endDate);
		turtleSimulationRepository.save("avb", operateResult.get("breakers"), operateResult.get("CSV"), operateResult.get("dailyAmount"));		
		return new AsyncResult<String>("avb执行完毕");
	}

}