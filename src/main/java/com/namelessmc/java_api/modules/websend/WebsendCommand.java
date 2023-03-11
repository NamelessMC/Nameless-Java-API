package com.namelessmc.java_api.modules.websend;

import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;

public class WebsendCommand {

	private final @Positive int id;
	private final @NonNull String commandLine;

	public WebsendCommand(final @Positive int id,
						  final @NonNull String commandLine) {
		this.id = id;
		this.commandLine = commandLine;
	}

	public @Positive int id() {
		return id;
	}

	public @NonNull String command() {
		return this.commandLine;
	}

}
