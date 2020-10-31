package com.namelessmc.java_api;

import static com.namelessmc.java_api.RequestHandler.RequestMethod.GET;
import static com.namelessmc.java_api.RequestHandler.RequestMethod.POST;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class RequestHandler {
	
	private final URL baseUrl;
	private final String userAgent;
	
	RequestHandler(final URL baseUrl, final String userAgent, final boolean debug) {
		this.baseUrl = baseUrl;
		this.userAgent = userAgent;
	}
	
	public URL getApiUrl() {
		return this.baseUrl;
	}
	
	public JsonObject post(final Action action, final JsonObject postData) throws NamelessException {
		if (action.method != RequestMethod.POST) {
			throw new IllegalArgumentException("Cannot POST to a GET API method");
		}
		
		URL url;
		try {
			url = new URL(this.baseUrl.toString() + "/" + action);
		} catch (final MalformedURLException e) {
			throw new NamelessException("Invalid URL or parameter string");
		}
		
		try {
			return makeConnection(url, postData);
		} catch (final IOException e) {
			throw new NamelessException(e);
		}
	}
	
	public JsonObject get(final Action action, final Object... parameters) throws NamelessException {
		if (action.method != RequestMethod.GET) {
			throw new IllegalArgumentException("Cannot GET a POST API method");
		}
		
		final StringBuilder urlBuilder = new StringBuilder(this.baseUrl.toString());
		urlBuilder.append("/");
		urlBuilder.append(action);
		
		if (parameters.length > 0) {
			if (parameters.length % 2 != 0) {
				throw new IllegalArgumentException("Parameter string varargs array length must be even");
			}
			
			for (int i = 0; i < parameters.length; i++) {
				if (i % 2 == 0) {
					urlBuilder.append("&");
					urlBuilder.append(parameters[i]);
				} else {
					urlBuilder.append("=");
					try {
						urlBuilder.append(URLEncoder.encode(parameters[i].toString(), StandardCharsets.UTF_8.toString()));
					} catch (final UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		
		URL url;
		try {
			url = new URL(urlBuilder.toString());
		} catch (final MalformedURLException e) {
			throw new NamelessException("Error while building request URL: " + urlBuilder);
		}
		
		try {
			return makeConnection(url, null);
		} catch (final IOException e) {
			throw new NamelessException(e);
		}
	}
	
	private JsonObject makeConnection(final URL url, final JsonObject postBody) throws NamelessException, IOException {
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.addRequestProperty("User-Agent", this.userAgent);
		
		if (postBody != null) {
			connection.setRequestMethod("POST");
			final byte[] encodedMessage = postBody.toString().getBytes(StandardCharsets.UTF_8);
			connection.setRequestProperty("Content-Length", encodedMessage.length + "");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			try (OutputStream out = connection.getOutputStream()){
				out.write(encodedMessage);
			}
		}
		
		JsonObject response;
		
		try (InputStream in = connection.getInputStream();
				Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)){
			response = JsonParser.parseReader(reader).getAsJsonObject();
		}

		connection.disconnect();
		
		if (!response.has("error")) {
			throw new NamelessException("Unexpected response from website (missing json key 'error')");
		}

		if (response.get("error").getAsBoolean()) {
			throw new ApiError(response.get("code").getAsInt());
		}
		
		return response;
	}
	
	public enum Action {

		INFO("info", GET),
		GET_ANNOUNCEMENTS("getAnnouncements", GET),
		REGISTER("register", POST),
		USER_INFO("userInfo", GET),
		LIST_GROUPS("listGroups", GET),
		GROUP_INFO("groupInfo", GET),
		SET_PRIMARY_GROUP("setPrimaryGroup", POST),
		ADD_SECONDARY_GROUPS("addSecondaryGroups", POST),
		REMOVE_SECONDARY_GROUPS("setSecondaryGroups", POST),
		SET_GROUP("setGroup", POST),
		CREATE_REPORT("createReport", POST),
		GET_NOTIFICATIONS("getNotifications", GET),
		SERVER_INFO("serverInfo", POST),
		UPDATE_USERNAME("updateUsername", POST),
		VERIFY_MINECRAFT("verifyMinecraft", POST),
		LIST_USERS("listUsers", GET),
		INGAME_RANKS("ingameRanks", POST),
		SET_DISCORD_BOT_URL("setDiscordBotUrl", POST),
		VERIFY_DISCORD("verifyDiscord", POST),
		GET_DISCORD_ID("getDiscordId", GET),
		GET_DISCORD_ROLES("getDiscordRoles", GET),
		GET_USER_BY_DISCORD_ID("getUserByDiscordId", GET),
		SET_DISCORD_ROLES("setDiscordRoles", POST),
		ADD_DISCORD_ROLES("addDiscordRoles", POST),
		REMOVE_DISCORD_ROLES("removeDiscordRoles", POST),

		;

		RequestMethod method;
		String name;

		Action(final String name, final RequestMethod post){
			this.name = name;
			this.method = post;
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
