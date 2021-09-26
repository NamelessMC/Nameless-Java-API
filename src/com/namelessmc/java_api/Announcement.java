package com.namelessmc.java_api;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

public class Announcement {

	@NotNull
	private final String content;
	@NotNull
	private final Set<@NotNull String> displayPages;
	@NotNull
	private final Set<@NotNull String> displayRanks;

	Announcement(@NotNull final String content, @NotNull final Set<@NotNull String> displayPages, @NotNull final Set<@NotNull String> displayRanks) {
		this.content = content;
		this.displayPages = displayPages;
		this.displayRanks = displayRanks;
	}

	@NotNull
	public String getContent() {
		return this.content;
	}

	@NotNull
	public Set<@NotNull String> getDisplayPages() {
		return this.displayPages;
	}

	@NotNull
	public Set<@NotNull String> getDisplayRanks() {
		return this.displayRanks;
	}

}
