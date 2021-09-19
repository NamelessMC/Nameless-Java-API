package com.namelessmc.java_api;

import java.util.Set;

public class Announcement {

	private final String content;
	private final Set<String> displayPages;
	private final Set<String> displayRanks;

	Announcement(final String content, final Set<String> displayPages, final Set<String> displayRanks) {
		this.content = content;
		this.displayPages = displayPages;
		this.displayRanks = displayRanks;
	}

	public String getContent() {
		return this.content;
	}

	public Set<String> getDisplayPages() {
		return this.displayPages;
	}

	public Set<String> getDisplayRanks() {
		return this.displayRanks;
	}

}
