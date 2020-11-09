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

@Component("hlb")
public class SimulateHLB {
	protected static final Logger logger = LoggerFactory.getLogger(SimulateHLB.class);

	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("newbReco")
	Producer newbReco;

	@Autowired
	@Qualifier("aggressiveOperation")
	AggressiveOperation aggressiveOperation;
	
	
	BigDecimal initCash = new BigDecimal(1000000);
	
	@Async("taskExecutor")
	public Future<String> run(LocalDate beginDate, LocalDate endDate)  throws InterruptedException {
		BigDecimal initCash = new BigDecimal(1000000);
		Account account = new Account(initCash);
		Map<LocalDate, List<String>> operationList = newbReco.getResults(beginDate, endDate);
		Map<String, String> operateResult = aggressiveOperation.run(account, operationList, beginDate, endDate);
		turtleSimulationRepository.save("hlb", operateResult.get("breakers"), operateResult.get("CSV"), operateResult.get("dailyAmount"));
		return new AsyncResult<String>("hlb执行完毕");
	}
}