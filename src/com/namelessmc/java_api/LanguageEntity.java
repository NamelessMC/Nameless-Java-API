package com.namelessmc.java_api;

import com.namelessmc.java_api.exception.NamelessException;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Locale;

public interface LanguageEntity {

	String rawLocale() throws NamelessException;

	default @NonNull Locale locale() throws NamelessException {
		final String language = this.rawLocale();
		final String[] langSplit = language.split("_");
		if (langSplit.length != 2) {
			throw new IllegalArgumentException("Invalid language: " + language);
		}
		return new Locale(langSplit[0], langSplit[1]);
	}

}
