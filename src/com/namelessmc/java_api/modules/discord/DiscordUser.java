package com.namelessmc.java_api.modules.discord;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.RequestHandler;
import com.namelessmc.java_api.modules.ModuleNames;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DiscordUser {

	private final NamelessUser user;
	private final RequestHandler requests;

	public DiscordUser(NamelessUser user) throws NamelessException {
		this.user = user;
		this.requests = user.api().requests();
		user.api().ensureModuleInstalled(ModuleNames.DISCORD_INTEGRATION);
	}

	public void updateDiscordRoles(final long@NonNull [] roleIds) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("user", this.user.getId());
		post.add("roles", this.requests.gson().toJsonTree(roleIds));
		this.requests.post("discord/set-roles", post);
	}

}
