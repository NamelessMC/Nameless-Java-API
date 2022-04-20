package com.namelessmc.java_api.integrations;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class DetailedMinecraftIntegrationData extends DetailedIntegrationData implements IMinecraftIntegrationData {

	private final @NonNull UUID uuid;

	public DetailedMinecraftIntegrationData(final @NonNull JsonObject json) {
		super(json);
		this.uuid = NamelessAPI.websiteUuidToJavaUuid(this.getIdentifier());
	}

	@Override
	public @NonNull UUID getUniqueId() {
		return this.uuid;
	}
}
