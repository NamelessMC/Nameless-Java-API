package com.namelessmc.java_api;

public class Group {
	
	private final int id;
	private final String name;
	private final boolean primary;
	
	Group(final int id, final String name, final boolean primary) {
		this.id = id;
		this.name = name;
		this.primary = primary;
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
