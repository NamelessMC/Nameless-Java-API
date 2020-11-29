package com.namelessmc.java_api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namelessmc.java_api.Notification.NotificationType;
import com.namelessmc.java_api.RequestHandler.Action;

public final class NamelessUser {

	private final NamelessAPI api;
	private final RequestHandler requests;
	
	private Integer id;
	private String username;
	private Optional<UUID> uuid;

	private JsonObject userInfo;
	
	// only one of id, username, uuid has to be provided
	NamelessUser(final NamelessAPI api, final Integer id, final String username, final Optional<UUID> uuid) throws NamelessException {
		this.api = api;
		this.requests = api.getRequestHandler();
		
		if (id == null && username == null && uuid == null) {
			throw new IllegalArgumentException("You must specify at least one of ID, uuid, username");
		}
		
		this.id = id;
		this.username = username;
		this.uuid = uuid;
	}
	
	private void loadUserInfo() throws NamelessException {
		final JsonObject response;
		if (this.id != null) {
			response = this.requests.get(Action.USER_INFO, "id", this.id);
		} else if (this.uuid != null && this.uuid.isPresent()) {
			response = this.requests.get(Action.USER_INFO, "uuid", this.uuid.get());
		} else if (this.username != null) {
			response = this.requests.get(Action.USER_INFO, "username", this.username);
		} else {
			throw new IllegalStateException("ID, uuid, and username not known for this player.");
		}
		
		if (!response.get("exists").getAsBoolean()) {
			throw new UserNotExistException();
		}
		
		this.userInfo = response;
	}

	public NamelessAPI getApi() {
		return this.api;
	}
	
	/**
	 * The API method `userInfo` is only called once to improve performance.
	 * This means that if something changes on the website, methods that use
	 * data from the `userInfo` API method will keep returning the old data.
	 * Calling this method will invalidate the cache and require making a new
	 * API request. It will not make a new API request immediately. Calling
	 * this method multiple times while the cache is already cleared has no
	 * effect.
	 */
	public void invalidateCache() {
		this.userInfo = null;
	}
	
	public int getId() throws NamelessException {
		if (this.id == null) {
			this.loadUserInfo();
			this.id = this.userInfo.get("id").getAsInt();
		}
		
		return this.id;
	}
	
	public String getUsername() throws NamelessException {
		if (this.username == null) {
			this.loadUserInfo();
			this.username = this.userInfo.get("username").getAsString();
		}
		
		return this.username;
	}
	
	public Optional<UUID> getUniqueId() throws NamelessException {
		if (this.uuid == null) {
			this.loadUserInfo();
			if (this.userInfo.has("uuid")) {
				final String uuidString = this.userInfo.get("uuid").getAsString();
				if (uuidString == null ||
						uuidString.equals("none") ||
						uuidString.equals("")) {
					this.uuid = Optional.empty();
				} else {
					this.uuid = Optional.of(NamelessAPI.websiteUuidToJavaUuid(uuidString));
				}
			} else {
				this.uuid = Optional.empty();
			}
		}
		
		return this.uuid;
	}
	
	public boolean exists() throws NamelessException {
		if (this.userInfo == null) {
			try {
				loadUserInfo();
			} catch (final UserNotExistException e) {
				return false;
			}
		}
		
		return true;
	}

	public String getDisplayName() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}
		
		return this.userInfo.get("displayname").getAsString();
	}


	/**
	 * @return The date the user registered on the website.
	 * @throws NamelessException
	 */
	public Date getRegisteredDate() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}
		
		return new Date(this.userInfo.get("registered_timestamp").getAsLong() * 1000);
	}
	
	public Date getLastOnline() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}
		
		return new Date(this.userInfo.get("last_online_timestamp").getAsLong() * 1000);
	}

	/**
	 * @return Whether this account is banned from the website.
	 * @throws NamelessException
	 */
	public boolean isBanned() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}
		
		return this.userInfo.get("banned").getAsBoolean();
	}
	
	public boolean isVerified() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}
		
		return this.userInfo.get("verified").getAsBoolean();
	}
	
	public String getLangage() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}
		
		return this.userInfo.get("language").getAsString();
	}
	
	public VerificationInfo getVerificationInfo() throws NamelessException {
		final boolean verified = isVerified();
		final JsonObject verification = this.userInfo.getAsJsonObject("verification");
		return new VerificationInfo(verified, verification);
	}
	
	/**
	 * @return True if the user is member of at least one staff group, otherwise false
	 * @throws NamelessException
	 */
	public boolean isStaff() throws NamelessException {
		for (final Group group : this.getGroups()) {
			if (group.isStaff()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return List of the user's groups, sorted from low order to high order.
	 * @throws NamelessException
	 */
	public List<Group> getGroups() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}
		
		return StreamSupport.stream(this.userInfo.getAsJsonArray("groups").spliterator(), false)
				.map(JsonElement::getAsJsonObject)
				.map(Group::new)
				.sorted()
				.collect(Collectors.toList());
	}
	
	/**
	 * Same as doing {@link #getGroups()}.get(0), but with better performance
	 * since it doesn't need to create and sort a list of group objects.
	 * Empty if the user is not in any groups.
	 * @return Player's group with lowest order
	 * @throws NamelessException
	 */
	public Optional<Group> getPrimaryGroup() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}
		
		final JsonArray groups = this.userInfo.getAsJsonArray("groups");
		if (groups.size() > 0) {
			return Optional.of(new Group(groups.get(0).getAsJsonObject()));
		} else {
			return Optional.empty();
		}
	}
	
	public void addGroups(final Group... groups) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("user", this.id);
		post.add("groups", groupsToJsonArray(groups));
		this.requests.post(Action.ADD_GROUPS, post);
		this.userInfo = null; // Groups modified, invalidate cache
	}
	
	public void removeGroups(final Group... groups) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("user", this.id);
		post.add("groups", groupsToJsonArray(groups));
		this.requests.post(Action.REMOVE_GROUPS, post);
		this.userInfo = null; // Groups modified, invalidate cache
	}
	
	private JsonArray groupsToJsonArray(final Group[] groups) {
		final JsonArray array = new JsonArray();
		for (final Group group : groups) {
			array.add(group.getId());
		}
		return array;
	}
	
	public int getNotificationCount() throws NamelessException {
		final JsonObject response = this.requests.get(Action.GET_NOTIFICATIONS, "user", this.id);
		return response.getAsJsonArray("notifications").size();
	}
	
	public List<Notification> getNotifications() throws NamelessException {
		final JsonObject response = this.requests.get(Action.GET_NOTIFICATIONS, "user", this.id);
		
		final List<Notification> notifications = new ArrayList<>();
		response.getAsJsonArray("notifications").forEach((element) -> {
			final String message = element.getAsJsonObject().get("message").getAsString();
			final String url = element.getAsJsonObject().get("url").getAsString();
			final NotificationType type = NotificationType.fromString(element.getAsJsonObject().get("type").getAsString());
			notifications.add(new Notification(message, url, type));
		});
		
		return notifications;
	}

	/**
	 * Reports a player
	 * @param username Username of the player or user to report
	 * @param reason Reason why this player has been reported
	 * @throws NamelessException
	 */
	public void createReport(final String username, final String reason) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("reporter", this.id);
		post.addProperty("reported", username);
		post.addProperty("content", reason);
		this.requests.post(Action.CREATE_REPORT, post);
	}
	
	/**
	 * @param code
	 * @return True if the user could be validated successfully, false if the provided code is wrong
	 * @throws NamelessException
	 * @throws
	 */
	public boolean verifyMinecraft(final String code) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("user", this.id);
		post.addProperty("code", code);
		try {
			this.requests.post(Action.VERIFY_MINECRAFT, post);
			this.userInfo = null;
			return true;
		} catch (final ApiError e) {
			if (e.getError() == ApiError.INVALID_VALIDATE_CODE) {
				return false;
			} else {
				throw e;
			}
		}
		
	}
	
	public Optional<Integer> getDiscordId() throws NamelessException {
		final JsonObject response = this.requests.get(Action.GET_DISCORD_ID, "user", this.id);
		if (response.has("discord_id")) {
			return Optional.of(response.get("discord_id").getAsInt());
		} else {
			return Optional.empty();
		}
	}
	
	public long[] getDiscordRoles() throws NamelessException {
		final JsonObject response = this.requests.get(Action.GET_DISCORD_ROLES, "user", this.id);
		return StreamSupport.stream(response.getAsJsonArray("roles").spliterator(), false)
				.mapToLong(JsonElement::getAsLong).toArray();
	}
	
	public void setDiscordRoles(final long[] roleIds) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("user", this.id);
		post.add("roles", new Gson().toJsonTree(roleIds));
		this.requests.post(Action.SET_DISCORD_ROLES, post);
	}

	public void addDiscordRoles(final long... roleIds) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("user", this.id);
		post.add("roles", new Gson().toJsonTree(roleIds));
		this.requests.post(Action.ADD_DISCORD_ROLES, post);
	}
	
	public void removeDiscordRoles(final long... roleIds) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("user", this.id);
		post.add("roles", new Gson().toJsonTree(roleIds));
		this.requests.post(Action.REMOVE_DISCORD_ROLES, post);
	}

}