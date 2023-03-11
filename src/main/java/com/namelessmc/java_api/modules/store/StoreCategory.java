package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonObject;

public class StoreCategory {

	private final int id;
	private final String name;
	private final boolean hidden;
	private final boolean disabled;

	StoreCategory(JsonObject json) {
		this.id = json.get("id").getAsInt();
		this.name = json.get("name").getAsString();
		this.hidden = json.get("hidden").getAsBoolean();
		this.disabled = json.get("disabled").getAsBoolean();
	}

	public int id() {
		return this.id;
	}

	public String name() {
		return this.name;
	}

	public boolean isHidden() {
		return this.hidden;
	}

	public boolean isDisabled() {
		return this.disabled;
	}

}
