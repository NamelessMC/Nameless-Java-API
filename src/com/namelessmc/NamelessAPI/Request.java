package com.namelessmc.NamelessAPI;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static com.namelessmc.NamelessAPI.Request.RequestMethod.*;

public class Request {
	
	private URL url;
	private RequestMethod method;
	private String parameters;
	
	private JsonObject response;
	
	public Request(URL baseUrl, Action action, String... parameters) {
		try {
			url = new URL(appendCharacter(baseUrl.toString(), '/') + action.toString());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("URL or action is malformed (" + e.getMessage() + ")");
		}
		this.method = action.method;
		this.parameters = String.join("&", parameters);
	}
	
	public JsonObject getResponse() throws NamelessException {
		if (response == null) {
			connect();
		}
		
		return response;
	}
	
	public void connect() throws NamelessException {
		if (url.toString().startsWith("https://")){
			try {
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

				if (response.has("error")) {
					// Error with request
					String errorMessage = response.get("message").getAsString();
					throw new NamelessException(errorMessage);
				}

				// Close output/input stream
				outputStream.flush();
				outputStream.close();
				inputStream.close();

				// Disconnect
				connection.disconnect();
			} catch (Exception e) {
				throw new NamelessException(e);
			}
		} else {
			try {
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

				if (response.has("error")) {
					// Error with request
					String errorMessage = response.get("message").getAsString();
					throw new NamelessException(errorMessage);
				}

				// Close output/input stream
				outputStream.flush();
				outputStream.close();
				inputStream.close();

				// Disconnect
				connection.disconnect();
			} catch (Exception e) {
				throw new NamelessException(e);
			}
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
		
		INFO(GET),
		GET_ANNOUNCEMENTS(GET),
		REGISTER(POST),
		USER_INFO(GET),
		SET_GROUP(POST),
		CREATE_REPORT(POST),
		GET_NOTIFICATIONS(GET),
		SERVER_INFO(POST),
		
		;
		
		RequestMethod method;
		
		Action(RequestMethod method){
			this.method = method;
		}
		
		@Override
		public String toString() {
			List<String> list = Arrays.asList(super.toString().split("_"));
			StringBuilder builder = new StringBuilder();
			builder.append(list.remove(0).toLowerCase(Locale.ENGLISH));
			list.forEach((element) -> builder.append(element.substring(0, 1) + element.substring(1).toLowerCase()));
			return builder.toString();
		}

	}
	
	public static enum RequestMethod {
		
		GET, POST
		
	}

}
