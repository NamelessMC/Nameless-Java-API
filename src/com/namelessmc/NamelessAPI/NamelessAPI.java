package com.namelessmc.NamelessAPI;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import com.namelessmc.NamelessAPI.utils.NamelessRequestUtil;
import com.namelessmc.NamelessAPI.utils.NamelessRequestUtil.Request;

public final class NamelessAPI {

	private NamelessAPI() {}
	
	/**
	 * Checks if a web API connection can be established
	 * @return An exception if the connection was unsuccessful, null if the connection was successful.
	 */
	public static Exception checkWebAPIConnection(URL url) {
		Request request = NamelessRequestUtil.sendPostRequest(url, "checkConnection", null);
		if (request.hasSucceeded()) {
			return null;
		} else {
			return request.getException();
		}
	}
	
	static String encode(Object object) {
		try {
			return URLEncoder.encode(object.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}