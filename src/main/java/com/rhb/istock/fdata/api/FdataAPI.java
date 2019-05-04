package com.rhb.istock.fdata.api;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.istock.comm.api.ResponseContent;
import com.rhb.istock.comm.api.ResponseEnum;
import com.rhb.istock.fdata.FinancialStatement;
import com.rhb.istock.fdata.FinancialStatementService;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;

@RestController
public class FdataAPI {
	@Autowired
	@Qualifier("financialStatementServiceImp")
	FinancialStatementService financialStatementService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@GetMapping("/fdatas/{itemID}")
	public ResponseContent<FdatasView> getFdatas(@PathVariable(value="itemID") String itemID) {
		//System.out.println(itemID);
		Item item = itemService.getItem(itemID);
		FdatasView view = new FdatasView(item.getCode(),item.getName(),item.getIndustry());
		
		FinancialStatement fs = financialStatementService.getFinancialStatement(item.getCode());
		List<String> periods = new ArrayList<String>(fs.getBalancesheets().keySet());
		Collections.sort(periods);
		Double revenue, profit, cash;
		DecimalFormat df = new DecimalFormat("#.00");
		for(String period : periods) {
			revenue = fs.getProfitstatements().get(period).getOperatingRevenue()/100000000;
			profit = fs.getProfitstatements().get(period).getProfit()/100000000;
			cash = fs.getCashflows().get(period).getNetCashFlow()/100000000;
			view.add(period.substring(0,4), df.format(revenue), df.format(profit), df.format(cash));
		}
		
		return new ResponseContent<FdatasView>(ResponseEnum.SUCCESS, view);
	}

}
