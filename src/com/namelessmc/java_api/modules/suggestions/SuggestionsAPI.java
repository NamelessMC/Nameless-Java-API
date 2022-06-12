package com.namelessmc.java_api.modules.suggestions;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.RequestHandler;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.modules.ModuleNames;

public class SuggestionsAPI {

	private final NamelessAPI api;
	private final RequestHandler requests;

	public SuggestionsAPI(final NamelessAPI api) throws NamelessException {
		this.api = api;
		this.requests = api.requests();
		api.ensureModuleInstalled(ModuleNames.SUGGESTIONS);
	}

	public Suggestion suggestion(int suggestionId) throws NamelessException {
		final JsonObject response = this.requests.get("suggestions/" + suggestionId);
		return new Suggestion(this.api, response);
	}

}
