package com.greenmod.mixin;

import com.greenmod.Modules;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

	/** Makes both hands and held items smaller (keeps them at the same screen position). */
	@ModifyVariable(method = "renderHandsWithItems", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
	private PoseStack greenmod$smallHands(PoseStack stack) {
		if (Modules.SMALL_HANDS.enabled) {
			float s = (float) Modules.HAND_SCALE.value;
			float k = 1.0f - s;
			stack.translate(0.0f, -0.5f * k, -0.7f * k);
			stack.scale(s, s, s);
		}
		return stack;
	}
}
