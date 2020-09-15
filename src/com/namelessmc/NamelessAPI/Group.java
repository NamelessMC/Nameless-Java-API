package com.namelessmc.NamelessAPI;

public class Group {
	
	private final int id;
	private final String name;
	private final boolean primary;
	
	Group(int id, String name, boolean primary) {
		this.id = id;
		this.name = name;
		this.primary = primary;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isPrimary() {
		return primary;
	}

}
