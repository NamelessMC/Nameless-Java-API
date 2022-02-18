package com.namelessmc.java_api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CustomProfileField {

	private final int id;
	private final @NotNull String name;
	private final int type; // TODO make this an enum
	private final boolean isPublic;
	private final boolean isRequired;
	private final @NotNull String description;

	CustomProfileField(int id, String name, int type, boolean isPublic, boolean isRequired, String description) {
		this.id = id;
		this.name = name;
		this.type = type;
		this.isPublic = isPublic;
		this.isRequired = isRequired;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public @NotNull String getName() {
		return name;
	}

	public int getType() {
		return type;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof CustomProfileField &&
				((CustomProfileField) other).id == this.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

}
