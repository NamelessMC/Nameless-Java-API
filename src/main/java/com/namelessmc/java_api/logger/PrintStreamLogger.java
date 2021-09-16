package com.namelessmc.java_api.logger;

import java.io.PrintStream;

public class PrintStreamLogger extends ApiLogger {

	public static final PrintStreamLogger DEFAULT_INSTANCE = new PrintStreamLogger(System.err, "[Nameless-Java-API Debug] ");

	private final PrintStream stream;
	private final String prefix;

	public PrintStreamLogger(final PrintStream stream, final String prefix) {
		this.stream = stream;
		this.prefix = prefix;
	}

	@Override
	public void log(final String string) {
		this.stream.println(this.prefix + string);
	}

}
