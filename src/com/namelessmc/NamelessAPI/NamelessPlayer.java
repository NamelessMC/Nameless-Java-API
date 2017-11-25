package com.namelessmc.NamelessAPI;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.NamelessAPI.Request.Action;

public final class NamelessPlayer {

	private String userName;
	private String displayName;
	private UUID uuid;
	private int groupID;
	private int reputation;
	private Date registeredDate;
	private boolean exists;
	private boolean validated;
	private boolean banned;
	
	private URL baseUrl;
	
	private JsonParser parser;

	/**
	 * Creates a new NamelessPlayer object. This constructor should not be called in the main server thread.
	 * @param uuid
	 * @param baseUrl Base API URL: <i>http(s)://yoursite.com/api/v2/API_KEY<i>
	 * @see #NamelessPlayer(String, URL)
	 */
	public NamelessPlayer(UUID uuid, URL baseUrl) {	
		parser = new JsonParser();
		this.baseUrl = baseUrl;
		
		Request request = NamelessRequestUtil.sendPostRequest(baseUrl, "userinfo", "uuid=" + NamelessAPI.encode(uuid));
		init(request);
	}
	
	/**
	 * Creates a new NamelessPlayer object. This constructor should not be called in the main server thread.
	 * @param username
	 * @param baseUrl
	 * @see #NamelessPlayer(UUID, URL)
	 */
	public NamelessPlayer(String username, URL baseUrl) {	
		this.parser = new JsonParser();
		this.baseUrl = baseUrl;
		
		Request request = NamelessRequestUtil.sendPostRequest(baseUrl, "userinfo", "username=" + NamelessAPI.encode(username));
		init(request);
	}
	
	private void init(Request request) {	
		if (!request.hasSucceeded()) {
			exists = false;
			return;
		}
		
		//No errors, parse response
		JsonObject response = request.getResponse();
		JsonObject message = parser.parse(response.get("message").getAsString()).getAsJsonObject();

		exists = true;

		// Convert UNIX timestamp to date
		Date registered = new Date(Long.parseLong(message.get("registered").toString().replaceAll("^\"|\"$", "")) * 1000);

		// Display get user.
		userName = message.get("username").getAsString();
		displayName = message.get("displayname").getAsString();
		uuid = UUID.fromString(addDashesToUUID(message.get("uuid").getAsString()));
		groupID = message.get("group_id").getAsInt();
		registeredDate = registered;
		validated = message.get("validated").getAsString().equals("1");
		reputation = message.get("reputation").getAsInt();
		//banned = message.get("banned").getAsString().equals("1");
	}

	public static String addDashesToUUID(String uuid) {
		// https://bukkit.org/threads/java-adding-dashes-back-to-minecrafts-uuids.272746/
		StringBuffer sb = new StringBuffer(uuid);
		sb.insert(8, "-");
		 
		sb = new StringBuffer(sb.toString());
		sb.insert(13, "-");
		 
		sb = new StringBuffer(sb.toString());
		sb.insert(18, "-");
		 
		sb = new StringBuffer(sb.toString());
		sb.insert(23, "-");
		 
		return sb.toString();
	}
	
	/**
	 * @return The Minecraft username associated with the provided UUID. This is not always the name displayed on the website.
	 * @see #getDisplayName()
	 */
	public String getUsername() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return userName;
	}

	/**
	 * @return The name this player uses on the website. This is not always the same as their Minecraft username.
	 * @see #getUsername()
	 */
	public String getDisplayName() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return displayName;
	}
	
	/**
	 * @return Minecraft UUID of this player.
	 * @see #getUsername()
	 */
	public UUID getUniqueId() {
		return uuid;
	}

	/**
	 * @return A numerical group id.
	 */
	public int getGroupID() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return groupID;
	}

	/**
	 * @return The user's site reputation.
	 */
	public int getReputations() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return reputation;
	}

	/**
	 * @return The date the user registered on the website.
	 */
	public Date getRegisteredDate() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return registeredDate;
	}

	/**
	 * @return Whether an account associated with the UUID exists.
	 * @see #getUUID()
	 */
	public boolean exists() {	
		return exists;
	}

	/**
	 * @return Whether this account has been validated. An account is validated when a password is set.
	 */
	public boolean isValidated() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return validated;
	}

	/**
	 * @return Whether this account is banned from the website.
	 * @deprecated Api no longer returns this?
	 */
	@Deprecated
	public boolean isBanned() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return banned;
	}
	
	public List<Notification> getNotifications() throws NamelessException {
		final List<Notification> notifications = new ArrayList<>();
		
		Request request = new Request(baseUrl, Action.GET_NOTIFICATIONS, new ParameterBuilder().add("uuid", uuid).build());
		
		JsonObject object = request.getResponse();
		object.getAsJsonArray().forEach((element) -> {
			String message = element.getAsJsonObject().get("message").getAsString();
			String url = element.getAsJsonObject().get("url").getAsString();
			notifications.add(new Notification(message, url));
		});
		
		return notifications;
	}
	
	/**
	 * Sets the players group
	 * @param groupId Numerical ID associated with a group
	 * @throws NamelessException
	 */
	public void setGroup(int groupId) throws NamelessException {
		Request request = new Request(baseUrl, Action.SET_GROUP, new ParameterBuilder().add("uuid", uuid).add("group-id", groupId).build());
		request.connect();
	}

	/**
	 * Changes the players username on the website. You should check if another account with the name <i>newUserName</i> exists before calling this method. 
	 * @param newUserName
	 * @throws NamelessException
	 */
	public void updateUsername(String newUserName) throws NamelessException {
		String encodedUuid = NamelessAPI.encode(uuid);
		String encodedName = NamelessAPI.encode(newUserName);
		String postString = "id=" + encodedUuid + "&new_username=" + encodedName;
		
		Request request = NamelessRequestUtil.sendPostRequest(baseUrl, "updateUsername", postString);
		
		if (!request.hasSucceeded()) {
			throw new NamelessException(request.getException());
		}
	}
	
	/**
	 * Registers a new account. The player will be sent an email to set a password.
	 * @param minecraftName In-game name for this player
	 * @param email Email address
	 * @throws NamelessException
	 */
	public void register(String minecraftName, String email) throws NamelessException {
		String encodedUuid = NamelessAPI.encode(uuid);
		String encodedName = NamelessAPI.encode(minecraftName);
		String encodedEmail = NamelessAPI.encode(email);
		
		String postString = String.format("username=%s&uuid=%s&email=%s", encodedUuid, encodedName, encodedEmail);

		Request request = NamelessRequestUtil.sendPostRequest(baseUrl, "register", postString);

		if (!request.hasSucceeded()) {
			String errorMessage = request.getException().getMessage();
			if (errorMessage.contains("Username") || errorMessage.contains("UUID") || errorMessage.contains("Email")) {
				throw new IllegalArgumentException(errorMessage);
			} else {
				throw new NamelessException(request.getException());
			}
		}
	}

	/**
	 * Reports a player
	 * @param reportedUuid UUID of the reported player
	 * @param reportedUsername In-game name of the reported player
	 * @param reason Reason why this player has been reported
	 * @throws NamelessException
	 */
	public void reportPlayer(UUID reportedUuid, String reportedUsername, String reason) throws NamelessException {
		String encodedReporterUuid = NamelessAPI.encode(uuid);
		String encodedReportedUuid = NamelessAPI.encode(reportedUuid);
		String encodedName = NamelessAPI.encode(reportedUsername);
		String encodedReason = NamelessAPI.encode(reason);
		
		String postString = String.format("reporter_uuid=%s&reported_uuid=%s&reported_username=%s&content=%s", 
				encodedReporterUuid, encodedReportedUuid, encodedName, encodedReason);
		
		Request request = NamelessRequestUtil.sendPostRequest(baseUrl, "createReport", postString);
		
		if (!request.hasSucceeded()) {
			throw new NamelessException(request.getException());
		}
	
	}

}