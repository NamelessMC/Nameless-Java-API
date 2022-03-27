package com.namelessmc.java_api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LanguageEntity {

	@NotNull String getLanguage() throws NamelessException;

	@Nullable String getLanguagePosix() throws NamelessException;

}
