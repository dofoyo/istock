package com.rhb.istock.evaluation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

	@GetMapping("/evaluation/evaluate")
	public ResponseContent<String> simulate(){
		
		LocalDate theBeginDate = LocalDate.parse("2017-01-01");
		LocalDate theEndDate = LocalDate.now();
		
		evaluation.evaluate(theBeginDate, theEndDate);

		return new ResponseContent<String>(ResponseEnum.SUCCESS, "");
		
	}	
	
	@GetMapping("/evaluation/busi/{type}/{bdate}/{edate}/{isHighest}")
	public ResponseContent<BusiView> getDailyMeans(
			@PathVariable(value="type") String type,
			@PathVariable(value="bdate") String bdate,
			@PathVariable(value="edate") String edate,
			@PathVariable(value="isHighest") String isHighest){

		BusiView view = null;

		LocalDate theBeginDate = null;
		LocalDate theEndDate = null;
		try{
			theBeginDate = LocalDate.parse(bdate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			theEndDate = LocalDate.parse(edate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}catch(Exception e){
			return new ResponseContent<BusiView>(ResponseEnum.ERROR, view);
		}
		
		//System.out.println(isHighest);
		
		if("1".equals(isHighest)) {
			//System.out.println("isHighest=1");
			view = evaluationService.getMaxBusiView(type, theBeginDate, theEndDate);
		}else {
			//System.out.println("isHighest=0");
			view = evaluationService.getBusiView(type, theBeginDate, theEndDate);
		}
	
		return new ResponseContent<BusiView>(ResponseEnum.SUCCESS, view);
	}

}
