package com.rhb.istock.selector.favor;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.fdata.tushare.FdataServiceTushare;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;

@Service("favorServiceImp")
public class FavorServiceImp implements FavorService {
	@Value("${favorsFile}")
	private String favorsFile;
	
	@Value("${favorsPath}")
	private String favorsPath;	
	
	@Autowired
	@Qualifier("fdataServiceTushare")
	FdataServiceTushare fdataServiceTushare;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Override
	public Map<String, String> getFavors() {
		Map<String,String> articles = new HashMap<String,String> ();
		String source = FileTools.readTextFile(favorsFile);
		//System.out.println(source);
		String[] lines = source.split("\n");
		for(String str : lines) {
			//System.out.println(str);
			String[] ss = str.split(",");
			if(ss.length>1) {
				articles.put(ss[0].toLowerCase(), ss[2]);
			}
		}
		
		/*
		String id, value;
		Map<String,String> all = this.getAllFavors();
		for(Map.Entry<String, String> entry : all.entrySet()) {
			value = articles.get(entry.getKey());
			if(value == null) {
				articles.put(entry.getKey(), entry.getValue());
			}else {
				articles.put(entry.getKey(), value + "," + entry.getValue());
			}
		}
		
		Map<String,Map<String,Integer>> holdres = fdataServiceTushare.getFloatholders();
		for(Map.Entry<String, Map<String,Integer>> entry : holdres.entrySet()) {
			value = articles.get(entry.getKey());
			if(value == null) {
				articles.put(entry.getKey(), this.getMakers(entry.getValue()));
			}else {
				articles.put(entry.getKey(), value + "," + this.getMakers(entry.getValue()));
			}
			
		}*/
		
		return articles;
	}
	
	private String getMakers(Map<String,Integer> ms) {
		StringBuffer sb = new StringBuffer();
		for(Map.Entry<String, Integer> entry : ms.entrySet()) {
			if(sb.length()>0) {
				sb.append(",");
			}
			sb.append(entry.getKey());
			sb.append("(");
			sb.append(entry.getValue());
			sb.append(")");
		}
		return sb.toString();
	}
	
	private Map<String,String> getAllFavors(){
		Map<String,String> articles = new HashMap<String,String> ();
		String source, line, code, id, value;
		String[] lines;
		List<File> files = FileTools.getFiles(favorsPath, "txt", true);
		//System.out.println(files);

		for(File file : files){
			source = FileTools.readTextFile(file.getAbsolutePath());
			//System.out.println(file.getAbsolutePath());

			lines = source.split("\n");
			for(int i=1; i<lines.length-1; i++) {
				line = lines[i];
				//System.out.println(line);
				code = line.substring(0, 6);
				id = code.startsWith("6") ? "sh"+code : "sz"+code;
				value = articles.get(id);
				if(value == null) {
					value = file.getName().substring(0, file.getName().length()-4);
				}else {
					value = value + "，" + file.getName().substring(0, file.getName().length()-4);
				}
				articles.put(id, value);
			}
		}
		
		//System.out.println(articles);
		
		return articles;
	}

	@Override
	public Map<String, String> getFavorsOfB21() {
		Map<String,String> results = new HashMap<String,String>();
		LocalDate date = kdataService.getLastKdataDate("sh000001");
		Map<String,Muster> musters = kdataService.getMusters(date);
		Muster muster;
		Integer rate;
		Map<String,String> favors = this.getFavors();
		for(String itemID : favors.keySet()) {
			muster = musters.get(itemID);
			if(muster!=null ) {
				rate = Functions.growthRate(muster.getLatestPrice(), muster.getAveragePrice21());
				if(rate<0 && rate>-8) {
					results.put(itemID, favors.get(itemID));
				}
			}
		}
		
		return results;
	}
	
	@Override
	public Map<String, String> getFavorsOfB21up() {
		Map<String,String> results = new HashMap<String,String>();
		LocalDate date = kdataService.getLastKdataDate("sh000001");
		Map<String,Muster> musters = kdataService.getMusters(date);
		Muster muster;
		Integer rate;
		Map<String,String> favors = this.getFavors();
		for(String itemID : favors.keySet()) {
			muster = musters.get(itemID);
			if(muster!=null ) {
				rate = Functions.growthRate(muster.getLatestPrice(), muster.getAveragePrice21());
				if(rate<5 && rate>=0) {
					results.put(itemID, favors.get(itemID));
				}
			}
		}
		
		return results;
	}

	@Override
	public List<Muster> getFavors(LocalDate date) {
		List<Muster> results = new ArrayList<Muster>();
		Muster muster;
		Map<String,Muster> musters = kdataService.getMusters(date);
		if(musters!=null && musters.size()>0) {
			Map<String, String> favors = this.getFavors();
			for(String id : favors.keySet()) {
				muster = musters.get(id);
				if(muster!=null) {
					results.add(muster);
				}
			}
		}
		return results;
	}
	

}
