package com.greenmod.mixin;

import com.greenmod.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

	/** Hide the vanilla nametag in first person, our own bigger/glowing one is drawn instead. */
	@Inject(method = "extractRenderState", at = @At("RETURN"), require = 0)
	private void greenmod$hideVanillaName(Entity entity, EntityRenderState state, float partialTick, CallbackInfo ci) {
		if (!Modules.NAMETAGS.enabled) return;
		if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;
		if (entity instanceof Player || entity.hasCustomName()) {
			state.nameTag = null;
		}
	}
}
