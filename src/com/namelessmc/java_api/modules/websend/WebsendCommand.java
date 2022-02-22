package com.namelessmc.java_api.modules.websend;

import org.jetbrains.annotations.NotNull;

public class WebsendCommand {

	private final int id;
	private final @NotNull String commandLine;

	public WebsendCommand(final int id,
						  final @NotNull String commandLine) {
		this.id = id;
		this.commandLine = commandLine;
	}

	public int getId() {
		return id;
	}

	public @NotNull String getCommandLine() {
		return this.commandLine;
	}

}
