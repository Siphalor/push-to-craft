package de.siphalor.pushtocraft.mixin;

import de.siphalor.pushtocraft.PushToCraftManager;
import net.minecraft.server.DataPackContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataPackContents.class)
public class MixinDataPackContents {
	@SuppressWarnings({"FieldCanBeLocal", "unused"})
    private PushToCraftManager pushToCraftManager;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void onCreate(CallbackInfo ci) {
		pushToCraftManager = new PushToCraftManager();
	}
}
