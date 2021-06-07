package de.siphalor.pushtocraft.mixin;

import de.siphalor.pushtocraft.PushToCraftManager;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerResourceManager.class)
public class MixinServerResourceManager {
	@Shadow @Final private RecipeManager recipeManager;

	@Shadow @Final private ReloadableResourceManager resourceManager;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onConstructed(CallbackInfo callbackInfo) {
		List<ResourceReloader> listeners = ((ReloadableResourceManagerImplAccessor) resourceManager).getReloaders();
		PushToCraftManager pushToCraftManager = new PushToCraftManager();

		for (int i = 0; i < listeners.size(); i++) {
			if (listeners.get(i) == recipeManager) {
				listeners.add(i, pushToCraftManager);
				return;
			}
		}
	}
}
