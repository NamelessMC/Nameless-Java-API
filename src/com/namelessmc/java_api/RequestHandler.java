package com.namelessmc.java_api;

import com.google.common.base.Ascii;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.namelessmc.java_api.exception.ApiDisabledException;
import com.namelessmc.java_api.logger.ApiLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RequestHandler {

	private final @NotNull URL baseUrl;
	private final @NotNull String apiKey;
	private final @NotNull HttpClient httpClient;
	private final @NotNull String userAgent;
	private final @Nullable ApiLogger debugLogger;
	private final @Nullable Duration timeout;
	private final @NotNull Gson gson;

	RequestHandler(final @NotNull URL baseUrl,
				   final @NotNull String apiKey,
				   final @NotNull HttpClient httpClient,
				   final @NotNull Gson gson,
				   final @NotNull String userAgent,
				   final @Nullable ApiLogger debugLogger,
				   final @Nullable Duration timeout) {
		this.baseUrl = Objects.requireNonNull(baseUrl, "Base URL is null");
		this.apiKey = Objects.requireNonNull(apiKey, "Api key is null");
		this.httpClient = Objects.requireNonNull(httpClient, "http client is null");
		this.gson = gson;
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
		URI uri = URI.create(this.baseUrl + "/" + route);
		return makeConnection(uri, postData);
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

		final @NotNull URI uri = URI.create(urlBuilder.toString());
		return makeConnection(uri, null);
	}

	private void debug(final @NotNull String message, @NotNull Supplier<Object[]> argsSupplier) {
		if (this.debugLogger != null) {
			this.debugLogger.log(String.format(message, argsSupplier.get()));
		}
	}

	private @NotNull JsonObject makeConnection(final URI uri, final @Nullable JsonObject postBody) throws NamelessException {
		HttpRequest.Builder reqBuilder = HttpRequest.newBuilder(uri);
		if (timeout != null) {
			reqBuilder.timeout(timeout);
		}

		debug("Making connection %s to url %s", () -> new Object[]{ postBody != null ? "POST" : "GET", uri});

		if (postBody != null) {
			byte[] postBytes = gson.toJson(postBody).getBytes(StandardCharsets.UTF_8);
			reqBuilder.POST(HttpRequest.BodyPublishers.ofByteArray(postBytes));
			reqBuilder.header("Content-Type", "application/json");

			debug("Post body below\n-----------------\n%s\n-----------------",
					() -> new Object[] { new String(postBytes, StandardCharsets.UTF_8) });
		} else {
			reqBuilder.GET();
		}

		reqBuilder.header("User-Agent", this.userAgent);
		reqBuilder.header("X-API-Key", this.apiKey);

		HttpRequest httpRequest = reqBuilder.build();

		int statusCode;
		String responseBody;
		try {
			HttpResponse<String> httpResponse = httpClient.send(httpRequest,
					HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			statusCode = httpResponse.statusCode();
			responseBody = httpResponse.body();
		} catch (final IOException e) {
			final StringBuilder message = new StringBuilder("Network connection error (not a Nameless issue).");
			if (e.getMessage().contains("unable to find valid certification path to requested target")) {
				message.append("\n HINT: Your certificate is invalid or incomplete. Ensure your website uses a valid *full chain* SSL/TLS certificate.");
			}
			message.append(" IOException: ");
			message.append(e.getMessage());
			throw new NamelessException(message.toString(), e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		debug("Website response below\n-----------------\n%s\n-----------------",
				() -> new Object[] { regularAsciiOnly(responseBody) });

		if (responseBody.length() == 0) {
			throw new NamelessException("Website sent empty response with status code " + statusCode);
		}

		if (responseBody.equals("API is disabled")) {
			throw new ApiDisabledException();
		}

		JsonObject json;

		try {
			json = JsonParser.parseString(responseBody).getAsJsonObject();
		} catch (final JsonSyntaxException | IllegalStateException e) {
			StringBuilder message = new StringBuilder();
			message.append("Website returned invalid response with code ");
			message.append(statusCode);
			message.append(".\n");
			if (statusCode >= 301 && statusCode <= 303) {
				message.append("HINT: The URL results in a redirect. If your URL uses http://, change to https://. If your website forces www., make sure to add www. to the url.\n");
			} else if (statusCode == 520 || statusCode == 521) {
				message.append("HINT: Status code 520/521 is sent by CloudFlare when the backend webserver is down or having issues.\n");
			} else if (responseBody.contains("/aes.js")) {
				message.append("HINT: It looks like requests are being blocked by your web server or a proxy. ");
				message.append("This is a common occurrence with free web hosting services; they usually don't allow API access.\n");
			} else if (responseBody.contains("<title>Please Wait... | Cloudflare</title>")) {
				message.append("HINT: CloudFlare is blocking our request. Please see https://docs.namelessmc.com/cloudflare-apis\n");
			} else if (responseBody.startsWith("\ufeff")) {
				message.append("HINT: The website response contains invisible unicode characters.\n");
			}

			message.append("Website response:\n");
			message.append("-----------------\n");
			int totalLengthLimit = 1950; // fit in a Discord message with safety margin
			String printableResponse = regularAsciiOnly(responseBody);
			message.append(Ascii.truncate(printableResponse, totalLengthLimit - printableResponse.length(), "[truncated]\n"));
			if (message.charAt(message.length()) != '\n') {
				message.append('\n');
			}

			throw new NamelessException(message.toString(), e);
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
