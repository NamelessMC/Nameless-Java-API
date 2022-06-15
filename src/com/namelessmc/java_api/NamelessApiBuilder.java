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
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;

public class NamelessApiBuilder {

	private final @NonNull URL apiUrl;
	private final @NonNull String apiKey;

	private Duration timeout = Duration.ofSeconds(10);
	private int responseSizeLimit = 32*1024*1024;
	private String userAgent = "Nameless-Java-API";
	private @Nullable ApiLogger debugLogger = null;
	private @Nullable ProxySelector proxy = null;
	private @Nullable Authenticator authenticator = null;
	private HttpClient.@Nullable Version httpVersion = null;

	private boolean pettyJsonRequests = false;

	NamelessApiBuilder(final @NonNull URL apiUrl,
					   final @NonNull String apiKey) {
		try {
			this.apiUrl = apiUrl.toString().endsWith("/") ? apiUrl : new URL(apiUrl + "/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		this.apiKey = apiKey;
	}

	public NamelessApiBuilder userAgent(final String userAgent) {
		this.userAgent = userAgent;
		return this;
	}

	public NamelessApiBuilder stdErrDebugLogger() {
		this.debugLogger = PrintStreamLogger.DEFAULT_INSTANCE;
		return this;
	}

	public NamelessApiBuilder slf4jDebugLogger() {
		this.debugLogger = Slf4jLogger.DEFAULT_INSTANCE;
		return this;
	}

	public NamelessApiBuilder customDebugLogger(final @Nullable ApiLogger debugLogger) {
		this.debugLogger = debugLogger;
		return this;
	}

	public NamelessApiBuilder timeout(final Duration timeout) {
		this.timeout = Objects.requireNonNull(timeout);
		return this;
	}

	public NamelessApiBuilder withProxy(final @Nullable ProxySelector proxy) {
		this.proxy = proxy;
		return this;
	}

	public NamelessApiBuilder authenticator(final @Nullable Authenticator authenticator) {
		this.authenticator = authenticator;
		return this;
	}

	public NamelessApiBuilder pettyJsonRequests() {
		this.pettyJsonRequests = true;
		return this;
	}

	public NamelessApiBuilder responseSizeLimit(int responseSizeLimitBytes) {
		this.responseSizeLimit = responseSizeLimitBytes;
		return this;
	}

	public NamelessApiBuilder httpversion(final HttpClient. @Nullable Version httpVersion) {
		this.httpVersion = httpVersion;
		return this;
	}

	public NamelessAPI build() {
		final Methanol.Builder methanolBuilder = Methanol.newBuilder()
				.defaultHeader("Authorization", "Bearer " + this.apiKey)
				.userAgent(this.userAgent)
				.autoAcceptEncoding(true)
				.readTimeout(this.timeout)
				.requestTimeout(this.timeout)
				.connectTimeout(this.timeout)
				.headersTimeout(this.timeout);
		if (this.proxy != null) {
			methanolBuilder.proxy(this.proxy);
		}
		if (this.authenticator != null) {
			methanolBuilder.authenticator(this.authenticator);
		}
		if (this.httpVersion != null) {
			methanolBuilder.version(this.httpVersion);
		}

		GsonBuilder gsonBuilder = new GsonBuilder()
				.disableHtmlEscaping();

		if (this.pettyJsonRequests) {
			gsonBuilder.setPrettyPrinting();
		}

		return new NamelessAPI(
				new RequestHandler(
						this.apiUrl,
						methanolBuilder.build(),
						gsonBuilder.create(),
						this.debugLogger,
						this.responseSizeLimit
				),
				this.apiUrl,
				this.apiKey
		);
	}

}
