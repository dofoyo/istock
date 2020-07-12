package com.rhb.istock.fdata.tushare;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FdataRepositoryTushareTest {

	@Autowired
	@Qualifier("fdataRepositoryTushare")
	FdataRepositoryTushare fdataRepositoryTushare;
	
	//@Test
	public void getCashflows() {
		String itemID = "sz300022";
		Map<String,FinaCashflow> cashflows = fdataRepositoryTushare.getCashflows(itemID);
		for(FinaCashflow cashflow : cashflows.values()) {
			System.out.println(cashflow);
		}
		
	}
	
	
	//@Test
	public void getIncomes() {
		String itemID = "sz300022";
		Map<String,FinaIncome> incomes = fdataRepositoryTushare.getIncomes(itemID);
		for(FinaIncome income : incomes.values()) {
			System.out.println(income);
		}
	}
	
	@Test
	public void getIndicators() {
		String itemID = "sz300022";
		Map<String,FinaIndicator> indicators = fdataRepositoryTushare.getIndicators(itemID);
		for(Map.Entry<String, FinaIndicator> entry : indicators.entrySet()) {
			System.out.println(entry.getKey() +": "+ entry.getValue());
		}
	}
	
	//@Test
	public void getFina() {
		String itemID = "sz300022";
		String end_date = "20191231";
		Fina fina = fdataRepositoryTushare.getFina(itemID,end_date);
		System.out.println(fina);
		
	}
	
	//@Test
	public void getForecasts() {
		String itemID = "sz002077";
		Map<String,FinaForecast> forecasts = fdataRepositoryTushare.getForecasts(itemID);
		for(FinaForecast forecast : forecasts.values()) {
			System.out.println(forecast);
		}
	}
}
