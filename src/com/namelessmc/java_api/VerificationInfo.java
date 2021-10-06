package com.namelessmc.java_api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class VerificationInfo {

	private final boolean verified;
	private final @NotNull JsonObject json;

	VerificationInfo(final boolean verified, @NotNull final JsonObject json) {
		this.verified = verified;
		this.json = json;
	}

	public boolean isVerified() {
		return this.verified;
	}

	public boolean isVerifiedCustom(@NotNull final String name) {
		final JsonElement e = this.json.get(name);
		if (e == null) {
			throw new UnsupportedOperationException("The API did not return verification for '" + name + "'");
		} else {
			return e.getAsBoolean();
		}
	}

	public boolean isVerifiedEmail() {
		return isVerifiedCustom("email");
	}

	public boolean isVerifiedMinecraft() {
		return isVerifiedCustom("minecraft");
	}

	public boolean isVerifiedDiscord() {
		return isVerifiedCustom("discord");
	}

}
