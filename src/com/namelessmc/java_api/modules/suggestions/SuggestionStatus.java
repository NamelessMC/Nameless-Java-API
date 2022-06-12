package com.namelessmc.java_api.modules.suggestions;

import com.google.gson.JsonObject;

public class SuggestionStatus {

	private final int id;
	private final String name;
	private final boolean open;

	SuggestionStatus(JsonObject json) {
		this.id = json.get("id").getAsInt();
		this.name = json.get("name").getAsString();
		this.open = json.get("open").getAsBoolean();
	}

	public int id() {
		return this.id;
	}

	public String name() {
		return this.name;
	}

	public boolean isOpen() {
		return this.open;
	}

}
