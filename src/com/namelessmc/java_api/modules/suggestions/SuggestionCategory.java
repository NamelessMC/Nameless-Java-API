package com.namelessmc.java_api.modules.suggestions;

import com.google.gson.JsonObject;

public class SuggestionCategory {

	private final int id;
	private final String name;

	SuggestionCategory(final JsonObject json) {
		this.id = json.get("id").getAsInt();
		this.name = json.get("name").getAsString();
	}

	public int id() {
		return this.id;
	}

	public String name() {
		return this.name;
	}

}
