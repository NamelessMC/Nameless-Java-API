package com.namelessmc.NamelessAPI;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.JsonObject;
import com.namelessmc.NamelessAPI.Request.Action;

public final class NamelessAPI {

	private NamelessAPI() {}
	
	/**
	 * Checks if a web API connection can be established
	 * @return An exception if the connection was unsuccessful, null if the connection was successful.
	 */
	public static Throwable checkWebAPIConnection(URL url) {		
		try {
			JsonObject response = new Request(url, Action.INFO).getResponse();
			if (response.has("version")) {
				return null;
			} else {
				return new NamelessException("Invalid respose: " + response.getAsString());
			}
		} catch (NamelessException e) {
			if (e.getCause() != null)
				return e.getCause();
			else
				return e;
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