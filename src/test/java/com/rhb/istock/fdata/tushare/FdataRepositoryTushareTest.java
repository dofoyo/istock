package com.rhb.istock.fdata.tushare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.ItemService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FdataRepositoryTushareTest {

	@Autowired
	@Qualifier("fdataRepositoryTushare")
	FdataRepositoryTushare fdataRepositoryTushare;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
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
	
	//@Test
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
	
	//@Test
	public void getFloatholders() {
		String itemID = "sh603399";
		String[] end_dates = {"20200331","20191231"};

		Set<Floatholder> holders = fdataRepositoryTushare.getFloatholders(itemID, end_dates);
		for(Floatholder holder : holders) {
			System.out.println(holder);
		}
	}
	
	@Test
	public void getHolders() {
		Map<String,Integer> holderIds = new HashMap<String,Integer>();
		String str = "潘宇红";
		Set<Floatholder> holders;
		//List<String> ids = itemService.getItemIDs();
		List<String> ids = new ArrayList<String>();
		ids.add("sz002428");
		String[] end_dates = {"20200331","20191231"};
		Integer count, i=1;
		for(String id : ids) {
			Progress.show(ids.size(),i++, " getFloatholders: " + id);//进度条
			holders = fdataRepositoryTushare.getFloatholders(id, end_dates);
			for(Floatholder holder : holders) {
				//System.out.print(holder.getHolder_name());
				if(holder.getHolder_name().contains(str)) {
					//System.out.println(",yes");
					count = holderIds.get(holder.getTs_code());
					if(count == null) {
						count = 1;
					}else {
						count ++;
					}
					holderIds.put(holder.getTs_code(), count);
				}else {
					//System.out.println(",no");
				}
			}			
		}
		
		for(Map.Entry<String, Integer> entry : holderIds.entrySet()) {
			System.out.println(entry.getKey() + ", " + entry.getValue());
		}
		
	}
	
}
