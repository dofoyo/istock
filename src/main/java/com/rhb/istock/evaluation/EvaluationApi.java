package com.rhb.istock.evaluation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.istock.comm.api.ResponseContent;
import com.rhb.istock.comm.api.ResponseEnum;

@RestController
public class EvaluationApi {
	@Autowired
	@Qualifier("evaluationService")
	EvaluationService evaluationService;

	@Autowired
	@Qualifier("evaluation")
	Evaluation evaluation;

	protected static final Logger logger = LoggerFactory.getLogger(EvaluationApi.class);

	@GetMapping("/evaluation/evaluate")
	public ResponseContent<String> simulate(){
		
		LocalDate theBeginDate = LocalDate.parse("2017-01-01");
		LocalDate theEndDate = LocalDate.now();
		
		evaluation.evaluate(theBeginDate, theEndDate);

		return new ResponseContent<String>(ResponseEnum.SUCCESS, "");
		
	}
	
	@GetMapping("/evaluation/kellies/{period}/{edate}")
	public ResponseContent<KelliesView> getKelliesView(
			@PathVariable(value="period") Integer period,
			@PathVariable(value="edate") String edate
			){
		LocalDate theEndDate = LocalDate.parse(edate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		KelliesView view = evaluationService.getKelliesView(period, theEndDate);
		return new ResponseContent<KelliesView>(ResponseEnum.SUCCESS, view);
	}
	
	@GetMapping("/evaluation/kelly/{type}/{period}/{edate}/{isHighest}")
	public ResponseContent<KellyView> getKellyView(
			@PathVariable(value="type") String type,
			@PathVariable(value="period") Integer period,
			@PathVariable(value="edate") String edate,
			@PathVariable(value="isHighest") String isHighest){

		KellyView view = null;

		LocalDate theEndDate = LocalDate.parse(edate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		//System.out.println(theEndDate);
		//System.out.println(isHighest);
		
		if("1".equals(isHighest)) {
			//System.out.println("isHighest=1");
			view = evaluationService.getMaxKellyView(type, period, theEndDate);
		}else {
			//System.out.println("isHighest=0");
			view = evaluationService.getKellyView(type, period, theEndDate);
		}
	
		return new ResponseContent<KellyView>(ResponseEnum.SUCCESS, view);
	}
	
	@GetMapping("/evaluation/busis/{type}/{period}/{edate}")
	public ResponseContent<List<BusiView>> getBusis(
			@PathVariable(value="type") String type,
			@PathVariable(value="period") Integer period,
			@PathVariable(value="edate") String edate
			){

		List<BusiView> busis = evaluationService.getBusiViews(type, period, LocalDate.parse(edate, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
	
		//logger.info(busis.toString());
		
		return new ResponseContent<List<BusiView>>(ResponseEnum.SUCCESS, busis);
	}

}
