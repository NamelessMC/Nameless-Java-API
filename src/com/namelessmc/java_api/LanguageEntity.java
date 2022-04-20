package com.namelessmc.java_api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public interface LanguageEntity {

	@NotNull String getRawLanguage() throws NamelessException;

	default @NotNull Locale getLocale() throws NamelessException {
		final String language = this.getRawLanguage();
		final String[] langSplit = language.split("_");
		if (langSplit.length != 2) {
			throw new IllegalArgumentException("Invalid language: " + language);
		}
		return new Locale(langSplit[0], langSplit[1]);
	}

}
