package com.namelessmc.java_api;

import com.google.gson.JsonObject;

public class Group implements Comparable<Group> {
	
	private final int id;
	private final String name;
	private int order;
	private final boolean staff;
	
	Group(final JsonObject group) {
		this.id = group.get("id").getAsInt();
		this.name = group.get("name").getAsString();
		this.staff = group.has("staff") ? group.get("staff").getAsBoolean() : false;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getOrder() {
		return this.order;
	}
	
	public boolean isStaff() {
		return this.staff;
	}

	@Override
	public int compareTo(final Group other) {
		return this.order - other.order;
	}
	
}
