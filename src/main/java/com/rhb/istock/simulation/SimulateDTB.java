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
import com.rhb.istock.operation.ConservativeOperation;
import com.rhb.istock.producer.Producer;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Scope("prototype")
@Component("dtb")
public class SimulateDTB {
	protected static final Logger logger = LoggerFactory.getLogger(SimulateDTB.class);

	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;
	
	@Autowired
	@Qualifier("drumPlus")
	Producer drumPlus;
	
	@Autowired
	@Qualifier("conservativeOperation")
	ConservativeOperation conservativeOperation;
	
	@Async("taskExecutor")
	public Future<String> run(LocalDate beginDate, LocalDate endDate)  throws InterruptedException {
		BigDecimal initCash = new BigDecimal(1000000);
		Account account = new Account(initCash);
		Integer top = 1;
		Map<LocalDate, List<String>> operationList = drumPlus.getResults(beginDate, endDate);
		Map<String, String> operateResult = conservativeOperation.run(account, operationList, beginDate, endDate,"dtb", top);
		turtleSimulationRepository.save("dtb", operateResult.get("breakers"), operateResult.get("CSV"), operateResult.get("dailyAmount"));
		return new AsyncResult<String>("dtb执行完毕");
	}
}