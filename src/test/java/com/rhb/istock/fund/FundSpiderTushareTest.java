package com.rhb.istock.fund;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.comm.util.Progress;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FundSpiderTushareTest {
	@Autowired
	@Qualifier("fundSpiderTushare")
	FundSpiderTushare fundSpiderTushare;

	@Autowired
	@Qualifier("fundRepositoryTushare")
	FundRepositoryTushare fundRepositoryTushare;

	@Autowired
	@Qualifier("fundServiceTushare")
	FundServiceTushare fundServiceTushare;
	
	//@Test
	public void downFundBasic() {
		fundSpiderTushare.downFundBasic();
	}
	
	//@Test
	public void getFundBasics() {
		Set<FundBasic> basics = fundRepositoryTushare.getFundBasics();
		Set<String> types = new HashSet<String>();
		int i=1;
		for(FundBasic basic : basics) {
			types.add(basic.getFund_type());
			System.out.println(i++ + "：　" +basic);
		}
		
		for(String type : types) {
			System.out.println(type);
		}
		
		int j=1;
		for(FundBasic basic : basics) {
			if((basic.getFund_type().equals("股票型")
				|| basic.getFund_type().equals("混合型"))
					&& !basic.getStatus().equals("D")
				){
				System.out.println(j++ + "：　" +basic);
			}
		}
	}
	
	//@Test
	public void downFundPortfolio() {
		String ts_code = "005035.OF";
		fundSpiderTushare.downFundPortfolio(ts_code);
	}
	
	//@Test
	public void getFundPortfolios() {
		String ts_code = "005035.OF";
		List<FundPortfolio> basics = new ArrayList<FundPortfolio>(fundRepositoryTushare.getFundPortfolios(ts_code));
		
		Collections.sort(basics, new Comparator<FundPortfolio>() {

			@Override
			public int compare(FundPortfolio o1, FundPortfolio o2) {
				if(o1.getEnd_date().compareTo(o2.getEnd_date())==0) {
					return o1.getStk_mkv_ratio().compareTo(o2.getStk_mkv_ratio());
				}
				return o1.getEnd_date().compareTo(o2.getEnd_date());
			}
			
		});
		
		int i=1;
		for(FundPortfolio basic : basics) {
			System.out.println(i++ + "：　" +basic);
		}
	}
	
	//@Test
	public void downFundPortfolioes() {
		Set<FundBasic> basics = fundRepositoryTushare.getFundBasics();
		int i=1;
		for(FundBasic basic : basics) {
			Progress.show(basics.size(), i++, basic.getName());
			if(basic.getFund_type().equals("股票型") || basic.getFund_type().equals("混合型")){
				fundSpiderTushare.downFundPortfolio(basic.getTs_code());	
				try {
					Thread.sleep(1000);  //一分钟200个	
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}
		}
	}
	
	@Test
	public void getItemPortfolioes() {
		String period = "20200331";
		List<ItemPortfolio> itemPortfolioes = fundServiceTushare.getItemPortfolioes(period);
		int i=1;
		for(ItemPortfolio itemPortfolio : itemPortfolioes) {
			System.out.println(i++ + ": " + itemPortfolio.getItemID());
		}
	}
}
