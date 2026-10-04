package com.greenmod;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** 2D hitboxes, custom nametags and the info overlay. */
public final class HitboxHud {
	private static final int[] RECT = new int[4];

	private HitboxHud() {}

	public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer me = mc.player;
		ClientLevel level = mc.level;
		if (me == null || level == null) return;

		boolean boxes = Modules.PLAYERS.module.enabled || Modules.HOSTILE.module.enabled
				|| Modules.PASSIVE.module.enabled || Modules.ITEMS.module.enabled;
		boolean names = Modules.NAMETAGS.enabled;

		if ((boxes || names) && Projector.begin(delta)) {
			List<Entity> list = level.getEntities(me, me.getBoundingBox().inflate(96.0), e -> true);
			for (Entity e : list) {
				if (e == me) continue;
				if (boxes) box(g, me, e);
				if (names) nametag(g, mc, me, e);
			}
		}

		if (Modules.INFO.enabled) info(g, mc, me);
	}

	// ---------------- hitboxes ----------------
	private static void box(GuiGraphicsExtractor g, LocalPlayer me, Entity e) {
		Modules.Hit hit = null;
		if (e instanceof ItemEntity) hit = Modules.ITEMS;
		else if (e instanceof Player p) {
			if (p.isSpectator()) return;
			hit = Modules.PLAYERS;
		} else if (e instanceof Enemy) hit = Modules.HOSTILE;
		else if (e instanceof LivingEntity) hit = Modules.PASSIVE;
		if (hit == null || !hit.module.enabled) return;

		double range = hit.range.value;
		if (me.distanceToSqr(e) > range * range) return;
		if (hit.sight.value && !me.hasLineOfSight(e)) return;

		float pt = Projector.pt;
		double ox = Mth.lerp(pt, e.xOld, e.getX()) - e.getX();
		double oy = Mth.lerp(pt, e.yOld, e.getY()) - e.getY();
		double oz = Mth.lerp(pt, e.zOld, e.getZ()) - e.getZ();
		if (!Projector.rect(e.getBoundingBox(), ox, oy, oz, RECT)) return;

		int c = 0xFF000000 | hit.color.rgb;
		int x1 = RECT[0], y1 = RECT[1], x2 = RECT[2], y2 = RECT[3];
		g.fill(x1, y1, x2, y1 + 1, c);
		g.fill(x1, y2 - 1, x2, y2, c);
		g.fill(x1, y1, x1 + 1, y2, c);
		g.fill(x2 - 1, y1, x2, y2, c);
	}

	// ---------------- nametags ----------------
	private static void nametag(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer me, Entity e) {
		boolean isPlayer = e instanceof Player;
		if (!isPlayer && !(e instanceof LivingEntity && e.hasCustomName())) return;
		if (e.isSpectator()) return;
		double dist = Math.sqrt(me.distanceToSqr(e));
		if (dist > 64.0) return;

		float pt = Projector.pt;
		double x = Mth.lerp(pt, e.xOld, e.getX());
		double y = Mth.lerp(pt, e.yOld, e.getY());
		double z = Mth.lerp(pt, e.zOld, e.getZ());
		if (!Projector.project(x, y + e.getBbHeight() + 0.5, z)) return;

		String name = isPlayer ? e.getName().getString() : e.getCustomName().getString();
		if (Modules.NAME_HEALTH.value && e instanceof LivingEntity le) {
			name = name + " [" + (int) Math.ceil(le.getHealth()) + "]";
		}

		boolean badge = Modules.NAME_KELP.value && isPlayer && GreenMod.isModUser(e);
		int textW = mc.font.width(name);
		int totalW = textW + (badge ? 11 : 0);

		float scale = (float) (Modules.NAME_SIZE.value * Mth.clamp(10.0 / Math.max(dist, 1.0), 0.6, 1.6));
		int rgb = Modules.NAME_COLOR.rgb;

		var pose = g.pose();
		pose.pushMatrix();
		pose.translate((float) Projector.sx, (float) Projector.sy);
		pose.scale(scale, scale);

		int left = -totalW / 2;
		g.fill(left - 2, -10, left + totalW + 2, 1, 0x80000000);

		if (badge) kelpBlock(g, left, -9);

		int tx = left + (badge ? 11 : 0);
		if (Modules.NAME_GLOW.value) {
			int glow = 0x66000000 | rgb;
			g.text(mc.font, name, tx - 1, -9, glow);
			g.text(mc.font, name, tx + 1, -9, glow);
			g.text(mc.font, name, tx, -10, glow);
			g.text(mc.font, name, tx, -8, glow);
		}
		g.text(mc.font, name, tx, -9, 0xFF000000 | rgb);

		pose.popMatrix();
	}

	/** 8x8 dried-kelp-block lookalike. */
	private static void kelpBlock(GuiGraphicsExtractor g, int x, int y) {
		g.fill(x, y, x + 8, y + 8, 0xFF2F4A24);
		g.fill(x, y, x + 8, y + 1, 0xFF5E8A45);
		g.fill(x + 1, y + 1, x + 2, y + 8, 0xFF4A7A36);
		g.fill(x + 4, y + 1, x + 5, y + 8, 0xFF4A7A36);
		g.fill(x + 6, y + 1, x + 7, y + 8, 0xFF3C6A2C);
	}

	// ---------------- info overlay ----------------
	private static void info(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer me) {
		int c = 0xFF000000 | Modules.INFO_COLOR.rgb;
		int y = 4;
		if (Modules.INFO_COORDS.value) {
			g.text(mc.font, "XYZ: " + Mth.floor(me.getX()) + " " + Mth.floor(me.getY()) + " " + Mth.floor(me.getZ()), 4, y, c);
			y += 10;
		}
		if (Modules.INFO_DIR.value) {
			g.text(mc.font, "Richtung: " + me.getDirection().getName(), 4, y, c);
			y += 10;
		}
		if (Modules.INFO_FPS.value) {
			g.text(mc.font, "FPS: " + mc.getFps(), 4, y, c);
		}
	}

	// ---------------- crosshair ----------------
	public static void crosshair(GuiGraphicsExtractor g) {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.options.getCameraType().isFirstPerson()) return;
		int cx = mc.getWindow().getGuiScaledWidth() / 2;
		int cy = mc.getWindow().getGuiScaledHeight() / 2;
		int c = 0xFF000000 | Modules.CROSS_COLOR.rgb;
		int gap = (int) Modules.CROSS_GAP.value;
		int len = (int) Modules.CROSS_LEN.value;
		int t = (int) Modules.CROSS_THICK.value;
		int lo = t / 2;
		g.fill(cx - gap - len, cy - lo, cx - gap, cy - lo + t, c);
		g.fill(cx + gap + 1, cy - lo, cx + gap + 1 + len, cy - lo + t, c);
		g.fill(cx - lo, cy - gap - len, cx - lo + t, cy - gap, c);
		g.fill(cx - lo, cy + gap + 1, cx - lo + t, cy + gap + 1 + len, c);
		if (Modules.CROSS_DOT.value) g.fill(cx - lo, cy - lo, cx - lo + t, cy - lo + t, c);
	}
}
