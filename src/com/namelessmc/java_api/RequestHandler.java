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
import com.namelessmc.java_api.exception.ApiDisabledException;
import com.namelessmc.java_api.logger.ApiLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

	private final @NotNull URL apiUrl;
	private final @NotNull Methanol httpClient;
	private final @Nullable ApiLogger debugLogger;
	private final @NotNull Gson gson;
	private final int responseLengthLimit;

	RequestHandler(final @NotNull URL apiUrl,
				   final @NotNull Methanol httpClient,
				   final @NotNull Gson gson,
				   final @Nullable ApiLogger debugLogger,
				   final int responseLengthLimit) {
		this.apiUrl = Objects.requireNonNull(apiUrl, "API URL is null");
		this.httpClient = Objects.requireNonNull(httpClient, "http client is null");
		this.gson = gson;
		this.debugLogger = debugLogger;
		this.responseLengthLimit = responseLengthLimit;
	}

	public @NotNull JsonObject post(final @NotNull String route,
									final @NotNull JsonObject postData) throws NamelessException {
		return makeConnection(route, postData);
	}

	public @NotNull JsonObject get(final @NotNull String route,
								   final @NotNull Object @NotNull... parameters) throws NamelessException {
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

	private void debug(final @NotNull String message,
					   final @NotNull Supplier<Object[]> argsSupplier) {
		if (this.debugLogger != null) {
			this.debugLogger.log(String.format(message, argsSupplier.get()));
		}
	}

	private @NotNull JsonObject makeConnection(final @NotNull String route,
											   final @Nullable JsonObject postBody) throws NamelessException {
		Preconditions.checkArgument(!route.startsWith("/"), "Route must not start with a slash");
		final MutableRequest request = MutableRequest.create(URI.create(this.apiUrl.toString() + route));

		debug("Making connection %s to %s",
				() -> new Object[]{ postBody != null ? "POST" : "GET", request.uri()});

		if (postBody != null) {
			byte[] postBytes = gson.toJson(postBody).getBytes(StandardCharsets.UTF_8);
			request.POST(HttpRequest.BodyPublishers.ofByteArray(postBytes));
			request.header("Content-Type", "application/json");

			debug("Post body below\n-----------------\n%s\n-----------------",
					() -> new Object[] { new String(postBytes, StandardCharsets.UTF_8) });
		} else {
			request.GET();
		}

		int statusCode;
		String responseBody;
		try {
			HttpResponse<InputStream> httpResponse = httpClient.send(request,
					HttpResponse.BodyHandlers.ofInputStream());
			statusCode = httpResponse.statusCode();
			responseBody = getBodyAsString(httpResponse);
		} catch (final IOException e) {
			final @Nullable String exceptionMessage = e.getMessage();
			final StringBuilder message = new StringBuilder("Network connection error (not a Nameless issue).");
			if (exceptionMessage != null &&
					exceptionMessage.contains("unable to find valid certification path to requested target")) {
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
				message.append("HINT: The website response contains invisible unicode characters. This seems to be caused by Partydragen's Store module, we have no idea why.\n");
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
