package com.rhb.istock.fdata.tushare;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.comm.util.FileTools;
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
	public void getFina() {
		String itemID = "sh600519";
		String end_date = "20191231";
		Fina fina = fdataRepositoryTushare.getFina(itemID,end_date);
		System.out.println(fina);
	}
	
	//@Test
	public void getBalancesheets() {
		String itemID = "sh600519";
		Map<String,FinaBalancesheet> balancesheets = fdataRepositoryTushare.getBalancesheets(itemID);
		Map<String,FinaIncome> incoms = fdataRepositoryTushare.getIncomes(itemID);
		FinaBalancesheet balancesheet = balancesheets.get("20191231");
		FinaIncome income = incoms.get("20191231");
		System.out.println(balancesheet);
		System.out.println(income);
		
/*		for(Map.Entry<String, FinaBalancesheet> entry : balancesheets.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());

		}*/
/*		for(FinaBalancesheet balancesheet : balancesheets.values()) {
			System.out.println(balancesheet);
		}*/
	}
	
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
		String itemID = "sz300770";
		Map<String,FinaIncome> incomes = fdataRepositoryTushare.getIncomes(itemID);
		for(FinaIncome income : incomes.values()) {
			System.out.println(income);
		}
	}
	
	@Test
	public void getIndicators() {
		String itemID = "sh600901";
		Map<String,FinaIndicator> indicators = fdataRepositoryTushare.getIndicators(itemID, LocalDate.now());
		for(Map.Entry<String, FinaIndicator> entry : indicators.entrySet()) {
			System.out.println(entry.getKey() +": "+ entry.getValue());
		}
	}
	
	//@Test
	public void getIndicators1() {
		List<String> ids = itemService.getItemIDs();
		Map<String,FinaIndicator> indicators;
		FinaIndicator indicator;
		String date = "20201231";
		List<FinaIndicator> fis = new ArrayList<FinaIndicator>();
		int i=1;
		for(String id : ids) {
			Progress.show(ids.size(), i++, id);
			indicators = fdataRepositoryTushare.getIndicators(id, LocalDate.now());
			indicator = indicators.get(date);
			if(indicator!=null) {
				indicator.setItemID(id);
				fis.add(indicator);
			}
		}
		
		Collections.sort(fis, new Comparator<FinaIndicator>() {
			@Override
			public int compare(FinaIndicator o1, FinaIndicator o2) {
				return o2.getRoe().compareTo(o1.getRoe());
			}
			
		});
		
		System.out.println(fis.size());
		
		System.out.format("id,date,roe,profit_dedt,dt_netprofit_yoy,or_yoy,ocfps\n");
		for(FinaIndicator fi : fis) {
			//if(fi.getGrossprofit_margin().compareTo(BigDecimal.ZERO)==1) {
				System.out.format("%s, %s, %.2f, %.2f, %.2f, %.2f, %.3f\n", fi.getItemID(), fi.getEnd_date(), fi.getRoe(),fi.getProfit_dedt(),fi.getDt_netprofit_yoy(), fi.getOr_yoy(),fi.getOcfps());
			//}
		}
	}
	
	/*
	 * 	private BigDecimal profit_dedt;			//扣除非经常性损益后的净利润
	private BigDecimal grossprofit_margin;  //销售毛利率
	private BigDecimal op_yoy;  			//营业利润同比增长率(%)
	private BigDecimal ebt_yoy;  			//利润总额同比增长率(%)
	private BigDecimal netprofit_yoy; 		//归属母公司股东的净利润同比增长率(%)  -- 业绩预告
	private BigDecimal dt_netprofit_yoy; 	//归属母公司股东的净利润-扣除非经常损益同比增长率(%)
	private BigDecimal or_yoy; 				//营业收入同比增长率(%)
	private BigDecimal ocfps;				//每股经营活动产生的现金流量净额
	private BigDecimal roe;				//净资产收益率
	 */
	
	//@Test
	public void getIndicators2() {
		List<String> ids = itemService.getItemIDs();
		Map<String,FinaIndicator> indicators;
		FinaIndicator i1,i2,i3;
		String date1 = "20171231";
		String date2 = "20181231";
		String date3 = "20191231";
		List<FinaIndicator> fis = new ArrayList<FinaIndicator>();
		int i=1;
		for(String id : ids) {
			Progress.show(ids.size(), i++, id);
			indicators = fdataRepositoryTushare.getIndicators(id, LocalDate.now());
			i1 = indicators.get(date1);
			i2 = indicators.get(date2);
			i3 = indicators.get(date3);
			
			if(i1!=null && i2!=null && i3!=null
					&& i1.isOK() && i2.isOK() && i3.isOK()
					) {
				i3.setItemID(id);
				fis.add(i3);
			}
		}
		
		Collections.sort(fis, new Comparator<FinaIndicator>() {
			@Override
			public int compare(FinaIndicator o1, FinaIndicator o2) {
				return o2.getGrossprofit_margin().compareTo(o1.getGrossprofit_margin());
			}
			
		});
		
		System.out.println(fis.size());
		
		System.out.format("id,date,Grossprofit_margin,profit_dedt,dt_netprofit_yoy,or_yoy,ocfps\n");
		for(FinaIndicator fi : fis) {
			//if(fi.getGrossprofit_margin().compareTo(BigDecimal.ZERO)==1) {
				System.out.format("%s, %s, %.2f, %.2f, %.2f, %.2f, %.3f\n", fi.getItemID(), fi.getEnd_date(), fi.getGrossprofit_margin(),fi.getProfit_dedt(),fi.getDt_netprofit_yoy(), fi.getOr_yoy(),fi.getOcfps());
			//}
		}
	}
	
	//@Test
	public void getIndicators3() {
		Map<Integer,Set<String>> results = new HashMap<Integer,Set<String>>();
		List<String> ids = itemService.getItemIDs();
		int i=1;
		Set<Integer> years;
		Set<String> okIDs;
		for(String id : ids) {
			Progress.show(ids.size(), i++, id);
			years = this.getOKs(id);
			for(Integer year : years) {
				okIDs = results.get(year);
				if(okIDs == null) {
					okIDs = new HashSet<String>();
				}
				okIDs.add(id);
				results.put(year, okIDs);
			}
		}
		
		StringBuffer sb = new StringBuffer();
		for(Map.Entry<Integer, Set<String>> entry : results.entrySet()) {
			sb.append(entry.getKey());
			sb.append(",");
			for(String str : entry.getValue()) {
				sb.append(str);
				sb.append(",");
			}
			sb.replace(sb.length()-1, sb.length(), "\n");
		}

		String path = "D:\\dev\\istock-data\\fdata\\oks.txt";
		
		FileTools.writeTextFile(path, sb.toString(), false);
		
	}
	
	private Set<Integer> getOKs(String itemID){
		Set<Integer> years = new HashSet<Integer>();
		Map<String,FinaIndicator> indicators = fdataRepositoryTushare.getIndicators(itemID, LocalDate.now());
		FinaIndicator i1,i2,i3;
		for(int year=2020; year>=2009; year--) {
			String date1 = Integer.toString(year-3) + "1231";
			String date2 = Integer.toString(year-2) + "1231";
			String date3 = Integer.toString(year-1) + "1231";
			
			i1 = indicators.get(date1);
			i2 = indicators.get(date2);
			i3 = indicators.get(date3);
			
			if(i1!=null && i2!=null && i3!=null
					&& i1.isOK() && i2.isOK() && i3.isOK()
					) {
				years.add(year);
			}			
		}
		return years;
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
	
	//@Test
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
