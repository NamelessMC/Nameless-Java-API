package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public class StoreCustomer {

	private final NamelessAPI api;
	private final int id;
	private final @Nullable Integer userId;
	private final @Nullable String username;
	private final @Nullable String identifier;

	StoreCustomer(NamelessAPI api, JsonObject json) {
		this.api = api;
		this.id = json.get("id").getAsInt();
		this.userId = json.has("user_id") ? json.get("user_id").getAsInt() : null;
		this.username = json.has("username") ? json.get("username").getAsString() : null;
		this.identifier = json.has("identifier") ? json.get("identifier").getAsString() : null;

		if (this.username == null && this.identifier == null) {
			throw new IllegalStateException("Username and identifier cannot be null at the same time");
		}
	}

	public int id() {
		return this.id;
	}

	public @Nullable NamelessUser user() throws NamelessException {
		return this.userId != null ? this.api.user(this.userId) : null;
	}

	public @Nullable String username() {
		return this.username;
	}

	public @Nullable String identifier() {
		return this.identifier;
	}

	public @Nullable UUID identifierAsUuid() {
		// Unlike NamelessMC, the store module sends UUIDs with dashes
		return this.identifier != null ? UUID.fromString(this.identifier) : null;
	}

}
