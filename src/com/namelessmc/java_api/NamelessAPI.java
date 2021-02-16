package com.namelessmc.java_api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.RequestHandler.Action;
import com.namelessmc.java_api.exception.CannotSendEmailException;
import com.namelessmc.java_api.exception.InvalidUsernameException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class NamelessAPI {

	private static final String DEFAULT_USER_AGENT = "Nameless-Java-API";

	private final RequestHandler requests;

	public NamelessAPI(final URL apiUrl) {
		this(apiUrl, DEFAULT_USER_AGENT);
	}

	/**
	 * @param apiUrl URL of API to connect to, in the format http(s)://yoursite.com/index.php?route=/api/v2/API_KEY
	 * @param debug
	 */
	public NamelessAPI(final URL apiUrl, final boolean debug) {
		this(apiUrl, DEFAULT_USER_AGENT, debug);
	}

	public NamelessAPI(final URL apiUrl, final String userAgent) {
		this(apiUrl, userAgent, false);
	}

	/**
	 * @param host   URL of your website, in the format http(s)://yoursite.com
	 * @param apiKey API key
	 * @param debug
	 * @throws MalformedURLException
	 */
	public NamelessAPI(final String host, final String apiKey, final String userAgent, final boolean debug) throws MalformedURLException {
		this(new URL(host + "/index.php?route=/api/v2/" + apiKey), userAgent, debug);
	}

	public NamelessAPI(final String host, final String apiKey, final boolean debug) throws MalformedURLException {
		this(host, apiKey, DEFAULT_USER_AGENT, debug);
	}

	public NamelessAPI(final String host, final String apiKey, final String userAgent) throws MalformedURLException {
		this(host, apiKey, userAgent, false);
	}

	public NamelessAPI(final String host, final String apiKey) throws MalformedURLException {
		this(host, apiKey, DEFAULT_USER_AGENT);
	}

	public NamelessAPI(final URL apiUrl, final String userAgent, final boolean debug) {
		this.requests = new RequestHandler(apiUrl, userAgent, debug);
	}

	RequestHandler getRequestHandler() {
		return this.requests;
	}

	public URL getApiUrl() {
		return this.getRequestHandler().getApiUrl();
	}

	public String getApiKey() {
		return getApiKey(this.getApiUrl().toString());
	}

	static String getApiKey(final String url) {
		if (url.endsWith("/")) {
			return getApiKey(StringUtils.removeEnd(url, "/"));
		}

		return StringUtils.substringAfterLast(url, "/");
	}

	/**
	 * Checks if a web API connection can be established
	 * throws {@link NamelessException} if the connection was unsuccessful
	 */
	public void checkWebAPIConnection() throws NamelessException {
		final JsonObject response = this.requests.get(Action.INFO);
		if (!response.has("nameless_version")) {
			throw new NamelessException("Invalid response: " + response.getAsString());
		}
	}

	/**
	 * Get all announcements
	 *
	 * @return list of current announcements
	 * @throws NamelessException if there is an error in the request
	 */
	public List<Announcement> getAnnouncements() throws NamelessException {
		final JsonObject response = this.requests.get(Action.GET_ANNOUNCEMENTS);

		return jsonArrayToList(response.getAsJsonArray());
	}

	/**
	 * Get all announcements visible for the player with the specified uuid
	 *
	 * @param user player to get visibile announcements for
	 * @return list of current announcements visible to the player
	 * @throws NamelessException if there is an error in the request
	 */
	public List<Announcement> getAnnouncements(final NamelessUser user) throws NamelessException {
		final JsonObject response = this.requests.get(Action.GET_ANNOUNCEMENTS, "id", user.getId());

		return jsonArrayToList(response.get("announcements").getAsJsonArray());
	}

	private List<Announcement> jsonArrayToList(JsonArray array) {
		final List<Announcement> announcements = new ArrayList<>();
		array.forEach((element) -> {
			final JsonObject announcementJson = element.getAsJsonObject();
			final String content = announcementJson.get("content").getAsString();
			final String[] display = jsonToArray(announcementJson.get("display").getAsJsonArray());
			final String[] permissions = jsonToArray(announcementJson.get("permissions").getAsJsonArray());
			announcements.add(new Announcement(content, display, permissions));
		});
		return announcements;
	}

	public void submitServerInfo(final JsonObject jsonData) throws NamelessException {
		this.requests.post(Action.SERVER_INFO, jsonData);
	}

	public Website getWebsite() throws NamelessException {
		final JsonObject json = this.requests.get(Action.INFO);
		return new Website(json);
	}

	public List<NamelessUser> getRegisteredUsers(final UserFilter<?>... filters) throws NamelessException {
		final List<Object> parameters = new ArrayList<>();
		for (final UserFilter<?> filter : filters) {
			parameters.add(filter.getName());
			parameters.add(filter.getValue().toString());
		}
		final JsonObject response = this.requests.get(Action.LIST_USERS, parameters.toArray());
		final JsonArray array = response.getAsJsonArray("users");
		final List<NamelessUser> users = new ArrayList<>(array.size());
		for (final JsonElement e : array) {
			final JsonObject o = e.getAsJsonObject();
			final int id = o.get("id").getAsInt();
			final String username = o.get("username").getAsString();
			final Optional<UUID> uuid;
			if (o.has("uuid")) {
				final String uuidString = o.get("uuid").getAsString();
				if (uuidString == null || uuidString.equals("none") || uuidString.equals("")) {
					uuid = Optional.empty();
				} else {
					uuid = Optional.of(NamelessAPI.websiteUuidToJavaUuid(uuidString));
				}
			} else {
				uuid = null;
			}
			users.add(new NamelessUser(this, id, username, uuid, -1L));
		}

		return Collections.unmodifiableList(users);
	}

	public Optional<NamelessUser> getUser(final int id) throws NamelessException {
		final NamelessUser user = getUserLazy(id);
		if (user.exists()) {
			return Optional.of(user);
		} else {
			return Optional.empty();
		}
	}

	public Optional<NamelessUser> getUser(final String username) throws NamelessException {
		final NamelessUser user = getUserLazy(username);
		if (user.exists()) {
			return Optional.of(user);
		} else {
			return Optional.empty();
		}
	}

	public Optional<NamelessUser> getUser(final UUID uuid) throws NamelessException {
		final NamelessUser user = getUserLazy(uuid);
		if (user.exists()) {
			return Optional.of(user);
		} else {
			return Optional.empty();
		}
	}

	public Optional<NamelessUser> getUserByDiscordId(final long discordId) throws NamelessException {
		final NamelessUser user = getUserLazyDiscord(discordId);
		if (user.exists()) {
			return Optional.of(user);
		} else {
			return Optional.empty();
		}
	}

	public NamelessUser getUserLazy(final int id) {
		return new NamelessUser(this, id, null, null, -1L);
	}

	public NamelessUser getUserLazy(final String username) {
		return new NamelessUser(this, -1, username, null, -1L);
	}

	public NamelessUser getUserLazy(final UUID uuid) {
		return new NamelessUser(this, -1, null, Optional.ofNullable(uuid), -1L);
	}

	public NamelessUser getUserLazy(final String username, final UUID uuid) {
		return new NamelessUser(this, -1, null, Optional.ofNullable(uuid), -1L);
	}

	public NamelessUser getUserLazy(final int id, final String username, final UUID uuid) {
		return new NamelessUser(this, id, username, Optional.ofNullable(uuid), -1L);
	}

	public NamelessUser getUserLazyDiscord(final long discordId) {
		return new NamelessUser(this, -1, null, null, discordId);
	}

	public Optional<Group> getGroup(final int id) throws NamelessException {
		final JsonObject response = this.requests.get(Action.GROUP_INFO, "id", id);
		final JsonArray array = response.getAsJsonArray("groups");
		if (array.size() == 0) {
			return Optional.empty();
		} else {
			return Optional.of(new Group(response.getAsJsonObject("group")));
		}
	}

	public List<Group> getGroup(final String name) throws NamelessException {
		final JsonObject response = this.requests.get(Action.GROUP_INFO, "name", name);
		return groupListFromJsonArray(response.getAsJsonArray("groups"));
	}

	public List<Group> getAllGroups() throws NamelessException {
		final JsonObject response = this.requests.get(Action.GROUP_INFO);
		return groupListFromJsonArray(response.getAsJsonArray("groups"));

	}

	public int[] getAllGroupIds() throws NamelessException {
		final JsonObject response = this.requests.get(Action.GROUP_INFO);
		return StreamSupport.stream(response.getAsJsonArray("groups").spliterator(), false)
				.map(JsonElement::getAsJsonObject)
				.mapToInt(o -> o.get("id").getAsInt())
				.toArray();
	}

	private List<Group> groupListFromJsonArray(final JsonArray array) {
		return StreamSupport.stream(array.spliterator(), false)
				.map(JsonElement::getAsJsonObject)
				.map(Group::new)
				.collect(Collectors.toList());
	}

	/**
	 * Registers a new account. The user will be sent an email to set a password.
	 *
	 * @param username Username
	 * @param email Email address
	 * @param uuid (for minecraft integration)
	 * @return Email verification disabled: A link which the user needs to click to complete registration
	 * <br>Email verification enabled: An empty string (the user needs to check their email to complete registration)
	 * @throws NamelessException
	 * @throws InvalidUsernameException
	 * @throws CannotSendEmailException
	 */
	public Optional<String> registerUser(final String username, final String email, final Optional<UUID> uuid) throws NamelessException, InvalidUsernameException, CannotSendEmailException {
		Validate.notNull(username);
		Validate.notNull(email);
		Validate.notNull(uuid);
		
		final JsonObject post = new JsonObject();
		post.addProperty("username", username);
		post.addProperty("email", email);
		if (uuid.isPresent()) {
			post.addProperty("uuid", uuid.get().toString());
		}

		try {
			final JsonObject response = this.requests.post(Action.REGISTER, post);

			if (response.has("link")) {
				return Optional.of(response.get("link").getAsString());
			} else {
				return Optional.empty();
			}
		} catch (final ApiError e) {
			if (e.getError() == ApiError.INVALID_USERNAME) {
				throw new InvalidUsernameException();
			} else if (e.getError() == ApiError.UNABLE_TO_SEND_REGISTRATION_EMAIL) {
				throw new CannotSendEmailException();
			} else {
				throw e;
			}
		}
	}

	public Optional<String> registerUser(final String username, final String email) throws NamelessException, InvalidUsernameException, CannotSendEmailException {
		return registerUser(username, email, null);
	}

	public void verifyDiscord(final String verificationToken, final long discordUserId, final String discordUsername) throws NamelessException {
		final JsonObject json = new JsonObject();
		json.addProperty("token", verificationToken);
		json.addProperty("discord_id", discordUserId + ""); // website needs it as a string
		json.addProperty("discord_username", discordUsername);
		this.requests.post(Action.VERIFY_DISCORD, json);
	}

	public void setDiscordBotUrl(final URL url) throws NamelessException {
		final JsonObject json = new JsonObject();
		json.addProperty("url", url.toString());
		this.requests.post(Action.UPDATE_DISCORD_BOT_SETTINGS, json);
	}

	public void setDiscordGuildId(final long guildId) throws NamelessException {
		final JsonObject json = new JsonObject();
		json.addProperty("guild_id", guildId + "");
		this.requests.post(Action.UPDATE_DISCORD_BOT_SETTINGS, json);
	}

	public void setDiscordBotUser(final String username, final long userId) throws NamelessException {
		final JsonObject json = new JsonObject();
		json.addProperty("bot_username", username);
		json.addProperty("bot_user_id", userId + "");
		this.requests.post(Action.UPDATE_DISCORD_BOT_SETTINGS, json);
	}

	public void setDiscordBotSettings(final URL url, final long guildId, final String username, final long userId) throws NamelessException {
		final JsonObject json = new JsonObject();
		json.addProperty("url", url.toString());
		json.addProperty("guild_id", guildId + "");
		json.addProperty("bot_username", username);
		json.addProperty("bot_user_id", userId + "");
		this.requests.post(Action.UPDATE_DISCORD_BOT_SETTINGS, json);
	}

	public void submitDiscordRoleList(final Map<Long, String> discordRoles) throws NamelessException {
		final JsonArray roles = new JsonArray();
		discordRoles.forEach((id, name) -> {
			final JsonObject role = new JsonObject();
			role.addProperty("id", id);
			role.addProperty("name", name);
			roles.add(role);
		});
		final JsonObject json = new JsonObject();
		json.add("roles", roles);
		this.requests.post(Action.SUBMIT_DISCORD_ROLE_LIST, json);
	}

	public void updateDiscordUsername(final long discordUserId, final String discordUsername) throws NamelessException {
		final JsonObject user = new JsonObject();
		user.addProperty("id", discordUserId);
		user.addProperty("name", discordUsername);
		final JsonArray users = new JsonArray();
		users.add(user);
		final JsonObject json = new JsonObject();
		json.add("users", users);
		this.requests.post(Action.UPDATE_DISCORD_USERNAMES, json);
	}

	public void updateDiscordUsernames(final long[] discordUserIds, final String[] discordUsernames) throws NamelessException {
		if (discordUserIds.length != discordUsernames.length) {
			throw new IllegalArgumentException("discord user ids and discord usernames must be of same length");
		}

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
		this.requests.post(Action.UPDATE_DISCORD_USERNAMES, json);
	}

	@Deprecated
	static String[] jsonToArray(final JsonArray jsonArray) {
		final List<String> list = new ArrayList<>();
		jsonArray.iterator().forEachRemaining((element) -> list.add(element.getAsString()));
		return list.toArray(new String[]{});
	}

	static UUID websiteUuidToJavaUuid(final String uuid) {
		// Website sends UUIDs without dashses, so we can't use UUID#fromString
		// https://stackoverflow.com/a/30760478
		try {
			final BigInteger a = new BigInteger(uuid.substring(0, 16), 16);
			final BigInteger b = new BigInteger(uuid.substring(16, 32), 16);
			return new UUID(a.longValue(), b.longValue());
		} catch (final IndexOutOfBoundsException e) {
			throw new IllegalArgumentException("Invalid uuid: '" + uuid + "'", e);
		}
	}

}