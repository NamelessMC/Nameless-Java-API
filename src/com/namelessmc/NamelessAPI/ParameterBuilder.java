package com.namelessmc.NamelessAPI;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParameterBuilder {
	
	private Map<String, String> parameters;
	
	public ParameterBuilder() {
		parameters = new HashMap<>();
	}
	
	public ParameterBuilder add(String key, Object value) {
		parameters.put(key, value.toString());
		return this;
	}
	
	public String[] build() {
		List<String> parameterStrings = new ArrayList<>();
		
		parameters.entrySet().forEach((entry) -> {
			parameterStrings.add(entry.getKey() + "-" + encode(entry.getValue()));
		});
		
		return parameterStrings.toArray(new String[] {});
	}
	
	private static String encode(Object object) {
		try {
			return URLEncoder.encode(object.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
