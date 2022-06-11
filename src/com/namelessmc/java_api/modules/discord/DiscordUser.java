package com.namelessmc.java_api.modules.discord;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.RequestHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DiscordUser {

	private final NamelessUser user;
	private final RequestHandler requests;

	public DiscordUser(NamelessUser user) {
		this.user = user;
		this.requests = user.api().requests();
	}

	public void updateDiscordRoles(final long@NonNull [] roleIds) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("user", this.user.getId());
		post.add("roles", this.requests.gson().toJsonTree(roleIds));
		this.requests.post("discord/set-roles", post);
	}

}
