package com.namelessmc.java_api;

import com.namelessmc.java_api.logger.ApiLogger;
import com.namelessmc.java_api.logger.PrintStreamLogger;
import com.namelessmc.java_api.logger.Slf4jLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.Objects;

public class NamelessApiBuilder {

	private static final int DEFAULT_TIMEOUT = 5000;
	private static final String DEFAULT_USER_AGENT = "Nameless-Java-API";

	private final @NotNull URL apiUrl;
	private final @NotNull String apiKey;
	private @NotNull String userAgent = DEFAULT_USER_AGENT;
	private @Nullable ApiLogger debugLogger = null;
	private int timeout = DEFAULT_TIMEOUT;

	NamelessApiBuilder(@NotNull URL apiUrl, @NotNull String apiKey) {
		this.apiUrl = apiUrl;
		this.apiKey = apiKey;
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
		return new NamelessAPI(new RequestHandler(this.apiUrl, this.apiKey, this.userAgent, this.debugLogger, this.timeout));
	}

}
