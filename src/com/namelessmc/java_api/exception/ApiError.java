package com.namelessmc.java_api.exception;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum ApiError {

	// https://github.com/NamelessMC/Nameless/blob/v2/modules/Core/classes/Misc/Nameless2API.php
	NAMELESS_API_IS_DISABLED("nameless", "api_is_disabled"),
	NAMELESS_UNKNOWN_ERROR("nameless", "unknown_error"),
	NAMELESS_NOT_AUTHORIZED("nameless", "not_authorized"),
	NAMELESS_INVALID_API_KEY("nameless", "invalid_api_key"),
	NAMELESS_INVALID_API_METHOD("nameless", "invalid_api_method"),
	NAMELESS_CANNOT_FIND_USER("nameless", "cannot_find_user"),
	NAMELESS_INVALID_POST_CONTENTS("nameless", "invalid_post_contents"),
	NAMELESS_INVALID_GET_CONTENTS("nameless", "invalid_get_contents"),
	NAMELESS_NO_SITE_UID("nameless", "no_site_uid"),

	// https://github.com/NamelessMC/Nameless/blob/v2/modules/Core/classes/Misc/CoreApiErrors.php
	CORE_UNABLE_TO_FIND_GROUP("core", "unable_to_find_group"),
	CORE_BANNED_FROM_WEBSITE("core", "banned_from_website"),
	CORE_REPORT_CONTENT_TOO_LONG("core", "report_content_too_long"),
	CORE_CANNOT_REPORT_YOURSELF("core", "cannot_report_yourself"),
	CORE_OPEN_REPORT_ALREADY("core", "open_report_already"),
	CORE_UNABLE_TO_UPDATE_SERVER_INFO("core", "unable_to_update_server_info"),
	CORE_INVALID_SERVER_ID("core", "invalid_server_id"),
	CORE_EMAIL_ALREADY_EXISTS("core", "email_already_exists"),
	CORE_USERNAME_ALREADY_EXISTS("core", "username_already_exists"),
	CORE_INVALID_EMAIL_ADDRESS("core", "invalid_email_address"),
	CORE_INVALID_USERNAME("core", "invalid_username"),
	CORE_UNABLE_TO_CREATE_ACCOUNT("core", "unable_to_create_account"),
	CORE_UNABLE_TO_SEND_REGISTRATION_EMAIL("core", "unable_to_send_registration_email"),
	CORE_INVALID_INTEGRATION("core", "invalid_integration"),
	CORE_INVALID_CODE("core", "invalid_code"),
	CORE_USER_ALREADY_ACTIVE("core", "user_already_active"),
	CORE_UNABLE_TO_UPDATE_USERNAME("core", "unable_to_update_username"),

	// https://github.com/NamelessMC/Nameless/blob/v2/modules/Discord%20Integration/classes/DiscordApiErrors.php
	DISCORD_DISCORD_INTEGRATION_DISABLED("discord_integration", "discord_integration_disabled"),
	DISCORD_UNABLE_TO_UPDATE_DISCORD_ROLES("discord_integration", "unable_to_update_discord_roles"),
	DISCORD_UNABLE_TO_SET_DISCORD_BOT_URL("discord_integration", "unable_to_set_discord_bot_url"),
	DISCORD_UNABLE_TO_SET_DISCORD_GUILD_ID("discord_integration", "unable_to_set_discord_guild_id"),
	DISCORD_UNABLE_TO_SET_DISCORD_BOT_USERNAME("discord_integration", "unable_to_set_discord_bot_username"),

	;

	private final String key;
	private final String value;
	private final String string;

	ApiError(final String namespaceKey, final String namespaceValue) {
		this.key = namespaceKey;
		this.value = namespaceValue;
		this.string = this.key + ":" + this.value;
	}

	public String key() {
		return this.key;
	}

	public String value() {
		return this.value;
	}

	@Override
	public String toString() {
		return this.string;
	}

	private static final Map<String, ApiError> FROM_STRING = new HashMap<>();

	static {
		for (ApiError apiError : ApiError.values()) {
			FROM_STRING.put(apiError.string, apiError);
		}
	}

	public static @Nullable ApiError fromString(final String string) {
		return FROM_STRING.get(string);
	}

}
