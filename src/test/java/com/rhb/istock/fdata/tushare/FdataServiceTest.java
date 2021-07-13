package com.rhb.istock.fdata.tushare;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataServiceImp;
import com.rhb.istock.kdata.Muster;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FdataServiceTest {
	@Autowired
	@Qualifier("fdataRepositoryTushare")
	FdataRepositoryTushare fdataRepositoryTushare;
	
	@Autowired
	@Qualifier("fdataServiceTushare")
	FdataServiceTushare fdataServiceTushare;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataServiceImp kdataServiceImp;

	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	//@Test
	public void getAdvReceiptsRates() {
		String period = "20191231";
		Map<String, Integer> advReceiptsRates = fdataServiceTushare.getAdvReceiptsRates(period);
		System.out.println(advReceiptsRates);
	}
	
	//@Test
	public void testGetFina() {
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
	
	//@Test
	public void getGrowModels() {
		String b_date = "20190331";
		String e_date = "20200331";
		Integer n = 1;
		
		List<String> models = fdataServiceTushare.getGrowModels(b_date, e_date,n);
		
		for(String model : models) {
			System.out.println(model);
		}
		
		System.out.println(models.size());
	}
	
	//@Test
	public void getGrowModel() {
		String itemID = "sz300022";
		String b_date = "20190331";
		String e_date = "20200331";
		Integer n = 1;
		
		GrowModel model = fdataServiceTushare.getGrowModel(itemID, "",b_date, e_date,n);
		
		System.out.println(model);
	}
	
	//@Test
	public void getFinaGrowthRatioInfo() {
		Set<String> ids = new HashSet<String>();
		ids.add("sz002426");
		
		Map<String,String> result = fdataServiceTushare.getFinaGrowthRatioInfo(ids);
		
		System.out.println(result);
	}
	
	//@Test
	public void getMakerNames() {
		Set<String> names = fdataServiceTushare.getMakerNames();
		System.out.println(names);
	}
	
	
	//@Test
	public void getFloatholders() {
		Map<String,Map<String,Integer>> hs = fdataServiceTushare.getFloatholders();
		System.out.println(hs);
	}
	
	@Test
	public void showOKs() {
		LocalDate date1 = LocalDate.parse("20190506", DateTimeFormatter.ofPattern("yyyyMMdd"));
		LocalDate date2 = LocalDate.parse("20200506", DateTimeFormatter.ofPattern("yyyyMMdd"));
		LocalDate date3 = LocalDate.parse("20210506", DateTimeFormatter.ofPattern("yyyyMMdd"));
		LocalDate date4 = LocalDate.parse("20210520", DateTimeFormatter.ofPattern("yyyyMMdd"));
		LocalDate[] dates = {date1,date2,date3,date4};
		Map<String,Mbp> result = this.getOKs(date1, date2);
		Map<String,Mbp> tmp;
		Mbp mbp;
		for(int i=1; i<dates.length-1; i++) {
			tmp = this.getOKs(dates[i], dates[i+1]);
			for(Map.Entry<String, Mbp> entry : tmp.entrySet()) {
				mbp = result.get(entry.getKey());
				if(mbp==null) {
					result.put(entry.getKey(), entry.getValue());
				}else {
					mbp.setDate2(entry.getValue().getDate2());
					mbp.setPrice2(entry.getValue().getPrice2());
					mbp.setPe2(entry.getValue().getPe2());
				}
			}
		}
		
		for(Map.Entry<String, Mbp> entry : result.entrySet()) {
			System.out.println(entry.getValue());
		}
		
		
		
	}
	
	private Map<String, Mbp> getOKs(LocalDate date1, LocalDate date2) {
		System.out.println("------------");
		System.out.println(date1);
		System.out.println(date2);
		Map<String,Mbp> result = new HashMap<String,Mbp>();
		Map<Integer,Set<String>> oks = fdataServiceTushare.getOks(date1);
		Item item;
		Integer year = date1.getYear()-1;
		System.out.println(year);
		Set<String> one = oks.get(year);
		Set<String> two = oks.get(year-1);
		Set<String> three = oks.get(year-2);
		//Set<String> s2016 = oks.get(2016);
		//Set<String> s2015 = oks.get(2015);
		if(one!=null && two!=null && three!=null) {
			for(String str : one) {
				if(two.contains(str) && three.contains(str) 
						//&& s2016.contains(str) && s2015.contains(str)
						) {
					item = itemService.getItem(str);
					result.put(str,new Mbp(str,item.getNameWithCAGR()));
				}
			}
			
			//System.out.println(result.size());
			Map<String,Muster> musters1 = kdataServiceImp.getMusters(date1);
			Map<String,Muster> musters2 = kdataServiceImp.getMusters(date2);
			Muster m1,m2;
			for(Map.Entry<String, Mbp> entry : result.entrySet()) {
				m1 = musters1.get(entry.getKey());
				m2 = musters2.get(entry.getKey());
				if(m1!=null) {
					entry.getValue().setPrice1(m1.getLatestPrice());
					entry.getValue().setDate1(date1);
					entry.getValue().setPe1(m1.getPe());
				}
				if(m2!=null) {
					entry.getValue().setPrice2(m2.getLatestPrice());
					entry.getValue().setDate2(date2);
					entry.getValue().setPe2(m2.getPe());
				}
				
				//System.out.println(entry.getValue());
			}			
		}else {
			System.out.println("one two three all null!!!");
		}

		
		return result;
	}
	
	class Mbp{
		private String id;
		private String name;
		private LocalDate date1;
		private BigDecimal price1;
		private BigDecimal pe1;
		private LocalDate date2;
		private BigDecimal price2;
		private BigDecimal pe2;
		public Mbp(String id, String name) {
			this.id = id;
			this.name = name;
		}
		
		public BigDecimal getPe1() {
			return pe1;
		}

		public BigDecimal getPe2() {
			return pe2;
		}

		public void setPe1(BigDecimal pe1) {
			this.pe1 = pe1;
		}

		public void setPe2(BigDecimal pe2) {
			this.pe2 = pe2;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public BigDecimal getPrice1() {
			return price1;
		}
		public BigDecimal getPrice2() {
			return price2;
		}
		public void setPrice1(BigDecimal price1) {
			this.price1 = price1;
		}
		public void setPrice2(BigDecimal price2) {
			this.price2 = price2;
		}
		
		public String getId() {
			return id;
		}
		public LocalDate getDate1() {
			return date1;
		}
		public LocalDate getDate2() {
			return date2;
		}
		public void setId(String id) {
			this.id = id;
		}
		public void setDate1(LocalDate date1) {
			this.date1 = date1;
		}
		public void setDate2(LocalDate date2) {
			this.date2 = date2;
		}
		public Integer getGrowthRate() {
			if(price1!=null && price2!=null) {
				return Functions.growthRate(price2, price1);
			}else {
				return null;
			}
		}
		@Override
		public String toString() {
			String str = String.format("%s,%s,%s,%.2f,%.2f,%s,%.2f,%.2f,%d", this.id,this.name,this.getDate1(),this.price1,this.pe1,this.getDate2(),this.price2,this.pe2,this.getGrowthRate());
			return str;
		}
		
		
		
	}
	
}
