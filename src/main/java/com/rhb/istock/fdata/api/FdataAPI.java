package com.rhb.istock.fdata.api;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.istock.comm.api.ResponseContent;
import com.rhb.istock.comm.api.ResponseEnum;
import com.rhb.istock.fdata.eastmoney.FdataRepositoryEastmoney;
import com.rhb.istock.fdata.sina.FinancialStatementService;
import com.rhb.istock.fdata.tushare.FdataRepositoryTushare;
import com.rhb.istock.fdata.tushare.Fina;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;

@RestController
public class FdataAPI {
	@Autowired
	@Qualifier("financialStatementServiceImp")
	FinancialStatementService financialStatementService;

	@Autowired
	@Qualifier("fdataRepositoryTushare")
	FdataRepositoryTushare fdataRepositoryTushare;

	@Autowired
	@Qualifier("fdataRepositoryEastmoney")
	FdataRepositoryEastmoney fdataRepositoryEastmoney;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@GetMapping("/recommendations/{itemID}/{date}")
	public ResponseContent<RecommendationsView> getRecommendations(
			@PathVariable(value="itemID") String itemID, 
			@PathVariable(value="date") String date 
		){
		RecommendationsView view = null;
		
		Item item = itemService.getItem(itemID);
		if(item!=null) {
			view = new RecommendationsView(item.getCode(),item.getName(),item.getIndustry());
			Map<LocalDate,Integer> count = fdataRepositoryEastmoney.getRecommendationCount(itemID, LocalDate.parse(date));
			for(Map.Entry<LocalDate, Integer> entry : count.entrySet()) {
				view.add(entry.getKey().toString(), entry.getValue().toString());
			}
		}else {
			System.out.println("Can NOT find " + itemID + "!!!");
		}
		
		return new ResponseContent<RecommendationsView>(ResponseEnum.SUCCESS, view);
	}
	
	@GetMapping("/fdatas/{itemID}")
	public ResponseContent<FdatasView> getFdatas(@PathVariable(value="itemID") String itemID) {
		//System.out.println(itemID);
		FdatasView view = null;
		
		Item item = itemService.getItem(itemID);
		
		if(item!=null) {
			view = new FdatasView(item.getCode(),item.getName(),item.getIndustry());
			Double revenue, profit, cash;
			DecimalFormat df = new DecimalFormat("#.00");
			double a = 100000000;
			
			Map<String,Fina> finas = fdataRepositoryTushare.getFinas(itemID);
			for(Map.Entry<String, Fina> entry : finas.entrySet()) {
				if(entry.getValue()!=null && entry.getValue().isValid()) {
					revenue = entry.getValue().getIncome().getRevenue().doubleValue()/a;
					profit = entry.getValue().getIndicator().getProfit_dedt().doubleValue()/a;
					cash = entry.getValue().getCashflow().getN_cashflow_act().doubleValue()/a;
					view.add(entry.getKey().substring(0,4), df.format(revenue), df.format(profit), df.format(cash));
				}
				fdataRepositoryTushare.init();
			}
			
			Map<String,String[]> forcasts = fdataRepositoryEastmoney.getForcasts(itemID);
			//System.out.println(forcasts.size());
			String rq, yyzrs, yylr;
			for(Map.Entry<String, String[]> entry : forcasts.entrySet()) {
				rq = entry.getKey();
				yyzrs = entry.getValue()[0];
				yylr = entry.getValue()[1];
				view.add(rq, yyzrs, yylr, yylr);
				//System.out.println(rq + "," + yyzrs +  ", " + yylr );
			}
			
		}else {
			System.out.println("Can NOT find " + itemID + "!!!");
		}
		
		return new ResponseContent<FdatasView>(ResponseEnum.SUCCESS, view);
	}

}
