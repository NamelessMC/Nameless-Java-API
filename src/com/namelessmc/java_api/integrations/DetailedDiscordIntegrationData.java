package com.namelessmc.java_api.integrations;

import com.google.gson.JsonObject;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DetailedDiscordIntegrationData extends DetailedIntegrationData implements IDiscordIntegrationData {

	private final long idLong;

	public DetailedDiscordIntegrationData(final @NonNull JsonObject json) {
		super(json);
		this.idLong = Long.parseLong(this.identifier());
	}

	@Override
	public final long idLong() {
		return this.idLong;
	}

}
