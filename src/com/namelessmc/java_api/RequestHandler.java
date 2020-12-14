package com.namelessmc.java_api;

import static com.namelessmc.java_api.RequestHandler.RequestMethod.GET;
import static com.namelessmc.java_api.RequestHandler.RequestMethod.POST;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class RequestHandler {
	
	private final URL baseUrl;
	private final String userAgent;
	private final boolean debug;
	
	RequestHandler(final URL baseUrl, final String userAgent, final boolean debug) {
		this.baseUrl = baseUrl;
		this.userAgent = userAgent;
		this.debug = debug;
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
	
	private void debug(final String message, final Object... args) {
		if (this.debug) {
			System.out.println(String.format(message, args));
		}
	}
	
	private JsonObject makeConnection(final URL url, final JsonObject postBody) throws NamelessException, IOException {
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		debug("Making connection %s to url %s", postBody != null ? "POST" : "GET", url);

		connection.addRequestProperty("User-Agent", this.userAgent);
		
		debug("Using User-Agent '%s'", this.userAgent);
		
		if (postBody != null) {
			debug("Post body below\n-----------------\n%s\n-----------------", postBody);
			connection.setRequestMethod("POST");
			final byte[] encodedMessage = postBody.toString().getBytes(StandardCharsets.UTF_8);
			connection.setRequestProperty("Content-Length", encodedMessage.length + "");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			try (OutputStream out = connection.getOutputStream()){
				out.write(encodedMessage);
			}
		}
		
		String response;
		
		try (InputStream in = connection.getInputStream()) {
			response = IOUtils.toString(in, StandardCharsets.UTF_8);
		}
		
		debug("Website response below\n-----------------\n%s\n-----------------", response);
		
		JsonObject json;
		
		try {
			json = JsonParser.parseString(response).getAsJsonObject();
		} catch (final JsonSyntaxException | IllegalStateException e) {
			if (!response.endsWith("\n")) {
				response = response + "\n";
			}
			final String message = e.getMessage() + "\n"
					+ "Website response:\n"
					+ "-----------------\n"
					+ response
					+ "-----------------\n";
			throw new NamelessException(message, e);
		}

		connection.disconnect();
		
		if (!json.has("error")) {
			throw new NamelessException("Unexpected response from website (missing json key 'error')");
		}

		if (json.get("error").getAsBoolean()) {
			throw new ApiError(json.get("code").getAsInt());
		}
		
		return json;
	}
	
	public enum Action {

		INFO("info", GET),
		GET_ANNOUNCEMENTS("getAnnouncements", GET),
		REGISTER("register", POST),
		USER_INFO("userInfo", GET),
		GROUP_INFO("groupInfo", GET),
		ADD_GROUPS("addGroups", POST),
		REMOVE_GROUPS("removeGroups", POST),
		CREATE_REPORT("createReport", POST),
		GET_NOTIFICATIONS("getNotifications", GET),
		SERVER_INFO("serverInfo", POST),
		UPDATE_USERNAME("updateUsername", POST),
		VERIFY_MINECRAFT("verifyMinecraft", POST),
		LIST_USERS("listUsers", GET),
		INGAME_RANKS("ingameRanks", POST),
		UPDATE_DISCORD_BOT_SETTINGS("updateDiscordBotSettings", POST),
		VERIFY_DISCORD("verifyDiscord", POST),
		UPDATE_DISCORD_USERNAMES("updateDiscordUsernames", POST),
		GET_DISCORD_ROLES("getDiscordRoles", GET),
		SET_DISCORD_ROLES("setDiscordRoles", POST),
		ADD_DISCORD_ROLES("addDiscordRoles", POST),
		REMOVE_DISCORD_ROLES("removeDiscordRoles", POST),
		SUBMIT_DISCORD_ROLE_LIST("submitDiscordRoleList", POST),

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
