package com.namelessmc.java_api.integrations;

import org.jetbrains.annotations.NotNull;

public class DiscordIntegrationData extends IntegrationData {

	private final long id;
	private final @NotNull String username;

	public DiscordIntegrationData(final long id,
								  @NotNull String username) {
		super(IntegrationType.DISCORD);
		this.id = id;
		this.username = username;
	}

	public long getId() {
		return this.id;
	}

	public @NotNull String getUsername() {
		return this.getUsername();
	}

	@Override
	public @NotNull String getRawId() {
		return String.valueOf(this.id);
	}

	@Override
	public @NotNull String getRawUsername() {
		return this.username;
	}
}
