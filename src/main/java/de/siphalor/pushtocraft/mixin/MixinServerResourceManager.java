package de.siphalor.pushtocraft.mixin;

import de.siphalor.pushtocraft.PushToCraftManager;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceReloadListener;
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
		List<ResourceReloadListener> listeners = ((ReloadableResourceManagerImplAccessor) resourceManager).getListeners();
		List<ResourceReloadListener> initialListeners = ((ReloadableResourceManagerImplAccessor) resourceManager).getInitialListeners();
		PushToCraftManager pushToCraftManager = new PushToCraftManager();
		boolean awaitL = true, awaitIL = true;
		for (int i = 0, l = Math.min(listeners.size(), initialListeners.size()); i < l; i++) {
			if (awaitL && listeners.get(i) == recipeManager) {
				listeners.add(i, pushToCraftManager);
				awaitL = false;
				if (!awaitIL) {
					break;
				}
			}
			if (awaitIL && initialListeners.get(i) == recipeManager) {
				initialListeners.add(i, pushToCraftManager);
				awaitIL = false;
				if (!awaitL) {
					break;
				}
			}
		}
	}
}
