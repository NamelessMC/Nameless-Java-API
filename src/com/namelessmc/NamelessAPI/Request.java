package com.namelessmc.NamelessAPI;

import static com.namelessmc.NamelessAPI.Request.RequestMethod.GET;
import static com.namelessmc.NamelessAPI.Request.RequestMethod.POST;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
	private Action action;

	private JsonObject response;
	private boolean hasError;
	private int errorCode = -1;

	public Request(URL baseUrl, Action action, String... parameters) {
		this.action = action;
		this.method = action.method;
		this.parameters = String.join("&", parameters);
		
		try {
			/*
			 * If the url contains a question mark, it's most likely not a friendly url.
			 * Friendly urls have the normal url as a parameter like this:
			 * https://example.com/index.php?route=/api/v2/key/action
			 * Parameters should be added after the route parameter (&param1=value1&param2=value2)
			 * 
			 * If the url does not contain a question mark, it's most likely a friendly url
			 * https://example.com/api/v2/key/action
			 * Parameters need to be prefixed with a question mark (?param1&value1&param2=value2)
			 * 
			 * For post method, parameters need to be sent like this: param1=value1&param2=value2
			 * regardless of the friendly url option.
			 */
			
			String base = baseUrl.toString();
			base = base.endsWith("/") ? base : base + "/"; // Append trailing slash if not present
			
			if (action.method == RequestMethod.GET){
				if (parameters.length > 0) {
					char prefix = baseUrl.toString().contains("?") ? '&' : '?';
					url = new URL(base + action.toString() + prefix + this.parameters);
				} else {
					url = new URL(base + action.toString());
				}
			}
			
			if (action.method == RequestMethod.POST) {
				url = new URL(base + action.toString());
			}
		} catch (MalformedURLException e) {
			IllegalArgumentException ex = new IllegalArgumentException("URL is malformed (" + e.getMessage() + ")");
			ex.initCause(e);
			throw ex;
		}
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

		if (NamelessAPI.DEBUG_MODE) {
			System.out.println(String.format("NamelessAPI > Making %s request (%s", action.toString(), method.toString()));
			System.out.println(String.format("NamelessAPI > URL: %", url));
			System.out.println(String.format("NamelessAPI > Parameters: %s", parameters));
		}

		try {
			if (url.toString().startsWith("https://")){
				HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

				connection.setRequestMethod(method.toString());
				connection.setRequestProperty("Content-Length", Integer.toString(parameters.length()));
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

				// If request method is POST, send parameters. Otherwise, they're already included in the URL
				if (action.method == RequestMethod.POST) {
					connection.setDoOutput(true);
					DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
					writer.write(parameters);
					writer.flush();
					writer.close();
					outputStream.close();
				}

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

				inputStream.close();

				// Disconnect
				connection.disconnect();
			} else {
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				connection.setRequestMethod(method.toString());
				connection.setRequestProperty("Content-Length", Integer.toString(parameters.length()));
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");

				// If request method is POST, send parameters. Otherwise, they're already included in the URL
				if (action.method == RequestMethod.POST) {
					connection.setDoOutput(true);
					DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
					writer.write(parameters);
					writer.flush();
					writer.close();
					outputStream.close();
				}

				// Initialize input stream
				InputStream inputStream = connection.getInputStream();

				// Handle response
				BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				StringBuilder responseBuilder = new StringBuilder();

				String responseString;
				while ((responseString = streamReader.readLine()) != null)
					responseBuilder.append(responseString);

				JsonParser parser = new JsonParser();
				
				if (NamelessAPI.DEBUG_MODE) {
					System.out.println(String.format("NamelessAPI > Response: %s", responseBuilder.toString()));
				}
				
				response = parser.parse(responseBuilder.toString()).getAsJsonObject();

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

	public static enum Action {

		INFO("info", GET),
		GET_ANNOUNCEMENTS("getAnnouncements", GET),
		REGISTER("register", POST),
		USER_INFO("userInfo", GET),
		SET_GROUP("setGroup", POST),
		CREATE_REPORT("createReport", POST),
		GET_NOTIFICATIONS("getNotifications", GET),
		SERVER_INFO("serverInfo", POST),
		VALIDATE_USER("validateUser", POST),
		LIST_USERS("listUsers", GET),

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
