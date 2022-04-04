package com.namelessmc.java_api.integrations;

import org.jetbrains.annotations.NotNull;

public class DiscordIntegrationData extends IntegrationData {

	private final long id;

	public DiscordIntegrationData(final long id,
								  final @NotNull String username) {
		super(StandardIntegrationTypes.DISCORD, String.valueOf(id), username);
		this.id = id;
	}

	public long getIdLong() {
		return this.id;
	}

}
