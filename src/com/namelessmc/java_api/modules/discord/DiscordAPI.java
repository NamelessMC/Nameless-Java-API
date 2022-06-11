package com.namelessmc.java_api.modules.discord;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.NamelessException;
import com.namelessmc.java_api.RequestHandler;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.URL;
import java.util.Map;
import java.util.Objects;

public class DiscordAPI {

	private final RequestHandler requests;

	public DiscordAPI(NamelessAPI api) {
		this.requests = api.requests();
	}

	/**
	 * Set Discord bot URL (Nameless-Link internal webserver)
	 * @param url Discord bot URL
	 * @see #updateBotSettings(URL, long, String, long)
	 */
	public void updateBotUrl(final @NonNull URL url) throws NamelessException {
		Objects.requireNonNull(url, "Bot url is null");

		final JsonObject json = new JsonObject();
		json.addProperty("url", url.toString());
		this.requests.post("discord/update-bot-settings", json);
	}

	/**
	 * Set discord bot username and user id
	 * @param username Bot username#tag
	 * @param userId Bot user id
	 * @see #updateBotSettings(URL, long, String, long)
	 */
	public void updateBotUser(final @NonNull String username, final long userId) throws NamelessException {
		Objects.requireNonNull(username, "Bot username is null");

		final JsonObject json = new JsonObject();
		json.addProperty("bot_username", username);
		json.addProperty("bot_user_id", userId + "");
		this.requests.post("discord/update-bot-settings", json);
	}

	/**
	 * Set Discord guild (server) id
	 * @param guildId Discord guild (server) id
	 * @see #updateBotSettings(URL, long, String, long)
	 */
	public void updateGuildId(final long guildId) throws NamelessException {
		final JsonObject json = new JsonObject();
		json.addProperty("guild_id", guildId + "");
		this.requests.post("discord/update-bot-settings", json);
	}

	/**
	 * Update all Discord bot settings.
	 * @param url Discord bot URL
	 * @param guildId Discord guild (server) id
	 * @param username Discord bot username#tag
	 * @param userId Discord bot user id
	 * @see #updateBotUrl(URL)
	 * @see #updateGuildId(long)
	 * @see #updateBotUser(String, long)
	 */
	public void updateBotSettings(final @NonNull URL url,
									  final long guildId,
									  final @NonNull String username,
									  final long userId) throws NamelessException {
		Objects.requireNonNull(url, "Bot url is null");
		Objects.requireNonNull(username, "Bot username is null");

		final JsonObject json = new JsonObject();
		json.addProperty("url", url.toString());
		json.addProperty("guild_id", guildId + "");
		json.addProperty("bot_username", username);
		json.addProperty("bot_user_id", userId + "");
		this.requests.post("discord/update-bot-settings", json);
	}

	/**
	 * Update Discord username for a NamelessMC user associated with the provided Discord user id
	 * @param discordUserId Discord user id
	 * @param discordUsername New Discord [username#tag]s
	 * @see #updateDiscordUsernames(long[], String[])
	 */
	public void updateDiscordUsername(final long discordUserId,
									  final @NonNull String discordUsername)
			throws NamelessException {
		Objects.requireNonNull(discordUsername, "Discord username is null");

		final JsonObject user = new JsonObject();
		user.addProperty("id", discordUserId);
		user.addProperty("name", discordUsername);
		final JsonArray users = new JsonArray();
		users.add(user);
		final JsonObject json = new JsonObject();
		json.add("users", users);
		this.requests.post("discord/update-usernames", json);
	}

	/**
	 * Update Discord usernames in bulk
	 * @param discordUserIds Discord user ids
	 * @param discordUsernames New Discord [username#tag]s
	 * @see #updateDiscordUsername(long, String)
	 */
	public void updateDiscordUsernames(final long@NonNull[] discordUserIds,
									   final  @NonNull String@NonNull[] discordUsernames)
			throws NamelessException {
		Objects.requireNonNull(discordUserIds, "User ids array is null");
		Objects.requireNonNull(discordUsernames, "Usernames array is null");
		Preconditions.checkArgument(discordUserIds.length == discordUsernames.length,
				"discord user ids and discord usernames must be of same length");

		if (discordUserIds.length == 0) {
			return;
		}

		final JsonArray users = new JsonArray();

		for (int i = 0; i < discordUserIds.length; i++) {
			final JsonObject user = new JsonObject();
			user.addProperty("id", discordUserIds[i]);
			user.addProperty("name", discordUsernames[i]);
			users.add(user);
		}

		final JsonObject json = new JsonObject();
		json.add("users", users);
		this.requests.post("discord/update-usernames", json);
	}

	/**
	 * Send list of Discord roles to the website for populating the dropdown in StaffCP > API > Group sync
	 * @param discordRoles Map of Discord roles, key is role id, value is role name
	 */
	public void updateRoleList(final @NonNull Map<Long, String> discordRoles) throws NamelessException {
		final JsonArray roles = new JsonArray();
		discordRoles.forEach((id, name) -> {
			final JsonObject role = new JsonObject();
			role.addProperty("id", id);
			role.addProperty("name", name);
			roles.add(role);
		});
		final JsonObject json = new JsonObject();
		json.add("roles", roles);
		this.requests.post("discord/submit-role-list", json);
	}

}
