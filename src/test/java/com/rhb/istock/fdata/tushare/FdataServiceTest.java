package com.rhb.istock.fdata.tushare;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.comm.util.Functions;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FdataServiceTest {
	@Autowired
	@Qualifier("fdataRepositoryTushare")
	FdataRepositoryTushare fdataRepositoryTushare;
	
	@Autowired
	@Qualifier("fdataServiceTushare")
	FdataServiceTushare fdataServiceTushare;
	
	
	//@Test
	public void test() {
		String end_date1 = "20161231";
		String end_date2 = "20191231";
		Integer n = 3;
		
		String itemID = "sh600519";
		Fina fina1 = fdataRepositoryTushare.getFina(itemID, end_date1);
		Fina fina2 = fdataRepositoryTushare.getFina(itemID, end_date2);
		
		Integer revenueRatio = Functions.cagr(fina2.getIncome().getRevenue(), fina1.getIncome().getRevenue(),n);
		Integer cashflowRatio = Functions.cagr(fina2.getCashflow().getN_cashflow_act(), fina1.getCashflow().getN_cashflow_act(),n);
		Integer profitRatio = Functions.cagr(fina2.getIndicator().getProfit_dedt(), fina1.getIndicator().getProfit_dedt(),n);
		
		System.out.println(fina1);
		System.out.println(fina2);
		System.out.println(String.format("revenue:%d, cashflow:%d, profit:%d", revenueRatio,cashflowRatio, profitRatio));
		System.out.println(String.format("ratio1=profit/revenue=%d", fina1.getOperationMarginRatio()));
		System.out.println(String.format("ratio2=profit/revenue=%d", fina2.getOperationMarginRatio()));
	}
	
	@Test
	public void getGrowModels() {
		String b_date = "20161231";
		String e_date = "20191231";
		Integer n = 3;
		
		List<GrowModel> models = fdataServiceTushare.getGrowModels(b_date, e_date,n);
		
		for(GrowModel model : models) {
			System.out.println(model);
		}
		
		System.out.println(models.size());
	}
}
