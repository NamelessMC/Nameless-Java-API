package com.namelessmc.java_api;

import com.google.gson.JsonObject;

public class Group {
	
	private final int id;
	private final String name;
	private final boolean primary;
	
	Group(final int id, final String name, final boolean primary) {
		this.id = id;
		this.name = name;
		this.primary = primary;
	}
	
	Group(final JsonObject group) {
		this.id = group.get("id").getAsInt();
		this.name = group.get("name").getAsString();
		this.primary = group.has("primary") ? group.get("primary").getAsBoolean() : false;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public boolean isPrimary() {
		return this.primary;
	}
	
}
