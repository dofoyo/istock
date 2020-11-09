package com.rhb.istock.fdata.sina;

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
import com.rhb.istock.fdata.sina.FinancialStatement;
import com.rhb.istock.fdata.sina.FinancialStatementService;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FinancialStatementServiceTest {
	@Autowired
	@Qualifier("financialStatementServiceImp")
	FinancialStatementService financialStatementService;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	//@Test
	public void downloadReports() {
		financialStatementService.downloadAllReports();
	}
	
	/*
		1）19年营业总收入每季同比增长率（与18年同期）均大于0，且Q3增长率>Q2增长率>Q1增长率；
		2）19年每季报的利润（营业总收入-营业成本-营业税金及附加-销售费用-管理费用-财务费用-研发费用）大于0，
			且每季同比增长率（与18年同期）均大于0，且Q3增长率>Q2增长率>Q1增长率。
	 */
	//@Test
	public void test() {
		String code = "601398";
		boolean flag = this.doit(code);
		if(flag) {
			System.out.println("YES");
		}else {
			System.out.println("NO");
		}
	}
	
	private boolean doit(String code) {
		FinancialStatement fs;
		Double[] revenue_2019;
		Double[] revenue_2018;
		Double[] revenue_q;

		Double[] profit_2019;
		Double[] profit_2018;
		Double[] profit_q;

		
		fs = financialStatementService.getFinancialStatement(code);
		if(fs==null 
				|| fs.getProfitstatements()==null 
				|| fs.getProfitstatements().get("20190331")==null
				|| fs.getProfitstatements().get("20190630")==null
				|| fs.getProfitstatements().get("20190930")==null
				|| fs.getProfitstatements().get("20180331")==null
				|| fs.getProfitstatements().get("20180630")==null
				|| fs.getProfitstatements().get("20180930")==null
				) {
			return false;
		}
		revenue_2019 = new Double[3];
		revenue_2019[0] = fs.getProfitstatements().get("20190331").getOperatingRevenue();
		revenue_2019[1] = fs.getProfitstatements().get("20190630").getOperatingRevenue();
		revenue_2019[2] = fs.getProfitstatements().get("20190930").getOperatingRevenue();

		revenue_2018 = new Double[3];
		revenue_2018[0] = fs.getProfitstatements().get("20180331").getOperatingRevenue();
		revenue_2018[1] = fs.getProfitstatements().get("20180630").getOperatingRevenue();
		revenue_2018[2] = fs.getProfitstatements().get("20180930").getOperatingRevenue();

		revenue_q = new Double[3];
		revenue_q[0] = revenue_2019[0]/revenue_2018[0] - 1;
		revenue_q[1] = revenue_2019[1]/revenue_2018[1] - 1;
		revenue_q[2] = revenue_2019[2]/revenue_2018[2] - 1;
		
		System.out.printf("revenue_q = 2019Q1/2018Q1 = %f/%f = %.2f\n", revenue_2019[0],revenue_2018[0],revenue_q[0]);
		System.out.printf("revenue_q = 2019Q2/2018Q2 = %f/%f = %.2f\n", revenue_2019[1],revenue_2018[1],revenue_q[1]);
		System.out.printf("revenue_q = 2019Q3/2018Q3 = %f/%f = %.2f\n", revenue_2019[2],revenue_2018[2],revenue_q[2]);

		//----
		profit_2019 = new Double[3];
		profit_2019[0] = fs.getProfitstatements().get("20190331").getProfit();
		profit_2019[1] = fs.getProfitstatements().get("20190630").getProfit();
		profit_2019[2] = fs.getProfitstatements().get("20190930").getProfit();

		profit_2018 = new Double[3];
		profit_2018[0] = fs.getProfitstatements().get("20180331").getProfit();
		profit_2018[1] = fs.getProfitstatements().get("20180630").getProfit();
		profit_2018[2] = fs.getProfitstatements().get("20180930").getProfit();

		profit_q = new Double[3];
		profit_q[0] = profit_2019[0]/profit_2018[0] - 1;
		profit_q[1] = profit_2019[1]/profit_2018[1] - 1;
		profit_q[2] = profit_2019[2]/profit_2018[2] - 1;
		
		System.out.printf("profit_q = 2019Q1/2018Q1 = %f/%f = %.2f\n", profit_2019[0],profit_2018[0],profit_q[0]);
		System.out.printf("profit_q = 2019Q2/2018Q2 = %f/%f = %.2f\n", profit_2019[1],profit_2018[1],profit_q[1]);
		System.out.printf("profit_q = 2019Q3/2018Q3 = %f/%f = %.2f\n", profit_2019[2],profit_2018[2],profit_q[2]);

		if(revenue_q[2]>revenue_q[1] && revenue_q[1]>revenue_q[0] && revenue_q[0]>0
				&& profit_q[2]>profit_q[1] && profit_q[1]>profit_q[0] && profit_q[0]>0
				&& profit_2019[0]>0 && profit_2019[1]>0 && profit_2019[2]>0
				) {
			return true;
		}else {
			return false;
		}
	}
	
	/*
	 *  1）2017年12月1日前上市；
		2）19年营业总收入每季同比增长率（与18年同期）均大于0，且Q3增长率>Q2增长率>Q1增长率；
		3）19年每季报的利润（营业总收入-营业成本-营业税金及附加-销售费用-管理费用-财务费用-研发费用）大于0，
			且每季同比增长率（与18年同期）均大于0，且Q3增长率>Q2增长率>Q1增长率。
	 */
	
	@Test
	public void find() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("begin find ......");

		List<String> itemIDs = itemService.getItemIDs();
		Item item;
		boolean flag;
		Integer year;
		Set<Item> oks = new HashSet<Item>();
		
		int i=1;
		for(String id : itemIDs) {
			Progress.show(itemIDs.size(), i++, id);

			item = itemService.getItem(id);
			//System.out.println(item.getIpo());
			year = Integer.parseInt(item.getIpo().substring(0, 4));
			if(year<2018) {
				flag = this.doit(item.getCode());
				if(flag) {
					oks.add(item);
				}
			}
		}
		
		for(Item it : oks) {
			System.out.println(it.getCode() + it.getName());
		}
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          

	}
	
	/*
	 *  1.每股扣非收益0.3元以上
		2.今年半年扣非净利润较去年同比增长20%以上
		3.资产负债率不高于50%
		4.商誉/总资产<5%
		5.总市值大于100亿
	 */
	
	
	
}
