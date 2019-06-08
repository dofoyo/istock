package com.rhb.istock.selector.potential;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataMuster;
import com.rhb.istock.kdata.KdataService;

@Service("potentialService")
public class PotentialService {
	@Value("${latestPotentialsFile}")
	private String latestPotentialsFile;
	
	@Value("${tmpLatestPotentialsFile}")
	private String tmpLatestPotentialsFile;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	List<Potential> latestPotentials = null;
	LocalDate latestPotentialsDate = null;
	
	public void init() {
		String source = FileUtil.readTextFile(latestPotentialsFile);
		
		if(source==null || source.isEmpty()) {
			System.err.println("can NOT find " + latestPotentialsFile + "! or the file is empty!");
			return;
		}
		
		this.latestPotentials = new ArrayList<Potential>();
		
		String[] lines = source.split("\n");
		this.latestPotentialsDate = LocalDate.parse(lines[0]);
		
		for(int i=1; i<lines.length; i++) {
			this.latestPotentials.add(new Potential(lines[i]));
		}			

	}
	
	public LocalDate getLatestPotentialDate() {
		if(this.latestPotentialsDate==null) {
			this.init();
		}
		return this.latestPotentialsDate;
	}
	
	public List<String> getLatestPotentialIDs(){
		List<String> ids = new ArrayList<String>();

		List<Potential> potentials = this.getLatestPotentials();
		for(Potential p : potentials) {
			ids.add(p.getItemID());
		}
		
		return ids;
	}
	
	public List<Potential> getLatestPotentials(){
		List<Potential> potentials = new ArrayList<Potential>();
		Potential potential;
		
		List<KdataMuster> musters = kdataService.getKdataMusters();
		
		for(KdataMuster item : musters) {
			if(item.isPeriodCount()) {
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
		}

		return potentials;
	}

	
	/*
	 * 上一交易日的收盘数据要等开盘前才能下载到，因为涉及到除权的调整
	 * 因此收盘后的各统计用的是最后一天的实时行情，称之为tmp，如果某只股票正在除权，会失真
	 * tmpLatestPotentials仅供盘后分析用
	 * 每日开盘后，系统会在9:30生成latestPotentials，供实盘操作用
	 */
	public void generateTmpLatestPotentials() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate latest potentials with latest kdata......");
		
		List<String> news = new ArrayList<String>();
		List<String> olds = new ArrayList<String>();
		List<String> outs = this.getTmpLatestPotentials();
		
		Map<String,Object> features;

		LocalDate date;
		Kdata kdata;
		Integer count = 55;
		List<Item> items = itemService.getItems();
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, item.getItemID());
			kdata = kdataService.getDailyKdata(item.getItemID(),false);
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
		FileUtil.writeTextFile(tmpLatestPotentialsFile, sb.toString(), false);
		
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
	
	public void generateLatestPotentials() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate latest potentials ......");
		
		LocalDate latestKdataDate = kdataService.getLatestDownDate();
		LocalDate theDate = this.getLatestPotentialDate();
		Potential potential;
		if(theDate==null || theDate.isBefore(latestKdataDate)) {
			StringBuffer sb = new StringBuffer(latestKdataDate.toString() + "\n");
			
			List<KdataMuster> musters = kdataService.getKdataMusters();
			
			int i=1;
			for(KdataMuster item : musters) {
				Progress.show(musters.size(),i++, item.getItemID());//进度条
				if(item.isPeriodCount()) {
					potential =  new Potential(item.getItemID(),
							item.getAmount(),
							item.getAverageAmount(),
							item.getHighest(),
							item.getLowest(),
							item.getPrice(),
							item.getPrice());
					if(potential.getHNGap()<10) {
						sb.append(potential.toText());
						sb.append("\n");	
					}					
				}
			}
			sb.deleteCharAt(sb.length()-1);
			
			FileUtil.writeTextFile(latestPotentialsFile, sb.toString(), false);
			this.init();
		}else {
			System.out.println("it has been generated! pass!");
		}
		
		System.out.println("generate latest potentials done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	public List<String> getTmpLatestPotentials() {
		List<String> potentials = new ArrayList<String>();
		
		String[] lines = FileUtil.readTextFile(tmpLatestPotentialsFile).split("\n");
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
