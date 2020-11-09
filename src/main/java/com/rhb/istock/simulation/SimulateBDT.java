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
import com.rhb.istock.operation.ConservativeOperation;
import com.rhb.istock.producer.Producer;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Component("bdt")
public class SimulateBDT {
	protected static final Logger logger = LoggerFactory.getLogger(SimulateBDT.class);

	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("newbPlus")
	Producer newbPlus;

	@Autowired
	@Qualifier("conservativeOperation")
	ConservativeOperation conservativeOperation;
	
	BigDecimal initCash = new BigDecimal(1000000);
	
	@Async("taskExecutor")
	public Future<String> run(LocalDate beginDate, LocalDate endDate)  throws InterruptedException {
		BigDecimal initCash = new BigDecimal(1000000);
		Account account = new Account(initCash);
		Map<LocalDate, List<String>> operationList = newbPlus.getResults(beginDate, endDate);
		Map<String, String> operateResult = conservativeOperation.run(account, operationList, beginDate, endDate);
		turtleSimulationRepository.save("bdt", operateResult.get("breakers"), operateResult.get("CSV"), operateResult.get("dailyAmount"));
		return new AsyncResult<String>("bdt执行完毕");
	}
}