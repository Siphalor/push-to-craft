package de.siphalor.pushtocraft.mixin;

import com.google.gson.JsonObject;
import de.siphalor.pushtocraft.PushToCraft;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RecipeManager.class)
public class MixinRecipeManager {
	@Inject(
			method = "deserialize",
			at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/JsonHelper;getString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;"),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void onDeserialize(Identifier recipeId, JsonObject json, CallbackInfoReturnable<Recipe<?>> callbackInfoReturnable, String serializerId) {
		PushToCraft.currentRecipeSerializer = Registry.RECIPE_SERIALIZER.get(new Identifier(serializerId));
		PushToCraft.currentRecipeId = recipeId;
	}
}
