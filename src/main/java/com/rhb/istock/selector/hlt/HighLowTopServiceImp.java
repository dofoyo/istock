package com.rhb.istock.selector.hlt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

@Service("highLowTopServiceImp")
public class HighLowTopServiceImp implements HighLowTopService {
	@Value("${highLowTopsFile}")
	private String highLowTopsFile;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Override
	public List<String> getHighLowTops(Integer top) {
		List<String> ids = Arrays.asList(FileUtil.readTextFile(highLowTopsFile).split(","));
		return ids.subList(0, Math.min(top, ids.size()));
	}

	@Override
	public void generateHighLowTops() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate high low tops ......");
		
		List<HighLowGap> gaps = new ArrayList<HighLowGap>();
		
		Integer duration = 89;
		Kdata kdata;
		
		List<Item> items = itemService.getItems();
		
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, item.getItemID());
			kdata = kdataService.getDailyKdata(item.getItemID(),false);
			gaps.add(new HighLowGap(item.getItemID(),kdata.getHighLowGap(duration)));
		}
		
		Collections.sort(gaps);
		
		HighLowGap gap;
		StringBuffer sb = new StringBuffer();
		for(Iterator<HighLowGap> it = gaps.iterator(); it.hasNext();) {
			gap = it.next();
			//System.out.println(gap);
			sb.append(gap.getItemID());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);

		FileUtil.writeTextFile(highLowTopsFile, sb.toString(), false);
		System.out.println("generate high low tops done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          

	}
	
	class HighLowGap implements Comparable<HighLowGap>{
		String itemID;
		Integer highLowGap;
		
		public HighLowGap(String itemID, Integer highLowGap) {
			this.itemID = itemID;
			this.highLowGap = highLowGap;
		}
		
		public String getItemID() {
			return itemID;
		}

		public void setItemID(String itemID) {
			this.itemID = itemID;
		}

		public Integer getHighLowGap() {
			return highLowGap;
		}

		public void setHighLowGap(Integer highLowGap) {
			this.highLowGap = highLowGap;
		}

		@Override
		public int compareTo(HighLowGap o) {
			return this.highLowGap.compareTo(o.getHighLowGap());
		}

		@Override
		public String toString() {
			return "HighLowGap [itemID=" + itemID + ", highLowGap=" + highLowGap + "]";
		}
		
	}

}
