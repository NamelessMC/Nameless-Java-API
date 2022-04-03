package com.namelessmc.java_api.integrations;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MinecraftIntegrationData extends IntegrationData {

	private final @NotNull UUID uuid;
	private final @NotNull String username;

	public MinecraftIntegrationData(final @NotNull UUID uuid,
									final @NotNull String username) {
		super(IntegrationType.MINECRAFT);
		this.uuid = uuid;
		this.username = username;
	}

	public @NotNull UUID getUniqueId() {
		return this.uuid;
	}

	public @NotNull String getUsername() {
		return this.username;
	}

	@Override
	public @NotNull String getRawId() {
		return this.uuid.toString();
	}

	@Override
	public @NotNull String getRawUsername() {
		return this.username;
	}

}
