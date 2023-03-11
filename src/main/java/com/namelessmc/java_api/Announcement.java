package com.namelessmc.java_api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Announcement {

	private final int id;
	private final @NonNull String header;
	private final @NonNull String message;
	private final @NonNull Set<@NonNull String> displayPages;
	private final int @NonNull[] displayGroups;

	Announcement(@NonNull JsonObject announcementJson) {
		this.id = announcementJson.get("id").getAsInt();
		this.header = announcementJson.get("header").getAsString();
		this.message = announcementJson.get("message").getAsString();
		this.displayPages = Collections.unmodifiableSet(
				StreamSupport.stream(announcementJson.getAsJsonArray("pages").spliterator(), false)
						.map(JsonElement::getAsString)
						.collect(Collectors.toSet())
				);
		this.displayGroups = StreamSupport.stream(announcementJson.getAsJsonArray("groups").spliterator(), false)
						.mapToInt(JsonElement::getAsInt)
						.toArray();
	}

	public int id() {
		return id;
	}

	public @NonNull String header() {
		return this.header;
	}

	public @NonNull String message() {
		return this.message;
	}

	public @NonNull Set<@NonNull String> displayedPages() {
		return this.displayPages;
	}

	public int @NonNull[] displayedGroupIds() {
		return this.displayGroups;
	}

}
