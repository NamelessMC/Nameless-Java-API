package com.namelessmc.java_api;

import com.google.gson.GsonBuilder;
import com.namelessmc.java_api.logger.ApiLogger;
import com.namelessmc.java_api.logger.PrintStreamLogger;
import com.namelessmc.java_api.logger.Slf4jLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.Authenticator;
import java.net.ProxySelector;
import java.net.URL;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;

public class NamelessApiBuilder {

	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
	private static final String DEFAULT_USER_AGENT = "Nameless-Java-API";
	private static final int DEFAULT_RESPONSE_SIZE_LIMIT = 32*1024*1024;

	private final @NotNull URL apiUrl;
	private final @NotNull String apiKey;
	private final @NotNull HttpClient.Builder httpClientBuilder;
	private final @NotNull GsonBuilder gsonBuilder;
	private @NotNull String userAgent = DEFAULT_USER_AGENT;
	private @Nullable ApiLogger debugLogger = null;
	private @Nullable Duration timeout = DEFAULT_TIMEOUT;
	private int responseSizeLimit = DEFAULT_RESPONSE_SIZE_LIMIT;

	NamelessApiBuilder(@NotNull URL apiUrl, @NotNull String apiKey) {
		this.apiUrl = apiUrl;
		this.apiKey = apiKey;
		this.httpClientBuilder = HttpClient.newBuilder();
		this.gsonBuilder = new GsonBuilder();
		this.gsonBuilder.disableHtmlEscaping();
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

	@Deprecated
	public @NotNull NamelessApiBuilder withTimeoutMillis(final int timeout) {
		this.timeout = Duration.ofMillis(timeout);
		return this;
	}

	public @NotNull NamelessApiBuilder withTimeout(final @Nullable Duration timeout) {
		this.timeout = timeout;
		return this;
	}

	public @NotNull NamelessApiBuilder withProxy(ProxySelector proxy) {
		this.httpClientBuilder.proxy(proxy);
		return this;
	}

	public @NotNull NamelessApiBuilder withAuthenticator(Authenticator authenticator) {
		this.httpClientBuilder.authenticator(authenticator);
		return this;
	}

	public @NotNull NamelessApiBuilder withPrettyJson() {
		gsonBuilder.setPrettyPrinting();
		return this;
	}

	public @NotNull NamelessApiBuilder withResponseSizeLimit(int responseSizeLimitBytes) {
		this.responseSizeLimit = responseSizeLimitBytes;
		return this;
	}

	public @NotNull NamelessAPI build() {
		return new NamelessAPI(
				new RequestHandler(
						this.apiUrl,
						this.apiKey,
						this.httpClientBuilder.build(),
						this.gsonBuilder.create(),
						this.userAgent,
						this.debugLogger,
						this.timeout,
						this.responseSizeLimit
				)
		);
	}

}
