package com.namelessmc.java_api.modules.store;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.RequestHandler;
import com.namelessmc.java_api.modules.ModuleNames;

public class StoreUser {

	private final NamelessUser user;
	private final RequestHandler requests;

	public StoreUser(NamelessUser user) throws NamelessException {
		this.user = user;
		this.requests = user.api().requests();
		user.api().ensureModuleInstalled(ModuleNames.STORE);
	}

	public void addCredits(float creditsToAdd) throws NamelessException {
		JsonObject body = new JsonObject();
		body.addProperty("credits", creditsToAdd);
		this.requests.post("users/" + this.user.userTransformer() + "/add-credits", body);
	}

	public void removeCredits(float creditsToRemove) throws NamelessException {
		JsonObject body = new JsonObject();
		body.addProperty("credits", creditsToRemove);
		this.requests.post("users/" + this.user.userTransformer() + "/remove-credits", body);
	}

}
