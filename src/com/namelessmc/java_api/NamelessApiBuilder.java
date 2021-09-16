package com.namelessmc.java_api;

import com.namelessmc.java_api.logger.ApiLogger;
import com.namelessmc.java_api.logger.PrintStreamLogger;
import com.namelessmc.java_api.logger.Slf4jLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;

public class NamelessApiBuilder {

    private static final String DEFAULT_USER_AGENT = "Nameless-Java-API";

    private String userAgent = DEFAULT_USER_AGENT;
    private URL apiUrl = null;
    private Optional<ApiLogger> debugLogger = Optional.empty();
    private int timeout = 5000;

    NamelessApiBuilder() {
    }

    public NamelessApiBuilder apiUrl(final URL apiUrl) {
        this.apiUrl = apiUrl;
        return this;
    }

    public NamelessApiBuilder apiUrl(final String apiUrl) throws MalformedURLException {
        return apiUrl(new URL(apiUrl));
    }

    /**
     * Connect to a HTTPS website, not in a subdirectory.
     *
     * @param host   hostname, for example namelessmc.com
     * @param apiKey api key
     * @throws MalformedURLException
     */
    public NamelessApiBuilder apiUrl(final String host, final String apiKey) throws MalformedURLException {
        return apiUrl("https://" + host + "/index.php?route=/api/v2/" + apiKey);
    }

    public NamelessApiBuilder userAgent(final String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    @Deprecated
    public NamelessApiBuilder debug(final boolean debug) {
        if (debug) {
            return this.withStdErrDebugLogging();
        } else {
            this.debugLogger = Optional.empty();
            return this;
        }
    }

    public NamelessApiBuilder withStdErrDebugLogging() {
        this.debugLogger = Optional.of(PrintStreamLogger.DEFAULT_INSTANCE);
        return this;
    }

    public NamelessApiBuilder withSlf4jDebugLogging() {
        this.debugLogger = Optional.of(Slf4jLogger.DEFAULT_INSTANCE);
        return this;
    }

    public NamelessApiBuilder withCustomDebugLogger(final ApiLogger debugLogger) {
        this.debugLogger = Optional.of(Objects.requireNonNull(debugLogger, "Provided debug logger is null"));
        return this;
    }

    public NamelessApiBuilder withCustomDebugLogger(final Optional<ApiLogger> debugLogger) {
        this.debugLogger = Objects.requireNonNull(debugLogger);
        return this;
    }

    public NamelessApiBuilder withTimeoutMillis(final int timeout) {
        this.timeout = timeout;
        return this;
    }

    public NamelessAPI build() {
        if (this.apiUrl == null) {
            throw new IllegalStateException("No API URL specified");
        }

        return new NamelessAPI(new RequestHandler(this.apiUrl, this.userAgent, this.debugLogger, this.timeout));
    }

}
