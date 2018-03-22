package com.namelessmc.NamelessAPI;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.namelessmc.NamelessAPI.Request.Action;

public final class NamelessAPI {

	private NamelessAPI() {}
	
	/**
	 * Checks if a web API connection can be established
	 * @return An exception if the connection was unsuccessful, null if the connection was successful.
	 */
	public static Throwable checkWebAPIConnection(URL url) {		
		try {
			JsonObject response = new Request(url, Action.INFO).getResponse();
			if (response.has("version")) {
				return null;
			} else {
				return new NamelessException("Invalid respose: " + response.getAsString());
			}
		} catch (NamelessException e) {
			if (e.getCause() != null)
				return e.getCause();
			else
				return e;
		}
	}
	
	static String encode(Object object) {
		try {
			return URLEncoder.encode(object.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	static String[] jsonToArray(JsonArray jsonArray) {
		List<String> list = new ArrayList<>();
		jsonArray.iterator().forEachRemaining((element) -> list.add(element.getAsString()));
		return list.toArray(new String[] {});
	}
	
	/**
	 * Get all announcements
	 * @param apiUrl
	 * @return
	 * @throws NamelessException
	 */
	public static List<Announcement> getAnnouncements(URL apiUrl) throws NamelessException {
		Request request = new Request(apiUrl, Action.GET_ANNOUNCEMENTS);
		
		List<Announcement> announcements = new ArrayList<>();
		
		JsonObject object = request.getResponse();
		object.getAsJsonArray().forEach((element) -> {
			JsonObject announcementJson = element.getAsJsonObject();
			String content = announcementJson.get("content").getAsString();
			String[] display = jsonToArray(announcementJson.get("display").getAsJsonArray());
			String[] permissions = jsonToArray(announcementJson.get("permissions").getAsJsonArray());
			announcements.add(new Announcement(content, display, permissions));
		});
		
		return announcements;
	}
	
	/**
	 * Get all announcements visible for the player with the specified uuid
	 * @param apiUrl
	 * @return
	 * @throws NamelessException
	 */
	public static List<Announcement> getAnnouncements(URL apiUrl, UUID uuid) throws NamelessException {
		Request request = new Request(apiUrl, Action.GET_ANNOUNCEMENTS, new ParameterBuilder().add("uuid", uuid).build());
		
		List<Announcement> announcements = new ArrayList<>();
		
		JsonObject object = request.getResponse();
		object.getAsJsonArray().forEach((element) -> {
			JsonObject announcementJson = element.getAsJsonObject();
			String content = announcementJson.get("content").getAsString();
			String[] display = jsonToArray(announcementJson.get("display").getAsJsonArray());
			String[] permissions = jsonToArray(announcementJson.get("permissions").getAsJsonArray());
			announcements.add(new Announcement(content, display, permissions));
		});
		
		return announcements;
	}
	
	public static void submitServerInfo(URL apiUrl, String jsonData) throws NamelessException {
		Request request = new Request(apiUrl, Action.SERVER_INFO, new ParameterBuilder().add("info", jsonData).build());
		request.getResponse();
	}


}