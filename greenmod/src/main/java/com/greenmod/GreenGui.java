package com.greenmod;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/** Click GUI. Left click = toggle, right click = settings/bind/colour. Input is polled via GLFW. */
public class GreenGui extends Screen {
	private static final int W = 128;
	private static final int ROW = 14;
	private static final int[] PALETTE = {
			0x39FF14, 0xFF3B30, 0xFF9500, 0xFFD60A, 0x30D158, 0x00E5FF,
			0x0A84FF, 0xBF5AF2, 0xFF2D95, 0xFFFFFF, 0xAAAAAA, 0x000000
	};

	private boolean prevL, prevR;
	private final boolean[] prevKey = new boolean[349];

	private boolean cL, cR, cLDown;
	private int cmx, cmy;

	private Module expanded;
	private Object listening;
	private Setting.Num dragNum;
	private Setting.Color dragHue;

	public GreenGui() {
		super(Component.literal("Green Client"));
		prevL = Keys.mouse(0);
		prevR = Keys.mouse(1);
		for (int k : Keys.VALID) prevKey[k] = Keys.down(k);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void onClose() {
		Config.save();
		super.onClose();
	}

	private boolean hover(int x, int y, int w, int h) {
		return cmx >= x && cmx < x + w && cmy >= y && cmy < y + h;
	}

	private void text(GuiGraphicsExtractor g, String s, int x, int y, int rgb) {
		g.text(this.font, s, x, y, 0xFF000000 | rgb);
	}

	private static int hsv(float hue) {
		float h6 = hue * 6f;
		int i = (int) h6;
		float f = h6 - i;
		float r, gg, b;
		switch (i % 6) {
			case 0: r = 1; gg = f; b = 0; break;
			case 1: r = 1 - f; gg = 1; b = 0; break;
			case 2: r = 0; gg = 1; b = f; break;
			case 3: r = 0; gg = 1 - f; b = 1; break;
			case 4: r = f; gg = 0; b = 1; break;
			default: r = 1; gg = 0; b = 1 - f; break;
		}
		return ((int) (r * 255) << 16) | ((int) (gg * 255) << 8) | (int) (b * 255);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		boolean l = Keys.mouse(0);
		boolean r = Keys.mouse(1);
		cL = l && !prevL;
		cR = r && !prevR;
		cLDown = l;
		prevL = l;
		prevR = r;
		cmx = mouseX;
		cmy = mouseY;
		if (!l) {
			dragNum = null;
			dragHue = null;
		}

		int pressed = -1;
		for (int k : Keys.VALID) {
			boolean d = Keys.down(k);
			if (d && !prevKey[k] && pressed == -1) pressed = k;
			prevKey[k] = d;
		}
		if (pressed != -1) {
			if (listening != null) {
				int bound = pressed == GLFW.GLFW_KEY_ESCAPE ? -1 : pressed;
				if (listening instanceof Module m) m.key = bound;
				else if (listening instanceof Setting.Key ks) ks.key = bound;
				listening = null;
			} else if (pressed == GLFW.GLFW_KEY_ESCAPE || pressed == GLFW.GLFW_KEY_RIGHT_SHIFT) {
				onClose();
				return;
			}
		}

		g.fill(0, 0, this.width, this.height, 0x88000000);

		for (int ci = 0; ci < Modules.CATEGORIES.size(); ci++) {
			String cat = Modules.CATEGORIES.get(ci);
			int x = 14 + ci * (W + 8);
			int y = 14;
			g.fill(x, y, x + W, y + 16, 0xF0101010);
			g.fill(x, y + 15, x + W, y + 16, GreenMod.NEON_ARGB);
			text(g, cat, x + 4, y + 4, 0x39FF14);
			y += 16;
			for (Module m : Modules.ALL) {
				if (cat.equals(m.category)) y = drawModule(g, m, x, y);
			}
		}

		String hint = "Linksklick: an/aus   Rechtsklick: Einstellungen, Taste, Farbe   R-Shift/Esc: schlie\u00dfen";
		text(g, hint, (this.width - this.font.width(hint)) / 2, this.height - 12, 0xCCCCCC);
	}

	private int drawModule(GuiGraphicsExtractor g, Module m, int x, int y) {
		boolean h = hover(x, y, W, ROW);
		int bg = m.enabled ? (h ? 0xE0379A3A : 0xE02E7D32) : (h ? 0xE0333333 : 0xE0202020);
		g.fill(x, y, x + W, y + ROW, bg);
		text(g, m.name, x + 4, y + 3, m.enabled ? 0xFFFFFF : 0xBBBBBB);
		if (m.key >= 0) {
			String k = Keys.name(m.key);
			text(g, k, x + W - 4 - this.font.width(k), y + 3, 0xAAFFAA);
		}
		if (h && listening == null) {
			if (cL) m.toggle();
			if (cR) expanded = expanded == m ? null : m;
		}
		y += ROW;

		if (expanded != m) return y;

		// bind row
		boolean bh = hover(x, y, W, ROW);
		g.fill(x, y, x + W, y + ROW, bh ? 0xE0222222 : 0xE0141414);
		text(g, listening == m ? "Taste: dr\u00fccke eine Taste..." : "Taste (an/aus): " + Keys.name(m.key), x + 4, y + 3, 0xFFFFFF);
		if (bh && cL && listening == null) listening = m;
		y += ROW;

		for (Setting s : m.settings) {
			if (s instanceof Setting.Bool b) {
				boolean sh = hover(x, y, W, ROW);
				g.fill(x, y, x + W, y + ROW, sh ? 0xE0222222 : 0xE0141414);
				text(g, b.name + ": " + (b.value ? "AN" : "AUS"), x + 4, y + 3, b.value ? 0x39FF14 : 0xAAAAAA);
				if (sh && cL && listening == null) b.value = !b.value;
				y += ROW;
			} else if (s instanceof Setting.Num n) {
				boolean sh = hover(x, y, W, ROW);
				double frac = (n.value - n.min) / (n.max - n.min);
				g.fill(x, y, x + W, y + ROW, 0xE0141414);
				g.fill(x, y, x + (int) (W * frac), y + ROW, 0xE0206020);
				String val = n.step >= 1 ? String.valueOf((int) Math.round(n.value)) : String.format(Locale.ROOT, "%.2f", n.value);
				text(g, n.name + ": " + val, x + 4, y + 3, 0xFFFFFF);
				if (sh && cL && listening == null) dragNum = n;
				if (dragNum == n && cLDown) {
					double f = Math.max(0.0, Math.min(1.0, (cmx - x) / (double) W));
					double v = n.min + f * (n.max - n.min);
					v = Math.round(v / n.step) * n.step;
					n.value = Math.max(n.min, Math.min(n.max, v));
				}
				y += ROW;
			} else if (s instanceof Setting.Key k) {
				boolean sh = hover(x, y, W, ROW);
				g.fill(x, y, x + W, y + ROW, sh ? 0xE0222222 : 0xE0141414);
				text(g, listening == k ? k.name + ": dr\u00fccke Taste..." : k.name + ": " + Keys.name(k.key), x + 4, y + 3, 0xFFFFFF);
				if (sh && cL && listening == null) listening = k;
				y += ROW;
			} else if (s instanceof Setting.Color c) {
				y = drawColor(g, c, x, y);
			}
		}
		g.fill(x, y, x + W, y + 2, GreenMod.NEON_ARGB);
		return y + 2;
	}

	private int drawColor(GuiGraphicsExtractor g, Setting.Color c, int x, int y) {
		// label + preview
		g.fill(x, y, x + W, y + ROW, 0xE0141414);
		text(g, c.name, x + 4, y + 3, 0xFFFFFF);
		g.fill(x + W - 20, y + 2, x + W - 4, y + ROW - 2, 0xFF000000 | c.rgb);
		y += ROW;

		// palette
		g.fill(x, y, x + W, y + ROW, 0xE0141414);
		for (int i = 0; i < PALETTE.length; i++) {
			int sx = x + 4 + i * 10;
			g.fill(sx, y + 2, sx + 9, y + ROW - 2, 0xFF000000 | PALETTE[i]);
			if (hover(sx, y + 2, 9, ROW - 4) && cL && listening == null) c.rgb = PALETTE[i];
		}
		y += ROW;

		// hue strip
		g.fill(x, y, x + W, y + ROW, 0xE0141414);
		int sx0 = x + 4;
		int sw = W - 8;
		for (int i = 0; i < sw; i += 2) {
			g.fill(sx0 + i, y + 2, sx0 + i + 2, y + ROW - 2, 0xFF000000 | hsv(i / (float) sw));
		}
		if (hover(sx0, y, sw, ROW) && cL && listening == null) dragHue = c;
		if (dragHue == c && cLDown) {
			float hue = Math.max(0f, Math.min(0.999f, (cmx - sx0) / (float) sw));
			c.rgb = hsv(hue);
		}
		return y + ROW;
	}
}
