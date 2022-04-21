package com.namelessmc.java_api;

import com.google.gson.JsonObject;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class Group implements Comparable<Group> {

	private final int id;
	private final @NonNull String name;
	private final int order;
	private final boolean staff;

	Group(final @NonNull JsonObject group) {
		this.id = group.get("id").getAsInt();
		this.name = group.get("name").getAsString();
		this.order = group.get("order").getAsInt();
		this.staff = group.has("staff") && group.get("staff").getAsBoolean();
	}

	public int getId() {
		return this.id;
	}

	public @NonNull String getName() {
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
	public boolean equals(final @Nullable Object other) {
		return other instanceof Group &&
				((Group) other).id == this.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(839891, id);
	}

	@Override
	public @NonNull String toString() {
		return "Group[id=" + id + ",name=" + name + "]";
	}

}
