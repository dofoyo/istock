package com.rhb.istock.selector.favor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;

@Service("favorServiceImp")
public class FavorServiceImp implements FavorService {
	@Value("${favorsFile}")
	private String favorsFile;
	
	
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
				articles.put(ss[0].toLowerCase(), ss[1]);
			}
		}
		return articles;
	}

}
