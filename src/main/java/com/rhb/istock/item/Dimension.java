package com.rhb.istock.item;

import java.util.HashMap;
import java.util.Map;

public class Dimension {
	private Map<String,Map<String,String>> ic = new HashMap<String,Map<String,String>>();
	
	public void put(String[] ss, String id, String name) {
		for(String s : ss) {
			this.put(s, id, name);
		}
	}
	
	public void put(String str, String id, String name) {
		Map<String,String> ids = ic.get(str);
		if(ids == null) {
			ids = new HashMap<String,String>();
		}
		ids.put(id, name);
		ic.put(str, ids);
	}
	
	public Map<String,Map<String,String>> getResult(){
		return ic;
	}

}
