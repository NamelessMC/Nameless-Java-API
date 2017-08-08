package com.namelessmc.NamelessAPI.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.NamelessAPI.NamelessException;

public class NamelessRequestUtil {

	/**
	 * @param url Full URL with / at the end
	 * @param postString
	 * @param https
	 * @return
	 */
	public static Request sendPostRequest(URL baseUrl, String action, String postString) {

		if (baseUrl == null) {
			throw new IllegalArgumentException("URL must not be null");
		}
			
		if (postString == null) {
			postString = "";
		}
		
		String baseUrlString = appendCharacter(baseUrl.toString(), '/');
		
		URL url;
		
		try {
			url = new URL(baseUrlString + action);
		} catch (MalformedURLException e1) {
			throw new IllegalArgumentException("URL or action is malformed (" + e1.getMessage() + ")");
		}

		if(url.toString().startsWith("https")){
			return httpsRequest(url, postString);
		}else {
			return httpRequest(url, postString);
		}
	}
	
	private static Request httpsRequest(URL url, String postString) {
		Exception exception;
		JsonObject response;
		try {
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Length", Integer.toString(postString.length()));
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

			// Initialize output stream
			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

			// Write request
			outputStream.writeBytes(postString);

			// Initialize input stream
			InputStream inputStream = connection.getInputStream();

			// Handle response
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			StringBuilder responseBuilder = new StringBuilder();

			String responseString;
			while ((responseString = streamReader.readLine()) != null)
				responseBuilder.append(responseString);

			JsonParser parser = new JsonParser();

			response = parser.parse(responseBuilder.toString()).getAsJsonObject();

			if (response.has("error")) {
				// Error with request
				String errorMessage = response.get("message").getAsString();
				exception = new NamelessException(errorMessage);
			}

			// Close output/input stream
			outputStream.flush();
			outputStream.close();
			inputStream.close();

			// Disconnect
			connection.disconnect();

			exception = null;
		} catch (Exception e) {
			exception = e;
			response = null;
		}

		return new Request(exception, response);
	}
	
	private static Request httpRequest(URL url, String postString) {
		Exception exception;
		JsonObject response;
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Length", Integer.toString(postString.length()));
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

			// Initialize output stream
			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

			// Write request
			outputStream.writeBytes(postString);

			// Initialize input stream
			InputStream inputStream = connection.getInputStream();

			// Handle response
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			StringBuilder responseBuilder = new StringBuilder();

			String responseString;
			while ((responseString = streamReader.readLine()) != null)
				responseBuilder.append(responseString);

			JsonParser parser = new JsonParser();

			response = parser.parse(responseBuilder.toString()).getAsJsonObject();

			if (response.has("error")) {
				// Error with request
				String errorMessage = response.get("message").getAsString();
				exception = new NamelessException(errorMessage);
			}

			// Close output/input stream
			outputStream.flush();
			outputStream.close();
			inputStream.close();

			// Disconnect
			connection.disconnect();

			exception = null;
		} catch (Exception e) {
			exception = e;
			response = null;
		}

		return new Request(exception, response);
	}
	
	private static String appendCharacter(String string, char c) {
		if (string.endsWith(c + "")) {
			return string;
		} else {
			return string + c;
		}
	}
	
	public static class Request {
		
		private Exception exception;
		private JsonObject response;
		
		public Request(Exception exception, JsonObject response) {
			this.exception = exception;
		}
		
		public Exception getException() {
			return exception;
		}
		
		public boolean hasSucceeded() {
			return exception == null;
		}
		
		public JsonObject getResponse() {
			return response;
		}
		
	}

}