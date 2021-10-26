package com.namelessmc.java_api;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class Announcement {

	private final int id;
	private final @NotNull String header;
	private final @NotNull String message;
	private final @NotNull Set<@NotNull String> displayPages;
	private final int @NotNull[] displayGroups;

	Announcement(int id, JsonObject announcementJson) {
		this.id = id;
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

	public int getId() {
		return id;
	}

	public @NotNull String getHeader() {
		return this.header;
	}

	public @NotNull String getMessage() {
		return this.message;
	}

	@Deprecated
	public @NotNull String getContent() {
		return this.message;
	}


	public @NotNull Set<@NotNull String> getDisplayPages() {
		return this.displayPages;
	}

	public int @NotNull[] getDisplayGroupIds() {
		return this.displayGroups;
	}

}
