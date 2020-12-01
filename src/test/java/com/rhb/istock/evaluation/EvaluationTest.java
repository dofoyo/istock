package com.rhb.istock.evaluation;

import java.time.LocalDate;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class EvaluationTest {
	@Autowired
	@Qualifier("evaluationService")
	EvaluationService evaluationService;


	@Autowired
	@Qualifier("evaluation")
	Evaluation evaluation;
	
	@Test
	public void getResults() {
		LocalDate bDate = LocalDate.parse("2020-03-23");
		LocalDate eDate = LocalDate.parse("2020-11-27");
		String type = "hlb";
		BusiView busi = evaluationService.getBusiView(type, bDate, eDate);
		
		System.out.println(busi);
	}
	
	
	//@Test
	public void evaluate() {
		LocalDate beginDate = LocalDate.parse("2020-03-23");
		LocalDate endDate = LocalDate.parse("2020-11-27");

		evaluation.evaluate(beginDate, endDate);
	}
}
