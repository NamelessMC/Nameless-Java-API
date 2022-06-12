package com.namelessmc.java_api.modules.suggestions;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.RequestHandler;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.modules.ModuleNames;

import java.util.Objects;

public class SuggestionsUser {

	private final NamelessUser user;
	private final NamelessAPI api;
	private final RequestHandler requests;

	public SuggestionsUser(final NamelessUser user) throws NamelessException {
		this.user = user;
		this.api = this.user.api();
		this.requests = this.api.requests();

		this.api.ensureModuleInstalled(ModuleNames.SUGGESTIONS);
	}

	public void like(final int suggestionId) throws NamelessException {
		JsonObject body = new JsonObject();
		body.addProperty("user", this.user.userTransformer());
		this.requests.post("suggestions/like", body);
	}

	public void like(final Suggestion suggestion) throws NamelessException {
		this.like(suggestion.id());
	}

	public void dislike(final int suggestionId) throws NamelessException {
		final JsonObject body = new JsonObject();
		body.addProperty("user", this.user.userTransformer());
		this.requests.post("suggestions/dislike", body);
	}

	public void dislike(final Suggestion suggestion) throws NamelessException {
		this.dislike(suggestion.id());
	}

	public Suggestion createSuggestion(final String title, final String content, final int categoryId) throws NamelessException {
		final JsonObject body = new JsonObject();
		body.addProperty("user", this.user.userTransformer());
		body.addProperty("title", Objects.requireNonNull(title, "title is null"));
		body.addProperty("content", Objects.requireNonNull(content, "content is null"));
		if (categoryId > 0) {
			body.addProperty("category", categoryId);
		}
		final JsonObject response = this.requests.post("suggestions/create", body);
		final int suggestionId = response.get("id").getAsInt();
		return this.api.suggestions().suggestion(suggestionId);
	}

	public Suggestion createSuggestion(final String title, final String content) throws NamelessException {
		return this.createSuggestion(title, content, -1);
	}

	public Suggestion createSuggestion(final String title, final String content, final SuggestionCategory category) throws NamelessException {
		return this.createSuggestion(title, content, category.id());
	}

}
