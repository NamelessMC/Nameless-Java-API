package com.namelessmc.java_api;

import java.net.MalformedURLException;
import java.net.URL;

public class NamelessApiBuilder {

	private static final String DEFAULT_USER_AGENT = "Nameless-Java-API";

	private String userAgent = DEFAULT_USER_AGENT;
	private URL apiUrl = null;
	private boolean debug = false;

	NamelessApiBuilder() {
	}

	public NamelessApiBuilder apiUrl(final URL apiUrl) {
		this.apiUrl = apiUrl;
		return this;
	}

	public NamelessApiBuilder apiUrl(final String apiUrl) {
		try {
			return apiUrl(new URL(apiUrl));
		} catch (final MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Connect to a HTTPS website, not in a subdirectory.
	 *
	 * @param host   hostname, for example namelessmc.com
	 * @param apiKey api key
	 */
	public NamelessApiBuilder apiUrl(final String host, final String apiKey) {
		return apiUrl("https://" + host + "/index.php?route=/api/v2/" + apiKey);
	}

	public NamelessApiBuilder userAgent(final String userAgent) {
		this.userAgent = userAgent;
		return this;
	}

	public NamelessApiBuilder debug(final boolean debug) {
		this.debug = debug;
		return this;
	}

	public NamelessAPI build() {
		if (this.apiUrl == null) {
			throw new IllegalStateException("No API URL specified");
		}

		return new NamelessAPI(new RequestHandler(this.apiUrl, this.userAgent, this.debug));
	}

}
