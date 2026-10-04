package com.greenmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public class GreenModClient implements ClientModInitializer {
	public static final String MOD_ID = "greenmod";

	private static boolean prevOpen;
	private static boolean prevSort;
	private static Double savedGamma;
	private static Integer savedFov;

	@Override
	public void onInitializeClient() {
		Config.load();

		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "overlay"), HitboxHud::render);

		HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (graphics, delta) -> {
			if (Modules.CROSSHAIR.enabled) {
				HitboxHud.crosshair(graphics);
			} else {
				original.extractRenderState(graphics, delta);
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(GreenModClient::tick);

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			restoreOptions(client);
			Config.save();
		});
	}

	private static void tick(Minecraft mc) {
		if (mc.player == null || mc.level == null) return;

		// open GUI with Right Shift
		boolean rs = Keys.down(GLFW.GLFW_KEY_RIGHT_SHIFT);
		if (rs && !prevOpen && mc.screen == null) mc.setScreen(new GreenGui());
		prevOpen = rs;

		// module toggle keys
		for (Module m : Modules.ALL) {
			if (m.key < 0) continue;
			boolean d = Keys.down(m.key);
			if (d && !m.lastDown && mc.screen == null) m.toggle();
			m.lastDown = d;
		}

		// inventory sort key (only in container screens)
		boolean sd = Keys.down(Modules.SORT_KEY.key);
		if (Modules.SORT.enabled && sd && !prevSort && mc.screen instanceof AbstractContainerScreen<?>) {
			if (!Modules.SORT_CTRL.value || Keys.ctrl()) InventorySorter.sort();
		}
		prevSort = sd;

		if (Modules.REFILL.enabled) InventorySorter.refillTick(mc);

		// no view bobbing
		if (Modules.NO_BOB.enabled && mc.options.bobView().get()) {
			mc.options.bobView().set(false);
		}

		fullbright(mc);
		zoom(mc);

		// auto sprint
		if (Modules.SPRINT.enabled && mc.screen == null && mc.options.keyUp.isDown()
				&& !mc.player.isShiftKeyDown() && !mc.player.isUsingItem()) {
			mc.options.keySprint.setDown(true);
		}
	}

	private static void fullbright(Minecraft mc) {
		OptionInstance<Double> opt = mc.options.gamma();
		if (Modules.FULLBRIGHT.enabled) {
			if (savedGamma == null) savedGamma = Math.min(1.0, opt.get());
			if (opt.get() < 15.0) forceGamma(opt, 16.0);
		} else if (savedGamma != null) {
			opt.set(savedGamma);
			savedGamma = null;
		}
	}

	private static void forceGamma(OptionInstance<Double> opt, double v) {
		try {
			Field f = OptionInstance.class.getDeclaredField("value");
			f.setAccessible(true);
			f.set(opt, v);
		} catch (Throwable t) {
			opt.set(1.0);
		}
	}

	private static void zoom(Minecraft mc) {
		boolean want = Modules.ZOOM.enabled && mc.screen == null && Keys.down(Modules.ZOOM_KEY.key);
		if (want) {
			if (savedFov == null) savedFov = mc.options.fov().get();
			int target = (int) Math.round(Modules.ZOOM_FOV.value);
			if (mc.options.fov().get() != target) mc.options.fov().set(target);
		} else if (savedFov != null) {
			mc.options.fov().set(savedFov);
			savedFov = null;
		}
	}

	private static void restoreOptions(Minecraft mc) {
		if (savedGamma != null) {
			mc.options.gamma().set(savedGamma);
			savedGamma = null;
		}
		if (savedFov != null) {
			mc.options.fov().set(savedFov);
			savedFov = null;
		}
	}
}
