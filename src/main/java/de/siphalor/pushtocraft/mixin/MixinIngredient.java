package de.siphalor.pushtocraft.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.siphalor.pushtocraft.PushToCraftManager;
import de.siphalor.pushtocraft.Util;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;

@Mixin(value = Ingredient.class, priority = 2000)
public class MixinIngredient {
	@Shadow private IntList ids;

	@ModifyVariable(method = "fromJson", at = @At("HEAD"), argsOnly = true)
	private static JsonElement onFromJson(JsonElement jsonElement) {
		if (jsonElement == null)
			return null;
		if (jsonElement.isJsonObject()) {
			String id = extractId(jsonElement.getAsJsonObject());

			JsonArray jsonArray = new JsonArray();
			jsonArray.add(jsonElement);
			JsonObject baseClone = Util.shallowCopy(jsonElement.getAsJsonObject());
			baseClone.remove("tag");
			baseClone.remove("item");
			processEntries(Collections.singleton(id), jsonArray, s -> Util.shallowCopy(baseClone));
			return jsonArray;
		} else if (jsonElement.isJsonArray()) {
			HashMap<String, JsonObject> ids = new HashMap<>();
			for (JsonElement ele : jsonElement.getAsJsonArray()) {
				if (ele.isJsonObject()) {
					JsonObject object = jsonElement.getAsJsonObject();
					ids.put(extractId(object), object);
				}
			}
			processEntries(ids.keySet(), jsonElement.getAsJsonArray(), id -> {
				JsonObject copy = Util.shallowCopy(ids.get(id));
				copy.remove("item");
				copy.remove("tag");
				return copy;
			});
			return jsonElement;
		}
		return jsonElement;
	}

	@Unique
	private static String extractId(JsonObject obj) {
		if (obj.has("item")) {
			return JsonHelper.getString(obj, "item", "");
		} else {
			return JsonHelper.getString(obj, "tag", "");
		}
	}

	@Unique
	private static void processEntries(Set<String> ingredientEntries, JsonArray jsonArray, Function<String, JsonObject> jsonObjectSupplier) {
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
								jsonArray.add(obj);
							}
							break;
						}
					}
				});
	}
}
