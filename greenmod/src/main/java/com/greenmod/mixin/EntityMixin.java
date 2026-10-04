package com.greenmod.mixin;

import com.greenmod.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

	/** Low fire: you never see yourself burning. */
	@Inject(method = "isOnFire", at = @At("HEAD"), cancellable = true, require = 0)
	private void greenmod$noFire(CallbackInfoReturnable<Boolean> cir) {
		if (Modules.NO_FIRE.enabled && (Object) this == Minecraft.getInstance().player) {
			cir.setReturnValue(false);
		}
	}
}
