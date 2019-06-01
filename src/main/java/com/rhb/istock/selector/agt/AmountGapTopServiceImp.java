package com.rhb.istock.selector.agt;

import java.time.LocalDate;
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
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;

@Service("amountGapTopServiceImp")
public class AmountGapTopServiceImp implements AmountGapTopService {
	@Value("${amountGapsFile}")
	private String amountGapsFile;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Override
	public void generateAmountGaps() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate amount tops ......");

		Integer top = 100;
		Integer duration = 55; //与openduration一致

		AmountGap amountGap = null;
		TreeSet<AmountGap> amountGaps = new TreeSet<AmountGap>();

		boolean byCache = false;
		Kdata kdata;
		LocalDate date;
		
		List<Item> items = itemService.getItems();
		int d = 1;
		for(Item item : items) {
			Progress.show(items.size(), d++, item.getCode());
			
			date = kdataService.getLatestMarketDate();
			kdata = kdataService.getDailyKdata(item.getItemID(), date, duration*2, byCache);
			if(kdata.getSize() == duration*2) {
				amountGap = new AmountGap(item.getItemID(),kdata.getTotalAmounts());
				amountGaps.add(amountGap);				
			}
		}
		
		StringBuffer sb = new StringBuffer();
		for(Iterator<AmountGap> i = amountGaps.iterator() ; i.hasNext();) {
			amountGap = i.next();
			sb.append(amountGap.getItemID());
			sb.append(",");
			//System.out.print(bar.getCode() + "(" + bar.getAva() + "),");
		}
		sb.deleteCharAt(sb.length()-1);
		
		//System.out.println("\n");
		
		FileUtil.writeTextFile(amountGapsFile, sb.toString(), false);

		System.out.println("generate amount gap done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}

	@Override
	public List<String> getAmountGapTops(Integer top) {
		List<String> ids = Arrays.asList(FileUtil.readTextFile(amountGapsFile).split(","));
		return ids.subList(0, Math.min(top, ids.size()));
	}

}
