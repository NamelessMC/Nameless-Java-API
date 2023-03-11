package com.namelessmc.java_api.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.function.Function;

public class GsonHelper {

	public static int[] toIntArray(JsonArray jsonArray) {
		int[] array = new int[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			array[i] = jsonArray.get(i).getAsInt();
		}
		return array;
	}

	public static long[] toLongArray(JsonArray jsonArray) {
		long[] array = new long[jsonArray.size()];
		for (int i = 0; i < jsonArray.size(); i++) {
			array[i] = jsonArray.get(i).getAsLong();
		}
		return array;
	}

	public static Set<Integer> toIntegerSet(JsonArray jsonArray) {
		Set<Integer> set = new HashSet<>();
		for (JsonElement elem : jsonArray) {
			set.add(elem.getAsInt());
		}
		return Collections.unmodifiableSet(set);
	}

	public static <T> List<T> toObjectList(JsonArray array, Function<JsonObject, T> constructor) {
		List<T> list = new ArrayList<>(array.size());
		for (JsonElement e : array) {
			list.add(constructor.apply(e.getAsJsonObject()));
		}
		return Collections.unmodifiableList(list);
	}

	public static @Nullable String getNullableString(JsonObject object, String key) {
		if (object.has(key)) {
			JsonElement e = object.get(key);
			if (!e.isJsonNull()) {
				return e.getAsString();
			}
		}
		return null;
	}

}
