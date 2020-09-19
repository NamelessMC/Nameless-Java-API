package com.namelessmc.NamelessAPI;

import static com.namelessmc.NamelessAPI.Request.RequestMethod.GET;
import static com.namelessmc.NamelessAPI.Request.RequestMethod.POST;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Deprecated
public class Request {

	private URL url;
	private final RequestMethod method;
	private final String parameters;
	private final Action action;
	private final String userAgent;

	private JsonObject response;
	private boolean hasError;
	private int errorCode = -1;

	public Request(final URL baseUrl, final String userAgent, final Action action, final String... parameters) {
		this.userAgent = userAgent;
		this.action = action;
		this.method = action.method;
		this.parameters = String.join("&", parameters);

		try {
			final String base = baseUrl.toString() + "/" + action.toString();

			if (action.method == RequestMethod.GET && parameters.length > 0) {
				this.url = new URL(base + "&" + this.parameters);
			} else {
				this.url = new URL(base);
			}
		} catch (final MalformedURLException e) {
			final IllegalArgumentException ex = new IllegalArgumentException("URL is malformed (" + e.getMessage() + ")");
			ex.initCause(e);
			throw ex;
		}
	}

	public boolean hasError() {
		return this.hasError;
	}

	public int getError() throws NamelessException {
		if (!this.hasError) {
			throw new NamelessException("Requested error code but there is no error.");
		}

		return this.errorCode;
	}

	public JsonObject getResponse() throws NamelessException {
		return this.response;
	}

	public void connect() throws NamelessException {
		if (NamelessAPI.DEBUG_MODE) {
			System.out.println(String.format("NamelessAPI > Making %s request (%s", this.action.toString(), this.method.toString()));
			System.out.println(String.format("NamelessAPI > URL: %s", this.url));
			System.out.println(String.format("NamelessAPI > Parameters: %s", this.parameters));
		}

		try {
			final HttpURLConnection connection = (HttpURLConnection) this.url.openConnection();

			connection.setRequestMethod(this.method.toString());
			connection.addRequestProperty("User-Agent", this.userAgent);
			
			
			if (this.method == RequestMethod.POST) {
				final byte[] encodedMessage = this.parameters.getBytes(Charset.forName("UTF-8"));
				connection.setRequestProperty("Content-Length", encodedMessage.length + "");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setDoOutput(true);
				try (OutputStream out = connection.getOutputStream()){
					out.write(encodedMessage);
				}
			}

			try (InputStream in = connection.getInputStream();
					Reader reader = new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"))){
				JsonParser.parseReader(reader).getAsJsonObject();
			}

			connection.disconnect();

			this.hasError = this.response.get("error").getAsBoolean();
			if (this.hasError) {
				this.errorCode = this.response.get("code").getAsInt();
			}
		} catch (final IOException e) {
			throw new NamelessException(e);
		}
	}

	public enum Action {

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

		Action(final String name, final RequestMethod method){
			this.name = name;
			this.method = method;
		}

		@Override
		public String toString() {
			return this.name;
		}

	}

	public enum RequestMethod {

		GET, POST

	}

}
