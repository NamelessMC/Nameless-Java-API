package com.namelessmc.java_api.integrations;

import com.google.gson.JsonObject;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Date;

public class DetailedIntegrationData extends IntegrationData {

	private final boolean verified;
	private final @NonNull Date linkedDate;
	private final boolean shownPublicly;

	public DetailedIntegrationData(final @NonNull String integrationType,
							final @NonNull String identifier,
							final @NonNull String username,
							final boolean verified,
							final @NonNull Date linkedDate,
							final boolean shownPublicly) {
		super(integrationType, identifier, username);
		this.verified = verified;
		this.linkedDate = linkedDate;
		this.shownPublicly = shownPublicly;
	}

	public DetailedIntegrationData(final @NonNull JsonObject json) {
		this(
				json.get("integration").getAsString(),
				json.get("identifier").getAsString(),
				json.get("username").getAsString(),
				json.get("verified").getAsBoolean(),
				new Date(json.get("linked_date").getAsLong()),
				json.get("show_publicly").getAsBoolean()
		);
	}

	public boolean isVerified() {
		return verified;
	}

	public @NonNull Date getLinkedDate() {
		return this.linkedDate;
	}

	public boolean isShownPublicly() {
		return this.shownPublicly;
	}

}
