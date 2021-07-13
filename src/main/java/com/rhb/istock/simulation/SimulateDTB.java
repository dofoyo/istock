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
import com.rhb.istock.operation.Operation;
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
	//@Qualifier("drumPlusL21")
	@Qualifier("oks")  //测试
	Producer producer;
	
	@Autowired
	//@Qualifier("conservativeOperation")
	@Qualifier("oksOperation2") //测试
	Operation operation;
	
	@Async("taskExecutor")
	public Future<String> run(LocalDate beginDate, LocalDate endDate, BigDecimal initCash, Integer top, boolean isAveValue, Integer quantityType, boolean isEvaluation)  throws Exception {
		Account account = new Account(initCash);
		Map<LocalDate, List<String>> operationList = producer.getResults(beginDate, endDate);
		Map<String, String> operateResult = operation.run(account, operationList,null, beginDate, endDate,"dtb", top, isAveValue,quantityType);
		turtleSimulationRepository.save("dtb", operateResult.get("breakers"), operateResult.get("CSV"), operateResult.get("dailyAmount"), operateResult.get("dailyHolds"), isEvaluation);
		return new AsyncResult<String>("dtb执行完毕");
	}
}