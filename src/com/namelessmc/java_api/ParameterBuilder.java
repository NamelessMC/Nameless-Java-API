package com.namelessmc.java_api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class ParameterBuilder {
	
	private final Map<String, String> parameters;
	
	public ParameterBuilder() {
		this.parameters = new HashMap<>();
	}
	
	public ParameterBuilder add(final String key, final Object value) {
		this.parameters.put(key, value.toString());
		return this;
	}
	
	public String[] build() {
		final List<String> parameterStrings = new ArrayList<>();
		
		this.parameters.entrySet().forEach((entry) -> {
			parameterStrings.add(entry.getKey() + "=" + encode(entry.getValue()));
		});
		
		return parameterStrings.toArray(new String[] {});
	}
	
	private static String encode(final Object object) {
		try {
			return URLEncoder.encode(object.toString(), "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
