package com.namelessmc.NamelessAPI;

import static com.namelessmc.NamelessAPI.Request.RequestMethod.GET;
import static com.namelessmc.NamelessAPI.Request.RequestMethod.POST;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Request {
	
	private URL url;
	private RequestMethod method;
	private String parameters;
	
	private JsonObject response;
	private boolean hasError;
	private int errorCode = -1;
	
	public Request(URL baseUrl, Action action, String... parameters) {
		try {
			url = new URL(appendCharacter(baseUrl.toString(), '/') + action.toString());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("URL or action is malformed (" + e.getMessage() + ")");
		}
		this.method = action.method;
		this.parameters = String.join("&", parameters);
	}
	
	public boolean hasError() {
		return hasError;
	}
	
	public int getError() throws NamelessException {
		if (!hasError) {
			throw new NamelessException("Requested error code but there is no error.");
		}
		
		return errorCode;
	}
	
	public JsonObject getResponse() throws NamelessException {		
		return response;
	}
	
	public void connect() throws NamelessException {
		try {
			if (url.toString().startsWith("https://")){
				HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
	
				connection.setRequestMethod(method.toString());
				connection.setRequestProperty("Content-Length", Integer.toString(parameters.length()));
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setDoOutput(true);
				connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
	
				// Initialize output stream
				DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
	
				// Write request
				outputStream.writeBytes(parameters);
	
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
	
				// if (response.has("error")) {
				// Error with request
				// String errorMessage = response.get("message").getAsString();
				// throw new NamelessException("unknown error");
				// }
	
				// Close output/input stream
				outputStream.flush();
				outputStream.close();
				inputStream.close();
	
				// Disconnect
				connection.disconnect();
			} else {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	
				connection.setRequestMethod(method.toString());
				connection.setRequestProperty("Content-Length", Integer.toString(parameters.length()));
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setDoOutput(true);
				connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
	
				// Initialize output stream
				DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
	
				// Write request
				outputStream.writeBytes(parameters);
	
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
	
				// if (response.has("error")) {
				// Error with request
				// String errorMessage = response.get("message").getAsString();
				// throw new NamelessException(errorMessage);
				// }
	
				// Close output/input stream
				outputStream.flush();
				outputStream.close();
				inputStream.close();
	
				// Disconnect
				connection.disconnect();
			}
			
			hasError = response.get("error").getAsBoolean();
			if (hasError) {
				errorCode = response.get("code").getAsInt();
			}
		} catch (IOException e) {
			throw new NamelessException(e);
		}
	}
	
	private static String appendCharacter(String string, char c) {
		if (string.endsWith(c + "")) {
			return string;
		} else {
			return string + c;
		}
	}
	
	public static enum Action {
		
		INFO("info", GET),
		GET_ANNOUNCEMENTS("getAnnouncements", GET),
		REGISTER("register", POST),
		USER_INFO("userInfo", GET),
		SET_GROUP("setGroup", POST),
		CREATE_REPORT("createReport", POST),
		GET_NOTIFICATIONS("getNotifications", GET),
		SERVER_INFO("serverInfo", POST),
		
		;
		
		RequestMethod method;
		String name;
		
		Action(String name, RequestMethod method){
			this.name = name;
			this.method = method;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		/*@Override
		public String toString() {
			List<String> list = Arrays.asList(super.toString().split("_"));
			StringBuilder builder = new StringBuilder();
			builder.append(list.remove(0).toLowerCase(Locale.ENGLISH));
			list.forEach((element) -> builder.append(element.substring(0, 1) + element.substring(1).toLowerCase()));
			return builder.toString();
		}*/

	}
	
	public static enum RequestMethod {
		
		GET, POST
		
	}

}
