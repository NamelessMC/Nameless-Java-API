package com.namelessmc.java_api.integrations;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MinecraftIntegrationData extends IntegrationData implements IMinecraftIntegrationData {

	private final @NotNull UUID uuid;

	public MinecraftIntegrationData(final @NotNull UUID uuid,
									final @NotNull String username) {
		super(StandardIntegrationTypes.MINECRAFT, uuid.toString(), username);
		this.uuid = uuid;
	}

	public @NotNull UUID getUniqueId() {
		return this.uuid;
	}

}
