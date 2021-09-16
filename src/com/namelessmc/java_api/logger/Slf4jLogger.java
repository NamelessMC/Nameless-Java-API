package com.namelessmc.java_api.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogger extends ApiLogger {

	public static final Slf4jLogger DEFAULT_INSTANCE = new Slf4jLogger(LoggerFactory.getLogger("Nameless-Java-API Debug"));

	private final Logger logger;

	public Slf4jLogger(final Logger logger) {
		this.logger = logger;
	}

	@Override
	public void log(final String string) {
		this.logger.info(string);
	}

}
