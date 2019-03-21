package com.rhb.istock.selector.bluechip;

import java.time.LocalDate;
import java.util.List;

public interface BluechipService {
	public void generateBluechip();
	public List<String> getBluechipIDs(LocalDate date);

}
