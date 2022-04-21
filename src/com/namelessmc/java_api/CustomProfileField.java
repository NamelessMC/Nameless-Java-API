package com.namelessmc.java_api;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class CustomProfileField {

	private final int id;
	private final @NonNull String name;
	private final @NonNull CustomProfileFieldType type;
	private final boolean isPublic;
	private final boolean isRequired;
	private final @NonNull String description;

	CustomProfileField(final int id,
					   final @NonNull String name,
					   final @NonNull CustomProfileFieldType type,
					   final boolean isPublic,
					   final boolean isRequired,
					   final @NonNull String description) {
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

	public @NonNull String getName() {
		return name;
	}

	public @NonNull CustomProfileFieldType getType() {
		return type;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public @NonNull String getDescription() {
		return description;
	}

	@Override
	public boolean equals(final @Nullable Object other) {
		return other instanceof CustomProfileField &&
				((CustomProfileField) other).id == this.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

}
