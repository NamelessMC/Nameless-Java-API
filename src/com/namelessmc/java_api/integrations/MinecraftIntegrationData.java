package com.namelessmc.java_api.integrations;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class MinecraftIntegrationData extends IntegrationData implements IMinecraftIntegrationData {

	private final @NonNull UUID uuid;

	public MinecraftIntegrationData(final @NonNull UUID uuid,
									final @NonNull String username) {
		super(StandardIntegrationTypes.MINECRAFT, uuid.toString(), username);
		this.uuid = uuid;
	}

	public @NonNull UUID getUniqueId() {
		return this.uuid;
	}

}
