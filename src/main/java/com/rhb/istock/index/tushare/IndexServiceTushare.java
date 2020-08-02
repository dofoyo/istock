package com.rhb.istock.index.tushare;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;

@Service("indexServiceTushare")
public class IndexServiceTushare {
	@Autowired
	@Qualifier("indexRepositoryTushare")
	IndexRepositoryTushare indexRepositoryTushare;
	
	public Set<LocalDate> getSseiDates(LocalDate endDate, Integer period){
		String ts_code = "000001.SH";
		IndexData data = indexRepositoryTushare.getIndexDatas(ts_code, endDate, period);
		return data.getDates();
	
	}
	
	public Integer getSseiGrowthRate(LocalDate endDate, Integer period) {
		String ts_code = "000001.SH";
		IndexData data = indexRepositoryTushare.getIndexDatas(ts_code, endDate, period);
		return data.growthRate();
	}
	
	public Integer getGrowthRate(String itemID,LocalDate endDate, Integer period) {
		Set<LocalDate> dates = this.getSseiDates(endDate, period);
		IndexData data = indexRepositoryTushare.getIndexDatas(itemID, dates);
		if(data==null) {
			return 0;
		}else {
			return data.growthRate();
		}
	}
	
	public TreeMap<Integer, Set<IndexBasic>> getGrowthRate(LocalDate endDate, Integer period) {
		TreeMap<Integer, Set<IndexBasic>> indexs = new TreeMap<Integer, Set<IndexBasic>>();
		Set<IndexBasic> allIndexs = indexRepositoryTushare.getIndexBasic();
		Integer ssei = this.getSseiGrowthRate(endDate, period);
		Integer index, i=1;
		Set<IndexBasic> tmp;
		for(IndexBasic ib : allIndexs) {
			index = this.getGrowthRate(ib.getTs_code(), endDate, period);
			if(index > ssei) {
				tmp = indexs.get(index);
				if(tmp==null) {
					tmp = new HashSet<IndexBasic>();
					indexs.put(index, tmp);
				}
				tmp.add(ib);
			}
			Progress.show(allIndexs.size(),i++, " getUpIndex: " + ib.getTs_code() + ", " + index);//进度条

		}
		return indexs;
	}
	
	public Map<String,Set<IndexWeight>> getIndexWeights(){
		return indexRepositoryTushare.getIndexWeights();
	}
}
