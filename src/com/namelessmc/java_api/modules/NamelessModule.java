package com.namelessmc.java_api.modules;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NamelessModule {

	public static final NamelessModule CORE = new NamelessModule("Core", true, null);
	public static final NamelessModule FORUM = new NamelessModule("Forum", true, null);
	public static final NamelessModule DISCORD_INTEGRATION = new NamelessModule("Discord Integration", true, null);
	public static final NamelessModule COOKIE_CONSENT = new NamelessModule("Cookie Consent", true, null);

	public static final NamelessModule STORE = new NamelessModule("Store", false, "https://namelessmc.com/resources/resource/139");
	public static final NamelessModule WEBSEND = new NamelessModule("Websend", false, "https://github.com/supercrafter100/Nameless-Websend/archive/refs/heads/master.zip");
	public static final NamelessModule SUGGESTIONS = new NamelessModule("Suggestions", false, "https://namelessmc.com/resources/resource/129");

	private final String name;
	private final boolean included;
	private final @Nullable String downloadLink;

	private NamelessModule(String name, boolean included, @Nullable String downloadLink) {
		this.name = name;
		this.included = included;
		this.downloadLink = downloadLink;
	}

	public String name() {
		return this.name;
	}

	public boolean isIncluded() {
		return this.included;
	}

	public @Nullable String downloadLink() {
		return downloadLink;
	}

	private static final List<NamelessModule> MODULES = List.of(
			CORE,
			FORUM,
			DISCORD_INTEGRATION,
			COOKIE_CONSENT,
			STORE,
			WEBSEND,
			SUGGESTIONS
	);

	private static final Map<String, NamelessModule> BY_NAME = new HashMap<>();

	static {
		for (NamelessModule module : MODULES) {
			BY_NAME.put(module.name(), module);
		}
	}

	public static NamelessModule custom(String name) {
		return new NamelessModule(Objects.requireNonNull(name), false, null);
	}

	public static NamelessModule byName(String name) {
		if (BY_NAME.containsKey(name)) {
			return BY_NAME.get(name);
		} else {
			return custom(name);
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(final @Nullable Object obj) {
		return obj instanceof NamelessModule &&
				((NamelessModule) obj).name().equals(this.name);
	}

}
