package com.namelessmc.java_api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.RequestHandler.Action;

public final class NamelessAPI {

	private static final String DEFAULT_USER_AGENT = "Nameless-Java-API/beta"; // TODO include mavenversion string
	
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
	 * @param host URL of your website, in the format http(s)://yoursite.com
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

	/**
	 * Checks if a web API connection can be established
	 * throws {@link NamelessException} if the connection was unsuccessful
	 */
	public void checkWebAPIConnection() throws NamelessException {
		final JsonObject response = this.requests.get(Action.INFO);
		if (!response.has("nameless_version")) {
			throw new NamelessException("Invalid respose: " + response.getAsString());
		}
	}

	/**
	 * Get all announcements
	 * @return list of current announcements
	 * @throws NamelessException if there is an error in the request
	 */
	public List<Announcement> getAnnouncements() throws NamelessException {
		final JsonObject response = this.requests.get(Action.GET_ANNOUNCEMENTS);

		final List<Announcement> announcements = new ArrayList<>();

		response.getAsJsonArray().forEach((element) -> {
			final JsonObject announcementJson = element.getAsJsonObject();
			final String content = announcementJson.get("content").getAsString();
			final String[] display = jsonToArray(announcementJson.get("display").getAsJsonArray());
			final String[] permissions = jsonToArray(announcementJson.get("permissions").getAsJsonArray());
			announcements.add(new Announcement(content, display, permissions));
		});

		return announcements;
	}

	/**
	 * Get all announcements visible for the player with the specified uuid
	 * @param uuid UUID of player to get visibile announcements for
	 * @return list of current announcements visible to the player
	 * @throws NamelessException if there is an error in the request
	 */
	public List<Announcement> getAnnouncements(final NamelessUser user) throws NamelessException {
		final JsonObject response = this.requests.get(Action.GET_ANNOUNCEMENTS, "id", user.getId());

		final List<Announcement> announcements = new ArrayList<>();

		response.get("announcements").getAsJsonArray().forEach((element) -> {
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
		final List<String> parameters = new ArrayList<>();
		for (final UserFilter<?> filter : filters) {
			parameters.add(filter.getName());
			parameters.add(filter.getValue().toString());
		}
		final JsonObject response = this.requests.get(Action.LIST_USERS, parameters);
		final JsonArray array = response.getAsJsonArray("users");
		final List<NamelessUser> users = new ArrayList<>(array.size());
		for (final JsonElement e : array) {
			final JsonObject o = e.getAsJsonObject();
			final int id = o.get("id").getAsInt();
			final String username = o.get("username").getAsString();
			Optional<UUID> uuid;
			if (o.has("uuid")) {
				uuid = Optional.of(NamelessAPI.websiteUuidToJavaUuid(o.get("uuid").getAsString()));
			} else {
				uuid = Optional.empty();
			}
			users.add(new NamelessUser(this, id, username, uuid));
		};
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
	
	public NamelessUser getUserLazy(final int id) throws NamelessException {
		return new NamelessUser(this, id, null, null);
	}
	
	public NamelessUser getUserLazy(final String username) throws NamelessException {
		return new NamelessUser(this, null, username, null);
	}
	
	public NamelessUser getUserLazy(final UUID uuid) throws NamelessException {
		return new NamelessUser(this, null, null, Optional.of(uuid));
	}

	public NamelessUser getUserLazy(final String username, final UUID uuid) throws NamelessException {
		return new NamelessUser(this, null, null, Optional.of(uuid));
	}
	
	public NamelessUser getUserLazy(final int id, final String username, final UUID uuid) throws NamelessException {
		return new NamelessUser(this, id, username, Optional.of(uuid));
	}
	
	public Optional<NamelessUser> getUserByDiscordId(final long id) throws NamelessException {
		try {
			final JsonObject response = this.requests.get(Action.GET_USER_BY_DISCORD_ID, "discord_id", id);
			return Optional.of(new NamelessUser(this, response.get("id").getAsInt(), null, null));
		} catch (final ApiError e) {
			if (e.getError() == ApiError.UNABLE_TO_FIND_USER) {
				return Optional.empty();
			} else {
				throw e;
			}
		}
	}
	
	public Optional<Group> getGroup(final int id) throws NamelessException {
		final JsonObject response = this.requests.get(Action.GROUP_INFO, "id", id);
		if (!response.has("group")) {
			return Optional.empty();
		} else {
			return Optional.of(new Group(response.getAsJsonObject("group")));
		}
	}
	
	public Optional<Group> getGroup(final String name) throws NamelessException {
		final JsonObject response = this.requests.get(Action.GROUP_INFO, "name", name);
		if (!response.has("group")) {
			return Optional.empty();
		} else {
			return Optional.of(new Group(response.getAsJsonObject("group")));
		}
	}
	
	/**
	 * Registers a new account. The user will be sent an email to set a password.
	 * @param username Username
	 * @param email Email address
	 * @return Email verification disabled: A link which the user needs to click to complete registration
	 * <br>Email verification enabled: An empty string (the user needs to check their email to complete registration)
	 * @throws NamelessException
	 */
	public Optional<String> registerUser(final String username, final String email, final UUID uuid) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("username", username);
		post.addProperty("email", email);
		if (uuid != null) {
			post.addProperty("uuid", uuid.toString());
		}
		
		final JsonObject response = this.requests.post(Action.REGISTER, post);
		
		if (response.has("link")) {
			return Optional.of(response.get("link").getAsString());
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<String> registerUser(final String username, final String email) throws NamelessException {
		return registerUser(username, email, null);
	}
	
	public void verifyDiscord(final String verificationToken, final long discordUserId) throws NamelessException {
		final JsonObject json = new JsonObject();
		json.addProperty("token", verificationToken);
		json.addProperty("discord_id", discordUserId + ""); // website needs it as a string
		this.requests.post(Action.VERIFY_DISCORD, json);
	}
	
	public void setDiscordBotUrl(final URL url) throws NamelessException {
		final JsonObject json = new JsonObject();
		json.addProperty("url", url.toString());
		this.requests.post(Action.SET_DISCORD_BOT_URL, json);
	}

	@Deprecated
	static String[] jsonToArray(final JsonArray jsonArray) {
		final List<String> list = new ArrayList<>();
		jsonArray.iterator().forEachRemaining((element) -> list.add(element.getAsString()));
		return list.toArray(new String[] {});
	}

	static UUID websiteUuidToJavaUuid(final String uuid) {
		// Add dashes to uuid
		// https://bukkit.org/threads/java-adding-dashes-back-to-minecrafts-uuids.272746/
		StringBuffer sb = new StringBuffer(uuid);
		sb.insert(8, "-");

		sb = new StringBuffer(sb.toString());
		sb.insert(13, "-");

		sb = new StringBuffer(sb.toString());
		sb.insert(18, "-");

		sb = new StringBuffer(sb.toString());
		sb.insert(23, "-");

		return UUID.fromString(sb.toString());
	}


}