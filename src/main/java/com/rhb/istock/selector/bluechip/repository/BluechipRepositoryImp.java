package com.rhb.istock.selector.bluechip.repository;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service("bluechipRepositoryImp")
public class BluechipRepositoryImp implements BluechipRepository {
	@Value("${bluechipsFile}")
	private String bluechipsFile;
	
	@Override
	public void save(Collection<BluechipEntity> bluechips) {
		writeToFile(bluechipsFile, bluechips);
	}

	@Override
	public Set<BluechipEntity> getBluechips() {
		Set<BluechipEntity> bluechips = null;

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		try {
			bluechips = mapper.readValue(new File(bluechipsFile), new TypeReference<Set<BluechipEntity>>() {});
		} catch (IOException e) {
			e.printStackTrace();
		}

		return bluechips;
	}

	private void writeToFile(String jsonfile, Object object){
		ObjectMapper mapper = new ObjectMapper();
    	try {
			mapper.writeValue(new File(jsonfile),object);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
