package com.namelessmc.java_api;

import static com.namelessmc.java_api.RequestHandler.RequestMethod.GET;
import static com.namelessmc.java_api.RequestHandler.RequestMethod.POST;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.namelessmc.java_api.logger.ApiLogger;

public class RequestHandler {

	private final URL baseUrl;
	private final String userAgent;
	private final Optional<ApiLogger> debugLogger;
	private final int timeout;

	RequestHandler(final URL baseUrl, final String userAgent, final Optional<ApiLogger> debugLogger, final int timeout) {
		this.baseUrl = baseUrl;
		this.userAgent = userAgent;
		this.debugLogger = debugLogger;
		this.timeout = timeout;
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

		return makeConnection(url, postData);
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
				final String paramString = Arrays.stream(parameters).map(Object::toString).collect(Collectors.joining("|"));
				throw new IllegalArgumentException(String.format("Parameter string varargs array length must be even (length is %s - %s)", parameters.length, paramString));
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
			throw new NamelessException("Error while building request URL: " + urlBuilder, e);
		}

		return makeConnection(url, null);
	}

	private void debug(final String message, final Object... args) {
		if (this.debugLogger.isPresent()) {
			this.debugLogger.get().log(String.format(message, args).replace(NamelessAPI.getApiKey(this.getApiUrl().toString()), "**API_KEY_REMOVED**"));
		}
	}

	private JsonObject makeConnection(final URL url, final JsonObject postBody) throws NamelessException {
		final HttpURLConnection connection;
		final byte[] bytes;
		try {
			connection = (HttpURLConnection) url.openConnection();

			connection.setReadTimeout(this.timeout);
			connection.setConnectTimeout(this.timeout);

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
				try (OutputStream out = connection.getOutputStream()) {
					out.write(encodedMessage);
				}
			}

			if (connection.getResponseCode() >= 400) {
				try (InputStream in = connection.getErrorStream()) {
					if (in == null) {
						throw new NamelessException("Website sent empty response with code " + connection.getResponseCode());
					} else {
						bytes = getBytesFromInputStream(in);
					}
				}
			} else {
				try (InputStream in = connection.getInputStream()) {
					bytes = getBytesFromInputStream(in);
				}
			}
		} catch (final IOException e) {
			String message = "IOException: " + e.getMessage();
			if (e.getMessage().contains("unable to find valid certification path to requested target")) {
				message += "\nHINT: Ensure your website uses a fullchain certificate";
			}
			throw new NamelessException(message, e);
		}

		String response = new String(bytes, StandardCharsets.UTF_8);

		debug("Website response below\n-----------------\n%s\n-----------------", response);

		JsonObject json;

		try {
			json = JsonParser.parseString(response).getAsJsonObject();
		} catch (final JsonSyntaxException | IllegalStateException e) {
			if (response.length() > 5_000) {
				response = response.substring(0, 5_000) + "\n[response truncated to 5k characters]";
			}

			if (!response.endsWith("\n")) {
				response = response + "\n";
			}
			int code;
			try {
				code = connection.getResponseCode();
			} catch (final IOException e1) {
				throw new NamelessException(e1);
			}
			String message = e.getMessage() + "\n"
					+ "Unable to parse json. Received response code " + code + ". Website response:\n"
					+ "-----------------\n"
					+ response
					+ "-----------------\n";
			if (code == 301 || code == 302 || code == 303) {
				message += "HINT: The URL results in a redirect. If your URL uses http://, change to https://. If your website forces www., make sure to add www. to the url";
			} else if (code == 520 || code == 521) {
				message += "HINT: Status code 520/521 is sent by CloudFlare when the backend webserver is down or having issues.";
			}
			throw new NamelessException(message, e);
		}

		if (!json.has("error")) {
			throw new NamelessException("Unexpected response from website (missing json key 'error')");
		}

		if (json.get("error").getAsBoolean()) {
			String meta = null;
			if (json.has("meta") && !json.get("meta").isJsonNull()) {
				meta = json.get("meta").toString();
			}
			throw new ApiError(json.get("code").getAsInt(), Optional.ofNullable(meta));
		}

		return json;
	}

	private static byte[] getBytesFromInputStream(final InputStream is) throws IOException {
	    final ByteArrayOutputStream os = new ByteArrayOutputStream();
	    final byte[] buffer = new byte[0xFFFF];
	    for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
	        os.write(buffer, 0, len);
	    }
	    return os.toByteArray();
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

		Action(final String name, final RequestMethod post) {
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
