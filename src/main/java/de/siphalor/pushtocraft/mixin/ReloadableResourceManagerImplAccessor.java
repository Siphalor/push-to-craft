package de.siphalor.pushtocraft.mixin;

import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ReloadableResourceManagerImpl.class)
public interface ReloadableResourceManagerImplAccessor {
	@Accessor
	List<ResourceReloadListener> getListeners();
	@Accessor
	List<ResourceReloadListener> getInitialListeners();
}
