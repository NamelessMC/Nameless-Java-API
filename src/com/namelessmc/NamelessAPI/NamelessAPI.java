package com.namelessmc.NamelessAPI;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import com.namelessmc.NamelessAPI.utils.NamelessRequestUtil;
import com.namelessmc.NamelessAPI.utils.NamelessRequestUtil.Request;

public class NamelessAPI {

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
		
		/* try {
			boolean https = url.toString().startsWith("https");
			
			URL apiConnection = new URL(url + "/checkConnection");
			
			URLConnection connection;
			
			if (https) {
				HttpsURLConnection httpsConnection = (HttpsURLConnection) apiConnection.openConnection();
				httpsConnection.setRequestMethod("POST");
				connection = httpsConnection;
			} else {
				HttpURLConnection httpConnection = (HttpURLConnection) apiConnection.openConnection();
				httpConnection.setRequestMethod("POST");
				connection = httpConnection;
			}
			
			connection.setRequestProperty("Content-Length", Integer.toString(0));
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");


			// Initialize output stream
			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
			// Write request
			outputStream.writeBytes("");

			// Initialize input stream
			InputStream inputStream = connection.getInputStream();

			// Handle response
			BufferedReader streamReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			StringBuilder responseBuilder = new StringBuilder();

			String responseString;
			while ((responseString = streamReader.readLine()) != null)
				responseBuilder.append(responseString);

			JsonObject response = new JsonObject();
			JsonParser parser = new JsonParser();

			response = parser.parse(responseBuilder.toString()).getAsJsonObject();
			
			outputStream.flush();
			outputStream.close();
			inputStream.close();

			if (https) {
				((HttpsURLConnection) connection).disconnect();
			} else {
				((HttpURLConnection) connection).disconnect();
			}
			
			if (response.has("success")
					|| response.get("message").getAsString().equalsIgnoreCase("Invalid API method")) {
				return null;
			} else if (response.has("error")) {
				return new NamelessException(response.get("message").getAsString());
			} else {
				return new NamelessException();
			}
		} catch (Exception e) {
			return new NamelessException(e);
		}*/
	}
	
	protected static String urlEncodeString(String string) {
		try {
			return URLEncoder.encode(string, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}