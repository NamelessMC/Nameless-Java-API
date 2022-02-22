package com.namelessmc.java_api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomProfileFieldValue {

	private final @NotNull CustomProfileField field;
	private final @Nullable String value;

	CustomProfileFieldValue(@NotNull CustomProfileField field, @Nullable String value) {
		this.field = field;
		this.value = value;
	}

	public @NotNull CustomProfileField getField() {
		return this.field;
	}

	public @Nullable String getValue() {
		return value;
	}

}
