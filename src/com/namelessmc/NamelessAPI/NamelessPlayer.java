package com.namelessmc.NamelessAPI;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.namelessmc.NamelessAPI.utils.NamelessPostString;
import com.namelessmc.NamelessAPI.utils.NamelessRequestUtil;
import com.namelessmc.NamelessAPI.utils.NamelessRequestUtil.Request;

public class NamelessPlayer {

	private String userName;
	private String displayName;
	private String uuid;
	private int groupID;
	private int reputation;
	private Date registeredDate;
	private boolean exists;
	private boolean validated;
	private boolean banned;
	
	private URL baseUrl;
	
	private JsonParser parser;

	public NamelessPlayer(UUID uuid, URL baseUrl) {	
		this.baseUrl = baseUrl;
		
		Request request = NamelessRequestUtil.sendPostRequest(baseUrl, "get", "uuid=" + NamelessPostString.urlEncodeString(uuid.toString()));
		
		parser = new JsonParser();
		JsonObject response = request.getResponse();
		
		if (!request.hasSucceeded()) {
			exists = false;
			return;
		}

		//No errors, parse response
		
		JsonObject message = parser.parse(response.get("message").getAsString()).getAsJsonObject();

		exists = true;

		// Convert UNIX timestamp to date
		Date registered = new Date(Long.parseLong(message.get("registered").toString().replaceAll("^\"|\"$", "")) * 1000);

		// Display get user.
		userName = message.get("username").getAsString();
		displayName = message.get("displayname").getAsString();
		uuid = UUID.fromString(message.get("uuid").getAsString());
		groupID = message.get("group_id").getAsInt();
		registeredDate = registered;
		reputation = message.get("reputation").getAsInt();
		validated = message.get("validated").getAsString().equals("1");
		banned = message.get("banned").getAsString().equals("1");
	}

	public String getUsername() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return userName;
	}

	public String getDisplayName() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return displayName;
	}

	public String getUUID() {
		return uuid;
	}

	public int getGroupID() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return groupID;
	}

	public int getReputations() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return reputation;
	}

	public Date getRegisteredDate() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return registeredDate;
	}

	public boolean exists() {	
		return exists;
	}

	public boolean isValidated() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return validated;
	}

	public boolean isBanned() {
		if (!exists) {
			throw new UnsupportedOperationException("This player does not exist.");
		}
		
		return banned;
	}
	
	public int getAlertCount() throws NamelessException {
		String postString = "uuid=" + NamelessPostString.urlEncodeString(uuid.toString());
		Request request = NamelessRequestUtil.sendPostRequest(baseUrl, "getNotifications", postString);
		
		if (!request.hasSucceeded()) {
			throw new NamelessException(request.getException());
		}
		
		JsonObject response = request.getResponse();
		JsonObject message = parser.parse(response.get("message").getAsString()).getAsJsonObject();
		return message.get("alerts").getAsInt();
	}
	
	public int getMessageCount() throws NamelessException {
		String postString = "uuid=" + NamelessPostString.urlEncodeString(uuid.toString());
		Request request = NamelessRequestUtil.sendPostRequest(baseUrl, "getNotifications", postString);
		
		if (!request.hasSucceeded()) {
			throw new NamelessException(request.getException());
		}
		
		JsonObject response = request.getResponse();
		JsonObject message = parser.parse(response.get("message").getAsString()).getAsJsonObject();
		return message.get("messages").getAsInt();
	}
	
	public void setGroup(int groupId) throws NamelessException {
		Request request = NamelessRequestUtil.sendPostRequest(baseUrl, "setGroup", "uuid=" + NamelessPostString.urlEncodeString(uuid.toString()) + "?group_id=");
		
		if (!request.hasSucceeded()) {
			throw new NamelessException(request.getException());
		}
	}

	public void updateUsername(String newUserName) throws NamelessException {
		String encodedUuid = NamelessPostString.urlEncodeString(uuid.toString());
		String encodedName = NamelessPostString.urlEncodeString(newUserName);
		String postString = "id=" + encodedUuid + "?new_username=" + encodedName;
		
		Request request = NamelessRequestUtil.sendPostRequest(baseUrl, "updateUsername", postString);
		
		if (!request.hasSucceeded()) {
			throw new NamelessException(request.getException());
		}
	}
	
	public void register(String minecraftName, String email) throws NamelessException {
		String encodedUuid = NamelessPostString.urlEncodeString(uuid.toString());
		String encodedName = NamelessPostString.urlEncodeString(minecraftName);
		String encodedEmail = NamelessPostString.urlEncodeString(email);
		
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

	public void reportPlayer(UUID reportedUuid, String reportedUsername, String reason) throws NamelessException {
		String encodedReporterUuid = NamelessPostString.urlEncodeString(uuid.toString());
		String encodedReportedUuid = NamelessPostString.urlEncodeString(reportedUuid.toString());
		String encodedName = NamelessPostString.urlEncodeString(reportedUsername);
		String encodedReason = NamelessPostString.urlEncodeString(reason);
		
		String postString = String.format("reporter_uuid=%s?reported_uuid=%s?reported_username=%s?content=%s", 
				encodedReporterUuid, encodedReportedUuid, encodedName, encodedReason);
		
		Request request = NamelessRequestUtil.sendPostRequest(baseUrl, "createReport", postString);
		
		if (!request.hasSucceeded()) {
			throw new NamelessException(request.getException());
		}
	
	}

}