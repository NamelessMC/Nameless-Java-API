package com.namelessmc.java_api;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;

import java.util.Objects;

public class Group implements Comparable<Group> {

	private final int id;
	@NotNull
	private final String name;
	private final int order;
	private final boolean staff;

	Group(@NotNull final JsonObject group) {
		this.id = group.get("id").getAsInt();
		this.name = group.get("name").getAsString();
		this.order = group.get("order").getAsInt();
		this.staff = group.has("staff") && group.get("staff").getAsBoolean();
	}

	public int getId() {
		return this.id;
	}

	@NotNull
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

	@Override
	public boolean equals(Object other) {
		return other instanceof Group &&
				((Group) other).id == this.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(839891, id);
	}

	@Override
	public String toString() {
		return "Group[id=" + id + ",name=" + name + "]";
	}

}
