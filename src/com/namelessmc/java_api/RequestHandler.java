package com.namelessmc.java_api;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.namelessmc.java_api.exception.ApiDisabledException;
import com.namelessmc.java_api.logger.ApiLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RequestHandler {

	private final @NotNull URL baseUrl;
	private final @NotNull String apiKey;
	private final @NotNull String userAgent;
	private final @Nullable ApiLogger debugLogger;
	private final int timeout;

	RequestHandler(final @NotNull URL baseUrl, final @NotNull String apiKey, final @NotNull String userAgent, @Nullable ApiLogger debugLogger, final int timeout) {
		this.baseUrl = Objects.requireNonNull(baseUrl, "Base URL is null");
		this.apiKey = Objects.requireNonNull(apiKey, "Api key is null");
		this.userAgent = Objects.requireNonNull(userAgent, "User agent is null");
		this.debugLogger = debugLogger;
		this.timeout = timeout;
	}

	public @NotNull URL getApiUrl() {
		return this.baseUrl;
	}

	public @NotNull String getApiKey() {
		return this.apiKey;
	}

	public @NotNull JsonObject post(final @NotNull String route, final @Nullable JsonObject postData) throws NamelessException {
		Preconditions.checkArgument(!route.startsWith("/"), "Route must not start with a slash");

		URL url;
		try {
			url = new URL(this.baseUrl + "/" + route);
		} catch (final MalformedURLException e) {
			throw new NamelessException("Invalid URL or parameter string");
		}

		return makeConnection(url, postData);
	}

	public @NotNull JsonObject get(final @NotNull String route, final @NotNull Object @NotNull... parameters) throws NamelessException {
		Preconditions.checkArgument(!route.startsWith("/"), "Route must not start with a slash");

		final StringBuilder urlBuilder = new StringBuilder(this.baseUrl.toString());
		urlBuilder.append("/");
		urlBuilder.append(route);

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

		final @NotNull URL url;
		try {
			url = new URL(urlBuilder.toString());
		} catch (final MalformedURLException e) {
			throw new NamelessException("Error while building request URL: " + urlBuilder, e);
		}

		return makeConnection(url, null);
	}

	private void debug(final @NotNull String message, @NotNull Supplier<Object[]> argsSupplier) {
		if (this.debugLogger != null) {
			this.debugLogger.log(String.format(message, argsSupplier.get()));
		}
	}

	private @NotNull JsonObject makeConnection(final URL url, final @Nullable JsonObject postBody) throws NamelessException {
		final HttpURLConnection connection;
		final byte[] bytes;
		try {
			connection = (HttpURLConnection) url.openConnection();

			connection.setReadTimeout(this.timeout);
			connection.setConnectTimeout(this.timeout);

			debug("Making connection %s to url %s", () -> new Object[]{ postBody != null ? "POST" : "GET", url});

			connection.addRequestProperty("User-Agent", this.userAgent);
			connection.addRequestProperty("X-API-Key", this.apiKey);

			debug("Using User-Agent '%s'", () -> new Object[]{ this.userAgent });

			if (postBody != null) {
				debug("Post body below\n-----------------\n%s\n-----------------", () -> new Object[] { postBody });
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
				try (final InputStream in = connection.getErrorStream()) {
					if (in == null) {
						throw new NamelessException("Website sent empty response with code " + connection.getResponseCode());
					} else {
						bytes = getBytesFromInputStream(in);
					}
				}
			} else {
				try (final InputStream in = connection.getInputStream()) {
					bytes = getBytesFromInputStream(in);
				}
			}
		} catch (final IOException e) {
			final StringBuilder message = new StringBuilder("Network connection error (not a Nameless issue).");
			if (e.getMessage().contains("unable to find valid certification path to requested target")) {
				message.append("\n HINT: Your certificate is invalid or incomplete. Ensure your website uses a valid *full chain* SSL/TLS certificate.");
			}
			message.append(" IOException: ");
			message.append(e.getMessage());
			throw new NamelessException(message.toString(), e);
		}

		final String response = new String(bytes, StandardCharsets.UTF_8);

		if (response.equals("API is disabled")) {
			throw new ApiDisabledException();
		}

		debug("Website response below\n-----------------\n%s\n-----------------", () -> new Object[] { regularAsciiOnly(response) });

		JsonObject json;

		try {
			json = JsonParser.parseString(response).getAsJsonObject();
		} catch (final JsonSyntaxException | IllegalStateException e) {
			final StringBuilder printableResponseBuilder = new StringBuilder();
			if (response.length() > 5_000) {
				printableResponseBuilder.append(response, 0, 5_000);
				printableResponseBuilder.append("\n[response truncated to 5k characters]\n");
			} else {
				printableResponseBuilder.append(response);
				if (!response.endsWith("\n")) {
					printableResponseBuilder.append('\n');
				}
			}
			String printableResponse = regularAsciiOnly(printableResponseBuilder.toString());

			int code;
			try {
				code = connection.getResponseCode();
			} catch (final IOException e1) {
				throw new IllegalStateException("We've already made a connection, getting an IOException now is impossible", e1);
			}
			String message = e.getMessage() + "\n"
					+ "Unable to parse json. Received response code " + code + ". Website response:\n"
					+ "-----------------\n"
					+ printableResponse
					+ "-----------------\n";
			if (code == 301 || code == 302 || code == 303) {
				message += "HINT: The URL results in a redirect. If your URL uses http://, change to https://. If your website forces www., make sure to add www. to the url";
			} else if (code == 520 || code == 521) {
				message += "HINT: Status code 520/521 is sent by CloudFlare when the backend webserver is down or having issues.";
			} else if (printableResponse.contains("/aes.js")) {
				message += "HINT: It looks like requests are being blocked by your web server or a proxy. " +
						"This is a common occurrence with free web hosting services; they usually don't allow API access.";
			} else if (printableResponse.contains("<title>Please Wait... | Cloudflare</title>")) {
				message += "HINT: CloudFlare is blocking our request. Please see https://docs.namelessmc.com/cloudflare-apis";
			} else if (response.startsWith("\ufeff")) {
				message += "HINT: The website response contains invisible unicode characters.";
			}
			throw new NamelessException(message, e);
		}

		if (!json.has("error")) {
			throw new NamelessException("Unexpected response from website (missing json key 'error')");
		}

		if (json.get("error").getAsBoolean()) {
			@Nullable String meta = null;
			if (json.has("meta") && !json.get("meta").isJsonNull()) {
				meta = json.get("meta").toString();
			}
			throw new ApiError(json.get("code").getAsInt(), meta);
		}

		return json;
	}

	private static byte @NotNull[] getBytesFromInputStream(final @NotNull InputStream is) throws IOException {
	    final ByteArrayOutputStream os = new ByteArrayOutputStream();
	    final byte[] buffer = new byte[0xFFFF];
	    for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
	        os.write(buffer, 0, len);
	    }
	    return os.toByteArray();
	}

	private static @NotNull String regularAsciiOnly(@NotNull String message) {
		char[] chars = message.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			// only allow standard symbols, letters, numbers
			// look up an ascii table if you don't understand this if statement
			if (c >= ' ' && c <= '~' || c == '\n') {
				chars[i] = c;
			} else {
				chars[i] = '.';
			}
		}
		return new String(chars);
	}

}
