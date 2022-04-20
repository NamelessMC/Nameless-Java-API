package com.namelessmc.java_api;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public interface LanguageEntity {

	@NotNull String getRawLocale() throws NamelessException;

	default @NotNull Locale getLocale() throws NamelessException {
		final String language = this.getRawLocale();
		final String[] langSplit = language.split("_");
		if (langSplit.length != 2) {
			throw new IllegalArgumentException("Invalid language: " + language);
		}
		return new Locale(langSplit[0], langSplit[1]);
	}

}
