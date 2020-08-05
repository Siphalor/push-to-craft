package de.siphalor.pushtocraft.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.siphalor.pushtocraft.PushToCraft;
import de.siphalor.pushtocraft.PushToCraftManager;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.JsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Mixin(value = Ingredient.class, priority = 2000)
public class MixinIngredient {
	@ModifyVariable(method = "fromJson", at = @At("HEAD"), argsOnly = true)
	private static JsonElement onFromJson(JsonElement jsonElement) {
		if (jsonElement == null)
			return null;
		if (jsonElement.isJsonObject()) {
			String id = extractId(jsonElement.getAsJsonObject());

			JsonArray jsonArray = new JsonArray();
			jsonArray.add(jsonElement);
			processEntries(Collections.singleton(id), jsonArray);
			return jsonArray;
		} else if (jsonElement.isJsonArray()) {
			Set<String> ids = new HashSet<>();
			jsonElement.getAsJsonArray().forEach(ele -> {
				if (ele.isJsonObject())
					ids.add(extractId(ele.getAsJsonObject()));
			});
			processEntries(ids, jsonElement.getAsJsonArray());
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
	private static void processEntries(Set<String> ingredientEntries, JsonArray jsonArray) {
		PushToCraftManager.getInstance().getMatches(PushToCraft.currentRecipeId, PushToCraft.currentRecipeSerializer)
				.forEach(entry -> {
					if (entry.getTargets().stream().anyMatch(ingredientEntries::contains)) {
						entry.getAdditions().forEach(addition -> {
							JsonObject obj = new JsonObject();
							if (addition.charAt(0) == '#') {
								obj.addProperty("tag", addition.substring(1));
							} else {
								obj.addProperty("item", addition);
							}
							jsonArray.add(obj);
						});
					}
				});
	}
}
