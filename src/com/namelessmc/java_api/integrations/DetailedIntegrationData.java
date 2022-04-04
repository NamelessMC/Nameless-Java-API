package com.namelessmc.java_api.integrations;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class DetailedIntegrationData extends IntegrationData {

	private final boolean verified;
	private final @NotNull Date linkedDate;
	private final boolean shownPublicly;

	public DetailedIntegrationData(final @NotNull String integrationType,
							final @NotNull String identifier,
							final @NotNull String username,
							final boolean verified,
							final @NotNull Date linkedDate,
							final boolean shownPublicly) {
		super(integrationType, identifier, username);
		this.verified = verified;
		this.linkedDate = linkedDate;
		this.shownPublicly = shownPublicly;
	}

	public DetailedIntegrationData(final @NotNull JsonObject json) {
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

	public @NotNull Date getLinkedDate() {
		return this.linkedDate;
	}

	public boolean isShownPublicly() {
		return this.shownPublicly;
	}

}
