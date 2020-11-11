package com.rhb.istock.selector.favor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.rhb.istock.kdata.Muster;

public interface FavorService {
	public Map<String,String> getFavors();
	public List<Muster> getFavors(LocalDate date);
	public Map<String,String> getFavorsOfB21();
	public Map<String,String> getFavorsOfB21up();
}
