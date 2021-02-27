package de.siphalor.pushtocraft;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;

import java.util.Map;

public class Util {
	public static Identifier currentRecipeId;
	public static RecipeSerializer<?> currentRecipeSerializer;

	public static JsonObject shallowCopy(JsonObject jsonObject) {
		JsonObject copy = new JsonObject();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			copy.add(entry.getKey(), entry.getValue());
		}
		return copy;
	}

	public static boolean isString(JsonElement jsonElement) {
		return jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString();
	}
}
