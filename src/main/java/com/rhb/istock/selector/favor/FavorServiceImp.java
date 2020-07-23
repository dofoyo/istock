package com.rhb.istock.selector.favor;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.fdata.tushare.FdataServiceTushare;

@Service("favorServiceImp")
public class FavorServiceImp implements FavorService {
	@Value("${favorsFile}")
	private String favorsFile;
	
	@Value("${favorsPath}")
	private String favorsPath;	
	
	@Autowired
	@Qualifier("fdataServiceTushare")
	FdataServiceTushare fdataServiceTushare;
	
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
			
		}
		
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
	

}
