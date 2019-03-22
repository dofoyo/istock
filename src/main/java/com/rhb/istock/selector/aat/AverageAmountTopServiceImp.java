package com.rhb.istock.selector.aat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;

@Service("averageAmountTopServiceImp")
public class AverageAmountTopServiceImp implements AverageAmountTopService{
	@Value("${avarageAmountTopsFile}")
	private String avarageAmountTopsFile;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Override
	public List<String> getAverageAmountTops(Integer top) {
		List<String> ids = Arrays.asList(FileUtil.readTextFile(avarageAmountTopsFile).split(","));
		return ids.subList(0, Math.min(top, ids.size()));
	}

	@Override
	public void generateAverageAmountTops() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate average amount tops ......");

		Integer duration = 89;
		List<String> ids = kdataService.getLatestDailyTop(100);
		generateAverageAmountTops(ids, duration);
		
		
		System.out.println("generate high low tops done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	
	private void generateAverageAmountTops(List<String> itemIDs, Integer duration) {
		TreeSet<Amount> tops = new TreeSet<Amount>();
		Amount amount = null;
		Kdata kdata;
		BigDecimal av;
		int d = 1;
		for(String itemID : itemIDs) {
			Progress.show(itemIDs.size(), d++, itemID);
			kdata = kdataService.getDailyKdata(itemID,true);
			av = kdata.getAvarageAmount(duration);
			amount = new Amount(itemID,av);
			tops.add(amount);
		}
		StringBuffer sb = new StringBuffer();
		for(Iterator<Amount> i = tops.iterator() ; i.hasNext();) {
			amount = i.next();
			sb.append(amount.getCode());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		
		FileUtil.writeTextFile(avarageAmountTopsFile, sb.toString(), false);
	}
	
	class Amount  implements Comparable<Amount>{
		@Override
		public String toString() {
			return "BarEntity [code=" + code + ", amount=" + amount + "]";
		}

		private String code;
		private BigDecimal amount = new BigDecimal(0);
		
		public Amount(String code) {
			this.code = code;
		}
		
		public Amount(String code, BigDecimal amount) {
			this.code = code;
			this.amount = amount;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public BigDecimal getAmount() {
			return amount;
		}
		
		public Integer getAmountInt() {
			return amount.divide(new BigDecimal(100000),BigDecimal.ROUND_HALF_UP).intValue();
		}

		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}
		
		@Override
		public int compareTo(Amount o) {
			return o.getAmount().compareTo(this.getAmount()); //倒叙
		}
	}


}
