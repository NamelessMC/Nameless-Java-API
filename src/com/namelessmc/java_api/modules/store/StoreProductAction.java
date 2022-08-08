package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonObject;

public class StoreProductAction {

	private final int id;
	private final int typeId; // TODO enum
	private final int serviceId;
	private final String command;
	private final boolean requireOnline;
	private final boolean ownConnections;

	StoreProductAction(JsonObject json) {
		this.id = json.get("id").getAsInt();
		this.typeId = json.get("type").getAsInt();
		this.serviceId = json.get("service_id").getAsInt();
		this.command = json.get("command").getAsString();
		this.requireOnline = json.get("require_online").getAsBoolean();
		this.ownConnections = json.get("own_connections").getAsBoolean();
	}

	public int id() {
		return this.id;
	}

	@Deprecated
	public int typeId() {
		return this.typeId;
	}

	public int serviceId() {
		return this.serviceId;
	}

	public String command() {
		return this.command;
	}

	public boolean requireOnline() {
		return this.requireOnline;
	}

	public boolean ownConnections() {
		return this.ownConnections;
	}

}
