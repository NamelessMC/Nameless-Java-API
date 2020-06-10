package com.namelessmc.NamelessAPI;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.namelessmc.NamelessAPI.Request.Action;
import com.namelessmc.NamelessAPI.Website.Update;

public final class NamelessAPI {

	private static final String DEFAULT_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)";

	public static boolean DEBUG_MODE = false;

	private final URL apiUrl;

	private String userAgent = null;

	/**
	 * @param apiUrl URL of API to connect to, in the format http(s)://yoursite.com/index.php?route=/api/v2/API_KEY
	 * @param debug If debug is set to true, debug messages are enabled for <i>every</i> NamelessAPI instance.
	 */
	public NamelessAPI(final URL apiUrl, final boolean debug) {
		if (debug) {
			DEBUG_MODE = true;
		}
		this.apiUrl = apiUrl;
		this.userAgent = DEFAULT_USER_AGENT;
	}

	/**
	 * @param host URL of your website, in the format http(s)://yoursite.com
	 * @param apiKey API key
	 * @param debug If debug is set to true, debug messages are enabled for <i>every</i> NamelessAPI instance.
	 * @throws MalformedURLException
	 */
	public NamelessAPI(final String host, final String apiKey, final boolean debug) throws MalformedURLException {
		if (debug) {
			DEBUG_MODE = true;
		}

		this.apiUrl = new URL(host + "/index.php?route=/api/v2/" + apiKey);
		this.userAgent = DEFAULT_USER_AGENT;
	}

	/**
	 * Checks if a web API connection can be established
	 * @return
	 * <ul>
	 *   <li>An {@link ApiError} if the api has returned an error</li>
	 *   <li>A {@link NamelessException} if the connection was unsuccessful</li>
	 *   <li>null if the connection was successful.</li>
	 * </ul>
	 */
	public NamelessException checkWebAPIConnection() {
		try {
			final Request request = new Request(this.apiUrl, this.userAgent,  Action.INFO);
			request.connect();

			if (request.hasError()) {
				throw new ApiError(request.getError());
			}

			final JsonObject response = request.getResponse();
			if (response.has("nameless_version")) {
				return null;
			} else {
				return new NamelessException("Invalid respose: " + response.getAsString());
			}
		} catch (final NamelessException e) {
			return e;
		}
	}

	/**
	 * Get all announcements
	 * @return list of current announcements
	 * @throws NamelessException if there is an error in the request
	 */
	public List<Announcement> getAnnouncements() throws NamelessException {
		final Request request = new Request(this.apiUrl, this.userAgent,  Action.GET_ANNOUNCEMENTS);
		request.connect();

		if (request.hasError()) {
			throw new ApiError(request.getError());
		}

		final List<Announcement> announcements = new ArrayList<>();

		final JsonObject object = request.getResponse();
		object.getAsJsonArray().forEach((element) -> {
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
	public List<Announcement> getAnnouncements(final UUID uuid) throws NamelessException {
		final Request request = new Request(this.apiUrl, this.userAgent, Action.GET_ANNOUNCEMENTS, new ParameterBuilder().add("uuid", uuid).build());
		request.connect();

		if (request.hasError()) {
			throw new ApiError(request.getError());
		}

		final List<Announcement> announcements = new ArrayList<>();

		request.getResponse().get("announcements").getAsJsonArray().forEach((element) -> {
			final JsonObject announcementJson = element.getAsJsonObject();
			final String content = announcementJson.get("content").getAsString();
			final String[] display = jsonToArray(announcementJson.get("display").getAsJsonArray());
			final String[] permissions = jsonToArray(announcementJson.get("permissions").getAsJsonArray());
			announcements.add(new Announcement(content, display, permissions));
		});

		return announcements;
	}

	public void submitServerInfo(final String jsonData) throws NamelessException {
		final Request request = new Request(this.apiUrl, this.userAgent, Action.SERVER_INFO, new ParameterBuilder().add("info", jsonData).build());
		request.connect();
		if (request.hasError()) {
			throw new ApiError(request.getError());
		}
	}

	public Website getWebsite() throws NamelessException {
		final Request request = new Request(this.apiUrl, this.userAgent, Action.INFO);
		request.connect();

		if (request.hasError()) {
			throw new ApiError(request.getError());
		}

		final JsonObject json = request.getResponse();

		final String version = json.get("nameless_version").getAsString();

		final String[] modules = jsonToArray(json.get("modules").getAsJsonArray());

		final JsonObject updateJson = json.get("version_update").getAsJsonObject();
		final boolean updateAvailable = updateJson.get("update").getAsBoolean();
		Update update;
		if (updateAvailable) {
			final String updateVersion = updateJson.get("version").getAsString();
			final boolean isUrgent = updateJson.get("urgent").getAsBoolean();
			update = new Update(isUrgent, updateVersion);
		} else {
			update = null;
		}

		return new Website(version, update, modules);

	}

	public boolean validateUser(final UUID uuid, final String code) throws NamelessException {
		final String[] parameters = new ParameterBuilder().add("uuid", uuid.toString()).add("code", code).build();
		final Request request = new Request(this.apiUrl, this.userAgent, Action.VALIDATE_USER, parameters);
		request.connect();
		if (request.hasError()) {
			final int errorCode = request.getError();
			if (errorCode == 28) {
				return false;
			}
			throw new ApiError(errorCode);
		}
		return true;
	}

	public NamelessPlayer getPlayer(final UUID uuid) throws NamelessException {
		return new NamelessPlayer(uuid, this.apiUrl, this.userAgent);
	}

	public Map<UUID, String> getRegisteredUsers(final boolean hideInactive, final boolean hideBanned) throws NamelessException {
		final Request request = new Request(this.apiUrl, this.userAgent, Action.LIST_USERS);
		request.connect();
		if (request.hasError()) {
			throw new ApiError(request.getError());
		}

		final Map<UUID, String> users = new HashMap<>();

		request.getResponse().get("users").getAsJsonArray().forEach(userJsonElement -> {
			final String uuid = userJsonElement.getAsJsonObject().get("uuid").getAsString();
			final String username = userJsonElement.getAsJsonObject().get("username").getAsString();
			final String active = userJsonElement.getAsJsonObject().get("active").getAsString();
			final String banned = userJsonElement.getAsJsonObject().get("banned").getAsString();

			if (!(
					uuid.equals("none") ||
					hideInactive && active.equals("0") ||
					hideBanned && banned.equals("1")
					)) {
				try {
					users.put(websiteUuidToJavaUuid(uuid), username);
				} catch (final StringIndexOutOfBoundsException e) {
					System.err.println("NamelessMC API - Skipped user with invalid UUID '" + uuid + "' (username: '" + username + "')");
				}
			}
		});

		return users;
	}

	public List<NamelessPlayer> getRegisteredUsersAsNamelessPlayerList(final boolean hideInactive, final boolean hideBanned) throws NamelessException {
		final Map<UUID, String> users = this.getRegisteredUsers(hideInactive, hideBanned);
		final List<NamelessPlayer> namelessPlayers = new ArrayList<>();

		for (final UUID userUuid : users.keySet()) {
			namelessPlayers.add(this.getPlayer(userUuid));
		}

		return namelessPlayers;
	}

	public void setUserAgent(final String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 *
	 * @return Configured user agent or {@code null} if not configured.
	 */
	public String getUserAgent() {
		return this.userAgent;
	}

	static String encode(final Object object) {
		try {
			return URLEncoder.encode(object.toString(), "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

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