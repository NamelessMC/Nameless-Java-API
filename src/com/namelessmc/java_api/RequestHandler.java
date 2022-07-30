package com.namelessmc.java_api;

import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.MutableRequest;
import com.google.common.base.Ascii;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.namelessmc.java_api.exception.ApiError;
import com.namelessmc.java_api.exception.ApiException;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.logger.ApiLogger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RequestHandler {

	private final @NonNull URL apiUrl;
	private final @NonNull Methanol httpClient;
	private final @Nullable ApiLogger debugLogger;
	private final @NonNull Gson gson;
	private final int responseLengthLimit;

	RequestHandler(final @NonNull URL apiUrl,
				   final @NonNull Methanol httpClient,
				   final @NonNull Gson gson,
				   final @Nullable ApiLogger debugLogger,
				   final int responseLengthLimit) {
		this.apiUrl = Objects.requireNonNull(apiUrl, "API URL is null");
		this.httpClient = Objects.requireNonNull(httpClient, "http client is null");
		this.gson = gson;
		this.debugLogger = debugLogger;
		this.responseLengthLimit = responseLengthLimit;
	}

	public Gson gson() {
		return this.gson;
	}

	public @NonNull JsonObject post(final @NonNull String route,
									final @NonNull JsonObject postData) throws NamelessException {
		return makeConnection(route, postData);
	}

	public @NonNull JsonObject get(final @NonNull String route,
								   final @NonNull Object @NonNull... parameters) throws NamelessException {
		final StringBuilder urlBuilder = new StringBuilder(route);

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

		return makeConnection(urlBuilder.toString(), null);
	}

	private void debug(final @NonNull String message) {
		if (this.debugLogger != null) {
			this.debugLogger.log(message);
		}
	}

	private void debug(final @NonNull Supplier<String> messageSupplier) {
		if (this.debugLogger != null) {
			this.debugLogger.log(messageSupplier.get());
		}
	}

	private @NonNull JsonObject makeConnection(final @NonNull String route,
											   final @Nullable JsonObject postBody) throws NamelessException {
		Preconditions.checkArgument(!route.startsWith("/"), "Route must not start with a slash");
		final URI uri = URI.create(this.apiUrl + route);
		if (uri.getHost() == null) {
			throw new NamelessException("URI has empty host, does it contain invalid characters? Please note that although underscores are " +
					"legal in domain names, the Java URI class (and the Java HttpClient) does not accept them, because it uses the specification " +
					"for 'host names' not 'domain names'.");
		}
		final MutableRequest request = MutableRequest.create(uri);

		debug(() -> "Making connection " + (postBody != null ? "POST" : "GET") + " to " + request.uri());

		final long requestStartTime = System.currentTimeMillis();

		if (postBody != null) {
			byte[] postBytes = gson.toJson(postBody).getBytes(StandardCharsets.UTF_8);
			request.POST(HttpRequest.BodyPublishers.ofByteArray(postBytes));
			request.header("Content-Type", "application/json");

			debug(() -> "POST request body:\n" + new String(postBytes, StandardCharsets.UTF_8));
		} else {
			request.GET();
		}

		request.header("Accept", "application/json");

		int statusCode;
		String responseBody;
		try {
			HttpResponse<InputStream> httpResponse = httpClient.send(request,
					HttpResponse.BodyHandlers.ofInputStream());
			statusCode = httpResponse.statusCode();
			responseBody = getBodyAsString(httpResponse);
		} catch (final IOException e) {
			final @Nullable String exceptionMessage = e.getMessage();
			final StringBuilder message = new StringBuilder();
			message.append("Network connection error (not a Nameless issue). ");
			message.append(e.getClass().getSimpleName());
			message.append(": ");
			message.append(exceptionMessage);
			if (exceptionMessage != null) {
				if (exceptionMessage.contains("unable to find valid certification path to requested target")) {
					message.append("\nHINT: Your HTTPS certificate is probably valid, but is it complete? Ensure your website uses a valid *full chain* SSL/TLS certificate.");
				} else if (exceptionMessage.contains("No subject alternative DNS name matching")) {
					message.append("\nHINT: Is your HTTPS certificate valid? Is it for the correct domain?");
				} else if (exceptionMessage.contains("Connect timed out")) {
					message.append("\nHINT: Is a webserver running at the provided domain? Are we blocked by a firewall? Is your webserver fast enough?");
				} else if (exceptionMessage.contains("Connection refused")) {
					message.append("\nHINT: Is the domain correct? Is your webserver running? Are we blocked by a firewall?");
				} else if (exceptionMessage.contains("timed out")) {
					// All timeouts are set to the same value, so we only need to print one.
					long seconds = httpClient.connectTimeout().orElseThrow().getSeconds();
					message.append("\nHINT: The website responded too slow, no response after waiting for ");
					message.append((System.currentTimeMillis() - requestStartTime) / 1000);
					message.append(" seconds.");
				}
			}

			throw new NamelessException(message.toString(), e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		debug(() -> "Website response body, after " + (System.currentTimeMillis() - requestStartTime) + "ms:\n" + regularAsciiOnly(responseBody));

		if (responseBody.length() == 0) {
			throw new NamelessException("Website sent empty response with status code " + statusCode);
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
				message.append("HINT: Status code 520/521 is sent by CloudFlare when the backend webserver is down or having issues. Check your webserver and CloudFlare configuration.\n");
			} else if (responseBody.contains("/aes.js")) {
				message.append("HINT: It looks like requests are being blocked by your web server or a proxy. ");
				message.append("This is a common occurrence with free web hosting services; they usually don't allow API access.\n");
			} else if (responseBody.contains("<title>Please Wait... | Cloudflare</title>") ||
					responseBody.contains("#cf-bubbles") ||
					responseBody.contains("_cf_ch1_opt")) {
				message.append("HINT: CloudFlare is blocking our request. Please see https://docs.namelessmc.com/cloudflare-api\n");
			} else if (responseBody.startsWith("\ufeff")) {
				message.append("HINT: The website response contains invisible unicode characters. This seems to be caused by Partydragen's Store module, we have no idea why.\n");
			}

			message.append("Website response, after ");
			message.append(System.currentTimeMillis() - requestStartTime);
			message.append("ms:\n");
			message.append("-----------------\n");
			int totalLengthLimit = 1500; // fit in a Discord message
			String printableResponse = regularAsciiOnly(responseBody);
			message.append(Ascii.truncate(printableResponse, totalLengthLimit, "[truncated]\n"));
			if (message.charAt(message.length() - 1) != '\n') {
				message.append('\n');
			}

			throw new NamelessException(message.toString(), e);
		}

		if (json.has("error")) {
			final String errorString = json.get("error").getAsString();
			if (errorString.equals("true")) {
				throw new NamelessException("Error string is 'true', are you using an older NamelessMC version?");
			}
			final ApiError apiError = ApiError.fromString(errorString);
			if (apiError == null) {
				throw new NamelessException("Unknown API error: " + errorString);
			}

			final String meta;
			if (json.has("meta") && !json.get("meta").isJsonNull()) {
				meta = json.get("meta").toString();
			} else {
				meta = null;
			}
			throw new ApiException(apiError, meta);
		}

		return json;
	}

	private String getBodyAsString(HttpResponse<InputStream> response) throws IOException {
		try (InputStream in = response.body();
				InputStream limited = ByteStreams.limit(in, this.responseLengthLimit)) {
			byte[] bytes = limited.readAllBytes();
			if (bytes.length == this.responseLengthLimit) {
				throw new IOException("Response larger than limit of " + this.responseLengthLimit + " bytes.");
			}
			return new String(bytes, StandardCharsets.UTF_8);
		}
	}

	private static @NonNull String regularAsciiOnly(@NonNull String message) {
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
