package com.rhb.istock.trade.turtle.operation.repository;

import java.util.List;
import java.util.Map;

public interface TurtleRepository {
	List<HoldEntity> getHolds(); 
	public Map<String,String> getFavors();

}
