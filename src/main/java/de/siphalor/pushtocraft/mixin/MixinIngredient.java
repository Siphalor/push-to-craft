package de.siphalor.pushtocraft.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.siphalor.pushtocraft.PushToCraft;
import de.siphalor.pushtocraft.PushToCraftManager;
import de.siphalor.pushtocraft.Util;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;

@Mixin(value = Ingredient.class, priority = 2000)
public class MixinIngredient {
	@Unique
	private static final String PROCESSED_ENTRY_TAG = PushToCraft.MOD_ID + ":processed";

	@ModifyVariable(method = "fromJson", at = @At("HEAD"), argsOnly = true)
	private static JsonElement onFromJson(JsonElement jsonElement) {
		if (jsonElement == null)
			return null;
		if (jsonElement.isJsonObject()) {
			JsonObject object = jsonElement.getAsJsonObject();
			// Skip special ingredient types provided by other ingredient extensions
			if (object.has("type")) {
				return object;
			}
			// Skip infinite recursion with mods that tamper with the vanilla loading
			if (object.has(PROCESSED_ENTRY_TAG)) {
				return object;
			}
			object.addProperty(PROCESSED_ENTRY_TAG, true);

			String id = extractId(object);

			// Get the pushed entries
			JsonArray jsonArray = processEntries(Collections.singleton(id), null, s -> {
				JsonObject copy = Util.shallowCopy(object);
				copy.remove("tag");
				copy.remove("item");
				return copy;
			});
			// If there are none, then keep it simple and return the base object
			if (jsonArray.size() == 0) {
				return object;
			}

			// Add the existing object back and return
			jsonArray.add(object);
			return jsonArray;
		} else if (jsonElement.isJsonArray()) {
			JsonArray jsonArray = jsonElement.getAsJsonArray();
			// Collect all entries and create a lookup map to the base objects
			HashMap<String, JsonObject> ids = new HashMap<>();
			for (JsonElement ele : jsonArray) {
				if (ele.isJsonObject()) {
					JsonObject object = ele.getAsJsonObject();

					// Skip special ingredient types provided by other ingredient extensions
					if (object.has("type")) {
						continue;
					}
					// Skip infinite recursion with mods that tamper with the vanilla loading
					if (object.has(PROCESSED_ENTRY_TAG)) {
						continue;
					}

					ids.put(extractId(object), object);
				}
			}
			// If no entry applies, keep it simple and return the base array
			if (ids.isEmpty()) {
				return jsonArray;
			}

			// Push new entries to the existing array and return
			return processEntries(ids.keySet(), jsonArray, id -> {
				JsonObject copy = Util.shallowCopy(ids.get(id));
				copy.remove("item");
				copy.remove("tag");
				copy.addProperty(PROCESSED_ENTRY_TAG, true);
				return copy;
			});
		}
		return jsonElement;
	}

	/**
	 * Extracts the item id/tag from an ingredient entry
	 * @param obj The ingredient entry object
	 * @return The id/tag (tags are prefixed with a hash)
	 */
	@Unique
	private static String extractId(JsonObject obj) {
		if (obj.has("item")) {
			return JsonHelper.getString(obj, "item", "");
		} else {
			return "#" + JsonHelper.getString(obj, "tag", "");
		}
	}

	/**
	 * Processes the given ingredient entries and pushes alternate entries to the given array.
	 * @param ingredientEntries The item ids/tags of the ingredient entries
	 * @param jsonArray The JSON array to add on to or `null` to create a new one.
	 * @param jsonObjectSupplier A function that yields a base entry for the given entry id.
	 *                           Existing `item` and `tag` fields should be removed.
	 *                           It should also mark the entry with {@link MixinIngredient#PROCESSED_ENTRY_TAG}.
	 * @return jsonArray, if given, expanded by the pushed entries or a fresh array of those.
	 */
	@Unique
	private static JsonArray processEntries(Set<String> ingredientEntries, JsonArray jsonArray, Function<String, JsonObject> jsonObjectSupplier) {
		if (jsonArray == null) {
			jsonArray = new JsonArray();
		}
		JsonArray finalArray = jsonArray;
		PushToCraftManager.getInstance().getMatches(Util.currentRecipeId, Util.currentRecipeSerializer)
				.forEach(entry -> {
					for (String id : entry.getTargets()) {
						if (ingredientEntries.contains(id)) {
							for (String addition : entry.getAdditions()) {
								JsonObject obj = jsonObjectSupplier.apply(id);
								if (addition.charAt(0) == '#') {
									obj.addProperty("tag", addition.substring(1));
								} else {
									obj.addProperty("item", addition);
								}
								finalArray.add(obj);
							}
							break;
						}
					}
				});

		return finalArray;
	}
}
