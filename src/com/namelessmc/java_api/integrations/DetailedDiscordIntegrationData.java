package com.namelessmc.java_api.integrations;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class DetailedDiscordIntegrationData extends DetailedIntegrationData implements IDiscordIntegrationData {

	private final long idLong;

	public DetailedDiscordIntegrationData(@NotNull JsonObject json) {
		super(json);
		this.idLong = Integer.parseInt(this.getIdentifier());
	}

	@Override
	public long getIdLong() {
		return this.idLong;
	}
}
