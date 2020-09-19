package com.namelessmc.java_api;

import static com.namelessmc.java_api.RequestHandler.RequestMethod.GET;
import static com.namelessmc.java_api.RequestHandler.RequestMethod.POST;

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

public class RequestHandler {
	
	private final URL baseUrl;
	private final String userAgent;
	
	RequestHandler(final URL baseUrl, final String userAgent){
		this.baseUrl = baseUrl;
		this.userAgent = userAgent;
	}
	
	public JsonObject post(final Action action, final String postData) throws NamelessException {
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
	
	public JsonObject get(final Action action, final String parameters) throws NamelessException {
		URL url;
		try {
			final String base = this.baseUrl.toString() + "/" + action;
			if (parameters != null && !parameters.isEmpty()) {
				url = new URL(base + "&" + parameters);
			} else {
				url = new URL(base);
			}
		} catch (final MalformedURLException e) {
			throw new NamelessException("Invalid URL or parameter string");
		}
		
		try {
			return makeConnection(url, null);
		} catch (final IOException e) {
			throw new NamelessException(e);
		}
	}
	
	private JsonObject makeConnection(final URL url, final String postBody) throws NamelessException, IOException {
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.addRequestProperty("User-Agent", this.userAgent);
		
		if (postBody != null) {
			connection.setRequestMethod("POST");
			final String contentType = postBody.startsWith("[") || postBody.startsWith("{") ? "application/json" : "text/plain";
			final byte[] encodedMessage = postBody.getBytes(Charset.forName("UTF-8"));
			connection.setRequestProperty("Content-Length", encodedMessage.length + "");
			connection.setRequestProperty("Content-Type", contentType);
			connection.setDoOutput(true);
			try (OutputStream out = connection.getOutputStream()){
				out.write(encodedMessage);
			}
		}
		
		JsonObject response;
		
		try (InputStream in = connection.getInputStream();
				Reader reader = new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8"))){
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
		SET_GROUP("setGroup", POST),
		CREATE_REPORT("createReport", POST),
		GET_NOTIFICATIONS("getNotifications", GET),
		SERVER_INFO("serverInfo", POST),
		UPDATE_USERNAME("updateUsername", POST),
		VERIFY_MINECRAFT("verifyMinecraft", POST),
		LIST_USERS("listUsers", GET),
		INGAME_RANKS("ingameRanks", POST),
		VERIFY_DISCORD("verifyDiscord", POST),
		GET_DISCORD_ID("getDiscordId", GET),
		GET_DISCORD_ROLES("getDiscordRoles", GET),
		GET_USER_BY_DISCORD_ID("getUserByDiscordId", GET),
		SET_DISCORD_ROLES("setDiscordRoles", POST),
		ADD_DISCORD_ROLE("addDiscordRole", POST),
		REMOVE_DISCORD_ROLE("removeDiscordRole", POST),

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
