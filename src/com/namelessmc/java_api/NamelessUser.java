package com.namelessmc.java_api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
				this.uuid = Optional.of(NamelessAPI.websiteUuidToJavaUuid(this.userInfo.get("uuid").getAsString()));
			} else {
				this.uuid = Optional.empty();
			}
		}
		
		return this.uuid;
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
		
		return new Date(Long.parseLong(this.userInfo.get("registered").toString().replaceAll("^\"|\"$", "")) * 1000);
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
	
	public VerificationInfo getVerificationInfo() throws NamelessException {
		final boolean verified = isVerified();
		final JsonObject verification = this.userInfo.getAsJsonObject("verification");
		return new VerificationInfo(verified, verification);
	}
	
	public Group getPrimaryGroup() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}
		
		final JsonObject groups = this.userInfo.getAsJsonObject("groups");
		final JsonObject primary = groups.getAsJsonObject("primary");
		return new Group(primary.get("id").getAsInt(), primary.get("name").getAsString(), true);
	}
	
	public List<Group> getSecondaryGroups() throws NamelessException {
		if (this.userInfo == null) {
			this.loadUserInfo();
		}
		
		final JsonObject groups = this.userInfo.getAsJsonObject("groups");
		final List<Group> secondaryGroups = new ArrayList<>();
		groups.getAsJsonArray("secondary").forEach(e -> {
			final JsonObject group = e.getAsJsonObject();
			secondaryGroups.add(new Group(group.get("id").getAsInt(), group.get("name").getAsString(), false));
		});
		return Collections.unmodifiableList(secondaryGroups);
	}
	
	public List<Group> getGroups() throws NamelessException {
		final List<Group> secondaryGroups = this.getSecondaryGroups();
		final List<Group> list = new ArrayList<>(secondaryGroups.size() + 1);
		list.add(this.getPrimaryGroup());
		list.addAll(this.getSecondaryGroups());
		return Collections.unmodifiableList(list);
	}
	
	public int getNotificationCount() throws NamelessException {
		return getNotifications().size(); // TODO more efficient method
	}
	
	public List<Notification> getNotifications() throws NamelessException {
		final JsonObject response = this.requests.get(Action.GET_NOTIFICATIONS, "id", this.id);
		
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
		this.requests.post(Action.CREATE_REPORT, post.toString());
	}
	
	/**
	 * @param code
	 * @return True if the user could be validated successfully, false if the provided code is wrong
	 * @throws NamelessException
	 * @throws
	 */
	public boolean verifyMinecraft(final String code) throws NamelessException {
		final JsonObject post = new JsonObject();
		post.addProperty("id", this.id);
		post.addProperty("code", code);
		try {
			this.requests.post(Action.VERIFY_MINECRAFT, post.toString());
			return true;
		} catch (final ApiError e) {
			if (e.getError() == ApiError.INVALID_VALIDATE_CODE) {
				return false;
			} else {
				throw e;
			}
		}
	}
	
	public void verifyDiscord(final String verificationToken) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}
	
	public void setDiscordIds(final long[] discordId) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}
	
	public void addDiscordId(final long discordId) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}
	
	public void removeDiscordId(final long discordId) {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}
	
	public long[] getDiscordIds() {
		throw new UnsupportedOperationException("Not yet implemented"); // TODO
	}

}