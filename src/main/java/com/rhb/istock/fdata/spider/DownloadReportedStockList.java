package com.rhb.istock.fdata.spider;

import java.util.Map;

public interface DownloadReportedStockList {

	public Map<String,String> go(String year);
}
