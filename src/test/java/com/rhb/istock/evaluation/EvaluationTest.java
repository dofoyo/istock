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
		Integer period = 5;
		LocalDate eDate = LocalDate.parse("2020-12-15");
		String type = "bhl";
		
		KelliesView views = evaluationService.getKelliesView(period, eDate);
		System.out.println(views);
		System.out.println("**********");

		KellyView vies = evaluationService.getKellyView(type, period, eDate);		
		System.out.println(vies);

	}
	
	
	//@Test
	public void evaluate() {
		LocalDate beginDate = LocalDate.parse("2017-01-01");
		LocalDate endDate = LocalDate.parse("2020-12-14");

		evaluation.evaluate(beginDate, endDate);
	}
	
	//@Test
	public void getKelliesView() {
		LocalDate endDate = LocalDate.parse("2020-12-15");
/*		Map<LocalDate, Kelly> kellies = evaluationService.getKellies("bav",endDate);
		for(Map.Entry<LocalDate, Kelly> entry : kellies.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());
		}
*/	}
}
