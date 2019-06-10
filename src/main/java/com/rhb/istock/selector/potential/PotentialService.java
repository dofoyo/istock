package com.rhb.istock.selector.potential;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.kdata.KdataService;

@Service("potentialService")
public class PotentialService {
	@Value("${latestPotentialsFile}")
	private String latestPotentialsFile;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	public List<Potential> getLastPotentials(){
		List<Potential> potentials = new ArrayList<Potential>();
		Potential potential;
		
		List<Muster> musters = kdataService.getLastMusters();
		
		for(Muster item : musters) {
			potential =  new Potential(item.getItemID(),
					item.getAmount(),
					item.getAverageAmount(),
					item.getHighest(),
					item.getLowest(),
					item.getPrice(),
					item.getPrice());
			if(potential.getHNGap()<10) {
				//System.out.println(potential);
				potentials.add(potential);	
			}					
		}

		return potentials;
	}

	
	/*
	 * 上一交易日的收盘数据要等开盘前才能下载到，因为涉及到除权的调整
	 * 因此收盘后的各统计用的是最后一天的实时行情，如果某只股票正在除权，会失真
	 * latestPotentials仅供盘后分析用
	 * 每日开盘后，系统会在9:30生成potentials，供实盘操作用
	 */
	public void generateLatestPotentials() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate latest potentials with latest kdata......");
		
		List<String> news = new ArrayList<String>();
		List<String> olds = new ArrayList<String>();
		List<String> outs = this.getLatestPotentials();
		
		Map<String,Object> features;

		LocalDate date;
		Kdata kdata;
		Integer count = 55;
		List<Item> items = itemService.getItems();
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, item.getItemID());
			kdata = kdataService.getKdata(item.getItemID(),false);
			date = kdataService.getLatestMarketDate();
			if(kdata.getBar(date)==null) {
				kdata.addBar(date, kdataService.getLatestMarketData(item.getItemID()));
			}
			features = kdata.getPotentialFeatures(count);

			if((Integer)features.get("hnGap")<10) {
				if(outs.contains(item.getItemID())) {
					olds.add(item.getItemID());
				}else {
					news.add(item.getItemID());
				}
				outs.remove(item.getItemID());
			}
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(getLine("news",news));
		sb.append(getLine("olds",olds));
		sb.append(getLine("outs",outs));
		
		//System.out.println(sb.toString());
		FileTools.writeTextFile(latestPotentialsFile, sb.toString(), false);
		
		System.out.println("generate latest potentials with latest kdata done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	private String getLine(String str, List<String> ids) {
		StringBuffer sb = new StringBuffer();
		sb.append(str + ",");
		for(String id : ids) {
			sb.append(id);
			sb.append(",");				
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("\n");
		return sb.toString();
	}
	

	public List<String> getLatestPotentials() {
		List<String> potentials = new ArrayList<String>();
		
		String[] lines = FileTools.readTextFile(latestPotentialsFile).split("\n");
		String[] columns;
		for(String line : lines) {
			columns = line.split(",");
			if((columns[0].equals("news") || columns[0].equals("olds")) && columns.length>1) {
				for(int i=1; i<columns.length; i++) {
					potentials.add(columns[i]);
				}
			}
		}		
		return potentials;
	}

}
