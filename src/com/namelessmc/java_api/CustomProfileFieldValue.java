package com.namelessmc.java_api;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CustomProfileFieldValue {

	private final @NonNull CustomProfileField field;
	private final @Nullable String value;

	CustomProfileFieldValue(@NonNull CustomProfileField field, @Nullable String value) {
		this.field = field;
		this.value = value;
	}

	public @NonNull CustomProfileField getField() {
		return this.field;
	}

	public @Nullable String getValue() {
		return value;
	}

}
