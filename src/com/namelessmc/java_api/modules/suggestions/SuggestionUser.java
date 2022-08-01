package com.namelessmc.java_api.modules.suggestions;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.exception.NamelessException;

public class SuggestionUser {

	private final NamelessAPI api;

	private final int id;
	private final String username;

	SuggestionUser(final NamelessAPI api, final JsonObject json) {
		this.api = api;

		this.id = json.get("id").getAsInt();
		this.username = json.get("username").getAsString();
	}

	public int userId() {
		return this.id;
	}

	public NamelessUser user() throws NamelessException {
		NamelessUser user = this.api.user(this.id);
		if (user == null) {
			throw new IllegalStateException("Suggestions module returned a user id that doesn't exist");
		}
		return user;
	}

	public String username() {
		return username;
	}

}
