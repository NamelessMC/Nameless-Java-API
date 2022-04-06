package com.namelessmc.java_api;

import com.github.mizosoft.methanol.Methanol;
import com.google.gson.GsonBuilder;
import com.namelessmc.java_api.logger.ApiLogger;
import com.namelessmc.java_api.logger.PrintStreamLogger;
import com.namelessmc.java_api.logger.Slf4jLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.Authenticator;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.Executor;

public class NamelessApiBuilder {

	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
	private static final String DEFAULT_USER_AGENT = "Nameless-Java-API";
	private static final int DEFAULT_RESPONSE_SIZE_LIMIT = 32*1024*1024;

	private final @NotNull URL apiUrl;
	private final @NotNull String apiKey;

	private final @NotNull GsonBuilder gsonBuilder;
	private @Nullable ApiLogger debugLogger = null;
	private final @NotNull Methanol.Builder httpClientBuilder;
	private int responseSizeLimit = DEFAULT_RESPONSE_SIZE_LIMIT;

	NamelessApiBuilder(@NotNull URL apiUrl, @NotNull String apiKey) {
		this.apiUrl = apiUrl;
		this.apiKey = apiKey;

		this.gsonBuilder = new GsonBuilder();
		this.gsonBuilder.disableHtmlEscaping();

		try {
			this.httpClientBuilder = Methanol.newBuilder()
					.baseUri(apiUrl.toURI())
					.defaultHeader("X-Api-Key", apiKey)
					.userAgent(DEFAULT_USER_AGENT)
					.readTimeout(DEFAULT_TIMEOUT)
					.requestTimeout(DEFAULT_TIMEOUT)
					.connectTimeout(DEFAULT_TIMEOUT)
					.autoAcceptEncoding(true);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public @NotNull NamelessApiBuilder userAgent(@NotNull final String userAgent) {
		this.httpClientBuilder.userAgent(userAgent);
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

	@Deprecated
	public @NotNull NamelessApiBuilder withStdErrDebugLogging() {
		this.debugLogger = PrintStreamLogger.DEFAULT_INSTANCE;
		return this;
	}

	public @NotNull NamelessApiBuilder stdErrDebugLogger() {
		this.debugLogger = PrintStreamLogger.DEFAULT_INSTANCE;
		return this;
	}

	@Deprecated
	public @NotNull NamelessApiBuilder withSlf4jDebugLogging() {
		this.debugLogger = Slf4jLogger.DEFAULT_INSTANCE;
		return this;
	}

	public @NotNull NamelessApiBuilder slf4jDebugLogger() {
		this.debugLogger = Slf4jLogger.DEFAULT_INSTANCE;
		return this;
	}

	@Deprecated
	public @NotNull NamelessApiBuilder withCustomDebugLogger(final @Nullable ApiLogger debugLogger) {
		this.debugLogger = debugLogger;
		return this;
	}

	public @NotNull NamelessApiBuilder customDebugLogger(final @Nullable ApiLogger debugLogger) {
		this.debugLogger = debugLogger;
		return this;
	}

	@Deprecated
	public @NotNull NamelessApiBuilder withTimeoutMillis(final int timeout) {
		return this.withTimeout(Duration.ofMillis(timeout));
	}

	@Deprecated
	public @NotNull NamelessApiBuilder withTimeout(final @NotNull Duration timeout) {
		this.httpClientBuilder.readTimeout(timeout)
				.requestTimeout(timeout)
				.connectTimeout(timeout);
		return this;
	}

	public @NotNull NamelessApiBuilder timeout(final @NotNull Duration timeout) {
		this.httpClientBuilder.readTimeout(timeout)
				.requestTimeout(timeout)
				.connectTimeout(timeout);
		return this;
	}

	public @NotNull NamelessApiBuilder withProxy(ProxySelector proxy) {
		this.httpClientBuilder.proxy(proxy);
		return this;
	}

	@Deprecated
	public @NotNull NamelessApiBuilder proxy(ProxySelector proxy) {
		this.httpClientBuilder.proxy(proxy);
		return this;
	}

	@Deprecated
	public @NotNull NamelessApiBuilder withAuthenticator(Authenticator authenticator) {
		this.httpClientBuilder.authenticator(authenticator);
		return this;
	}

	public @NotNull NamelessApiBuilder authenticator(Authenticator authenticator) {
		this.httpClientBuilder.authenticator(authenticator);
		return this;
	}

	@Deprecated
	public @NotNull NamelessApiBuilder withPrettyJson() {
		gsonBuilder.setPrettyPrinting();
		return this;
	}

	public @NotNull NamelessApiBuilder pettyJsonRequests() {
		gsonBuilder.setPrettyPrinting();
		return this;
	}

	@Deprecated
	public @NotNull NamelessApiBuilder withResponseSizeLimit(int responseSizeLimitBytes) {
		this.responseSizeLimit = responseSizeLimitBytes;
		return this;
	}

	public @NotNull NamelessApiBuilder responseSizeLimit(int responseSizeLimitBytes) {
		this.responseSizeLimit = responseSizeLimitBytes;
		return this;
	}

	public @NotNull NamelessApiBuilder executor(final @NotNull Executor executor) {
		this.httpClientBuilder.executor(executor);
		return this;
	}

	public @NotNull NamelessAPI build() {
		return new NamelessAPI(
				new RequestHandler(
						this.httpClientBuilder.build(),
						this.gsonBuilder.create(),
						this.debugLogger,
						this.responseSizeLimit
				),
				this.apiUrl,
				this.apiKey
		);
	}

}
