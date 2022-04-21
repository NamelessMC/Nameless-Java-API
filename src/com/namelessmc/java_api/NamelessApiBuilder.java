package com.namelessmc.java_api;

import com.github.mizosoft.methanol.Methanol;
import com.google.gson.GsonBuilder;
import com.namelessmc.java_api.logger.ApiLogger;
import com.namelessmc.java_api.logger.PrintStreamLogger;
import com.namelessmc.java_api.logger.Slf4jLogger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.ProxySelector;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.Executor;

public class NamelessApiBuilder {

	private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
	private static final String DEFAULT_USER_AGENT = "Nameless-Java-API";
	private static final int DEFAULT_RESPONSE_SIZE_LIMIT = 32*1024*1024;

	private final @NonNull URL apiUrl;
	private final @NonNull String apiKey;

	private final @NonNull GsonBuilder gsonBuilder;
	private @Nullable ApiLogger debugLogger = null;
	private final Methanol.@NonNull Builder httpClientBuilder;
	private int responseSizeLimit = DEFAULT_RESPONSE_SIZE_LIMIT;

	NamelessApiBuilder(final @NonNull URL apiUrl,
					   final @NonNull String apiKey) {
		try {
			this.apiUrl = apiUrl.toString().endsWith("/") ? apiUrl : new URL(apiUrl + "/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		this.apiKey = apiKey;

		this.gsonBuilder = new GsonBuilder();
		this.gsonBuilder.disableHtmlEscaping();

		this.httpClientBuilder = Methanol.newBuilder()
				.defaultHeader("X-Api-Key", this.apiKey)
				.userAgent(DEFAULT_USER_AGENT)
				.readTimeout(DEFAULT_TIMEOUT)
				.requestTimeout(DEFAULT_TIMEOUT)
				.connectTimeout(DEFAULT_TIMEOUT)
				.autoAcceptEncoding(true);
	}

	public @NonNull NamelessApiBuilder userAgent(final @NonNull String userAgent) {
		this.httpClientBuilder.userAgent(userAgent);
		return this;
	}

	public @NonNull NamelessApiBuilder debug(final boolean debug) {
		if (debug) {
			return this.withStdErrDebugLogging();
		} else {
			this.debugLogger = null;
			return this;
		}
	}

	@Deprecated
	public @NonNull NamelessApiBuilder withStdErrDebugLogging() {
		this.debugLogger = PrintStreamLogger.DEFAULT_INSTANCE;
		return this;
	}

	public @NonNull NamelessApiBuilder stdErrDebugLogger() {
		this.debugLogger = PrintStreamLogger.DEFAULT_INSTANCE;
		return this;
	}

	@Deprecated
	public @NonNull NamelessApiBuilder withSlf4jDebugLogging() {
		this.debugLogger = Slf4jLogger.DEFAULT_INSTANCE;
		return this;
	}

	public @NonNull NamelessApiBuilder slf4jDebugLogger() {
		this.debugLogger = Slf4jLogger.DEFAULT_INSTANCE;
		return this;
	}

	@Deprecated
	public @NonNull NamelessApiBuilder withCustomDebugLogger(final @Nullable ApiLogger debugLogger) {
		this.debugLogger = debugLogger;
		return this;
	}

	public @NonNull NamelessApiBuilder customDebugLogger(final @Nullable ApiLogger debugLogger) {
		this.debugLogger = debugLogger;
		return this;
	}

	@Deprecated
	public @NonNull NamelessApiBuilder withTimeoutMillis(final int timeout) {
		return this.withTimeout(Duration.ofMillis(timeout));
	}

	@Deprecated
	public @NonNull NamelessApiBuilder withTimeout(final @NonNull Duration timeout) {
		this.httpClientBuilder.readTimeout(timeout)
				.requestTimeout(timeout)
				.connectTimeout(timeout);
		return this;
	}

	public @NonNull NamelessApiBuilder timeout(final @NonNull Duration timeout) {
		this.httpClientBuilder.readTimeout(timeout)
				.requestTimeout(timeout)
				.connectTimeout(timeout);
		return this;
	}

	public @NonNull NamelessApiBuilder withProxy(final ProxySelector proxy) {
		this.httpClientBuilder.proxy(proxy);
		return this;
	}

	@Deprecated
	public @NonNull NamelessApiBuilder proxy(final ProxySelector proxy) {
		this.httpClientBuilder.proxy(proxy);
		return this;
	}

	@Deprecated
	public @NonNull NamelessApiBuilder withAuthenticator(final Authenticator authenticator) {
		this.httpClientBuilder.authenticator(authenticator);
		return this;
	}

	public @NonNull NamelessApiBuilder authenticator(final Authenticator authenticator) {
		this.httpClientBuilder.authenticator(authenticator);
		return this;
	}

	@Deprecated
	public @NonNull NamelessApiBuilder withPrettyJson() {
		gsonBuilder.setPrettyPrinting();
		return this;
	}

	public @NonNull NamelessApiBuilder pettyJsonRequests() {
		gsonBuilder.setPrettyPrinting();
		return this;
	}

	@Deprecated
	public @NonNull NamelessApiBuilder withResponseSizeLimit(int responseSizeLimitBytes) {
		this.responseSizeLimit = responseSizeLimitBytes;
		return this;
	}

	public @NonNull NamelessApiBuilder responseSizeLimit(int responseSizeLimitBytes) {
		this.responseSizeLimit = responseSizeLimitBytes;
		return this;
	}

	public @NonNull NamelessApiBuilder executor(final @NonNull Executor executor) {
		this.httpClientBuilder.executor(executor);
		return this;
	}

	public @NonNull NamelessAPI build() {
		return new NamelessAPI(
				new RequestHandler(
						this.apiUrl,
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
