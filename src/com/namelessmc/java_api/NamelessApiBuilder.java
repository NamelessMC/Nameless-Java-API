package com.namelessmc.java_api;

import com.namelessmc.java_api.logger.ApiLogger;
import com.namelessmc.java_api.logger.PrintStreamLogger;
import com.namelessmc.java_api.logger.Slf4jLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class NamelessApiBuilder {

	private static final int DEFAULT_TIMEOUT = 5000;
	private static final String DEFAULT_USER_AGENT = "Nameless-Java-API";

	@NotNull
	private String userAgent = DEFAULT_USER_AGENT;
	@Nullable
	private URL apiUrl = null;
	private @Nullable ApiLogger debugLogger = null;
	private int timeout = DEFAULT_TIMEOUT;

	NamelessApiBuilder() {
	}

	public @NotNull NamelessApiBuilder apiUrl(@NotNull final URL apiUrl) {
		this.apiUrl = apiUrl;
		return this;
	}

	public @NotNull NamelessApiBuilder apiUrl(@NotNull final String apiUrl) throws MalformedURLException {
		return apiUrl(new URL(apiUrl));
	}

	/**
	 * Connect to a HTTPS website, not in a subdirectory.
	 *
	 * @param host   hostname, for example namelessmc.com
	 * @param apiKey api key
	 * @throws MalformedURLException If the URL is malformed after building it using the provided host and api key
	 */
	public @NotNull NamelessApiBuilder apiUrl(@NotNull final String host, @NotNull final String apiKey) throws MalformedURLException {
		Objects.requireNonNull(host, "Host is null");
		Objects.requireNonNull(apiKey, "Api key is null");
		return apiUrl("https://" + host + "/index.php?route=/api/v2/" + apiKey);
	}

	public @NotNull NamelessApiBuilder userAgent(@NotNull final String userAgent) {
		this.userAgent = Objects.requireNonNull(userAgent, "User agent is null");
		return this;
	}

	public @NotNull NamelessApiBuilder debug(final boolean debug) {
		if (debug) {
			return this.withStdErrDebugLogging();
		} else {
			this.debugLogger = null;
			return this;
		}
	}

	public @NotNull NamelessApiBuilder withStdErrDebugLogging() {
		this.debugLogger = PrintStreamLogger.DEFAULT_INSTANCE;
		return this;
	}

	public @NotNull NamelessApiBuilder withSlf4jDebugLogging() {
		this.debugLogger = Slf4jLogger.DEFAULT_INSTANCE;
		return this;
	}

	public @NotNull NamelessApiBuilder withCustomDebugLogger(final @Nullable ApiLogger debugLogger) {
		this.debugLogger = debugLogger;
		return this;
	}

	public @NotNull NamelessApiBuilder withTimeoutMillis(final int timeout) {
		this.timeout = timeout;
		return this;
	}

	public @NotNull NamelessAPI build() {
		if (this.apiUrl == null) {
			throw new IllegalStateException("No API URL specified");
		}

		return new NamelessAPI(new RequestHandler(this.apiUrl, this.userAgent, this.debugLogger, this.timeout));
	}

}
