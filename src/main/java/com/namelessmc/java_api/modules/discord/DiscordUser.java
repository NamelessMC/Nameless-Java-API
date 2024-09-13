package com.namelessmc.java_api.modules.discord;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessUser;
import com.namelessmc.java_api.RequestHandler;
import com.namelessmc.java_api.exception.NamelessException;
import com.namelessmc.java_api.modules.NamelessModule;

public class DiscordUser {

	private final NamelessUser user;
	private final RequestHandler requests;

	public DiscordUser(NamelessUser user) throws NamelessException {
		this.user = user;
		this.requests = user.api().requests();
		user.api().ensureModuleInstalled(NamelessModule.DISCORD_INTEGRATION);
	}

	/**
	 * @deprecated Replaced by {@link #syncGroups(long[], long[])}
	 */
	@Deprecated
	public void updateDiscordRoles(final long@NonNull [] roleIds) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("user", this.user.id());
		post.add("roles", this.requests.gson().toJsonTree(roleIds));
		this.requests.post("discord/set-roles", post);
	}
	
	/**
	 * Available from NamelessMC 2.2.0+. Use with a fallback to {@link #syncRoles(long[], long[])}.
	 * @throws NamelessException 
	 */
	public void syncRoles(final long[] addedRolesIds, final long[] removedRoleIds) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.add("add", this.requests.gson().toJsonTree(addedRolesIds));
		post.add("remove", this.requests.gson().toJsonTree(removedRoleIds));
		this.requests.post("discord/" + this.user.userTransformer() + "/sync-roles", post);
	}
}
