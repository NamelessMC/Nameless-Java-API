package com.namelessmc.java_api.modules.suggestions;

import com.google.gson.JsonObject;
import com.namelessmc.java_api.NamelessAPI;
import com.namelessmc.java_api.exception.NamelessException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class Suggestion {

	private final int id;
	private final URL url;
	private final SuggestionUser author;
	private final SuggestionUser updatedBy;
	private final SuggestionStatus status;
	private final SuggestionCategory category;
	private final String title;
	private final String content;
	private final int views;
	private final Date createdTime;
	private final Date lastUpdatedTime;
	private final int likeCount;
	private final int dislikeCount;

	Suggestion(final NamelessAPI api, final JsonObject json) throws NamelessException {
		this.id = json.get("id").getAsInt();
		final String urlString = json.get("link").getAsString();
		try {
			this.url = new URL(urlString);
		} catch (MalformedURLException e) {
			throw new NamelessException("Website provided invalid suggestion URL: " + urlString, e);
		}
		this.author = new SuggestionUser(api, json.getAsJsonObject("author"));
		this.updatedBy = new SuggestionUser(api, json.getAsJsonObject("updated_by"));
		this.status = new SuggestionStatus(json.getAsJsonObject("status"));
		this.category = new SuggestionCategory(json.getAsJsonObject("category"));
		this.title = json.get("title").getAsString();
		this.content = json.get("content").getAsString();
		this.views = json.get("views").getAsInt();
		this.createdTime = new Date(json.get("created").getAsLong() * 1000);
		this.lastUpdatedTime = new Date(json.get("last_updated").getAsLong() * 1000);
		this.likeCount = json.get("likes_count").getAsInt();
		this.dislikeCount = json.get("dislikes_count").getAsInt();
	}

	public int id() {
		return this.id;
	}

	public URL url() {
		return this.url;
	}

	public SuggestionUser author() {
		return this.author;
	}

	public SuggestionUser updatedBy() {
		return this.updatedBy;
	}

	public SuggestionStatus status() {
		return this.status;
	}

	public SuggestionCategory category() {
		return this.category;
	}

	public String title() {
		return this.title;
	}

	public String content() {
		return this.content;
	}

	public int views() {
		return this.views;
	}

	public Date createdTime() {
		return this.createdTime;
	}

	public Date lastUpdatedTime() {
		return this.lastUpdatedTime;
	}

	public int likeCount() {
		return this.likeCount;
	}

	public int dislikeCount() {
		return this.dislikeCount;
	}

}
