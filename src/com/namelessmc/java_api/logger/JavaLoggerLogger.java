package com.namelessmc.java_api.logger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaLoggerLogger extends ApiLogger {

	private final Logger logger;
	private final Level level;
	private final String prefix;

	public JavaLoggerLogger(final Logger logger, final Level level, final String prefix) {
		this.logger = logger;
		this.level = level;
		this.prefix = prefix;
	}

	@Override
	public void log(final String string) {
		this.logger.log(this.level, this.prefix + string);
	}

}
