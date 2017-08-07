package com.namelessmc.NamelessAPI.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class NamelessPostString {
	
	public static String urlEncodeString(String string) {
		try {
			return URLEncoder.encode(string, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
