package com.greenmod;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/** All modules and their default settings. */
public final class Modules {
	public static final List<Module> ALL = new ArrayList<>();
	public static final String C_HIT = "Hitboxen";
	public static final String C_VIEW = "Anzeige";
	public static final String C_INV = "Inventar";
	public static final String C_MISC = "Spieler & HUD";
	public static final List<String> CATEGORIES = List.of(C_HIT, C_VIEW, C_INV, C_MISC);

	private static Module reg(Module m) {
		ALL.add(m);
		return m;
	}

	/** A hitbox module with colour, range and line-of-sight options. */
	public static final class Hit {
		public final Module module;
		public final Setting.Color color;
		public final Setting.Bool sight;
		public final Setting.Num range;

		Hit(String id, String name, boolean on, int rgb) {
			module = reg(new Module(id, name, C_HIT, on));
			color = module.color("Farbe", rgb);
			sight = module.bool("Nur bei Sichtlinie", true);
			range = module.num("Reichweite", 8, 96, 64, 1);
		}
	}

	public static final Hit PLAYERS = new Hit("hit_players", "Hitbox: Spieler", true, 0x39FF14);
	public static final Hit HOSTILE = new Hit("hit_hostile", "Hitbox: Monster", false, 0xFF3B30);
	public static final Hit PASSIVE = new Hit("hit_passive", "Hitbox: Tiere/NPCs", false, 0x4FC3FF);
	public static final Hit ITEMS = new Hit("hit_items", "Hitbox: Items", false, 0xFFB020);

	// ---- Anzeige ----
	public static final Module NAMETAGS = reg(new Module("nametags", "Nametags", C_VIEW, true));
	public static final Setting.Color NAME_COLOR = NAMETAGS.color("Farbe", 0x39FF14);
	public static final Setting.Num NAME_SIZE = NAMETAGS.num("Gr\u00f6\u00dfe", 0.8, 3.0, 1.5, 0.1);
	public static final Setting.Bool NAME_GLOW = NAMETAGS.bool("Leuchten", true);
	public static final Setting.Bool NAME_HEALTH = NAMETAGS.bool("Leben anzeigen", false);
	public static final Setting.Bool NAME_KELP = NAMETAGS.bool("Kelp bei Mod-Nutzern", true);
	public static final Setting.Bool NAME_ANNOUNCE = NAMETAGS.bool("Mich markieren (nach Rejoin)", true);

	public static final Module CROSSHAIR = reg(new Module("crosshair", "Eigenes Fadenkreuz", C_VIEW, true));
	public static final Setting.Color CROSS_COLOR = CROSSHAIR.color("Farbe", 0x39FF14);
	public static final Setting.Num CROSS_LEN = CROSSHAIR.num("L\u00e4nge", 2, 12, 4, 1);
	public static final Setting.Num CROSS_GAP = CROSSHAIR.num("L\u00fccke", 0, 8, 2, 1);
	public static final Setting.Num CROSS_THICK = CROSSHAIR.num("Dicke", 1, 3, 1, 1);
	public static final Setting.Bool CROSS_DOT = CROSSHAIR.bool("Mittelpunkt", false);

	public static final Module SMALL_HANDS = reg(new Module("small_hands", "Kleine H\u00e4nde/Items", C_VIEW, true));
	public static final Setting.Num HAND_SCALE = SMALL_HANDS.num("Gr\u00f6\u00dfe", 0.3, 1.0, 0.6, 0.05);

	public static final Module NO_FIRE = reg(new Module("no_fire", "Low Fire (kein Feuer-Overlay)", C_VIEW, true));
	public static final Module NO_BOB = reg(new Module("no_bob", "Kein View-Bobbing", C_VIEW, true));
	public static final Module FULLBRIGHT = reg(new Module("fullbright", "Fullbright", C_VIEW, false));

	// ---- Inventar ----
	public static final Module SORT = reg(new Module("inv_sort", "Inventar sortieren", C_INV, true));
	public static final Setting.Key SORT_KEY = SORT.keybind("Sortier-Taste", GLFW.GLFW_KEY_R);
	public static final Setting.Bool SORT_CTRL = SORT.bool("Strg dabei halten", true);
	public static final Setting.Bool SORT_MERGE = SORT.bool("Stacks zusammenf\u00fchren", true);
	public static final Setting.Bool SORT_CHEST = SORT.bool("Kisten mitsortieren", true);

	public static final Module REFILL = reg(new Module("hotbar_refill", "Hotbar auff\u00fcllen", C_INV, true));

	// ---- Spieler & HUD ----
	public static final Module SPRINT = reg(new Module("auto_sprint", "Auto-Sprint", C_MISC, false));

	public static final Module ZOOM = reg(new Module("zoom", "Zoom (Taste halten)", C_MISC, true));
	public static final Setting.Key ZOOM_KEY = ZOOM.keybind("Zoom-Taste", GLFW.GLFW_KEY_C);
	public static final Setting.Num ZOOM_FOV = ZOOM.num("FOV beim Zoomen", 30, 60, 30, 1);

	public static final Module INFO = reg(new Module("info_hud", "Info-Anzeige", C_MISC, false));
	public static final Setting.Color INFO_COLOR = INFO.color("Farbe", 0xFFFFFF);
	public static final Setting.Bool INFO_COORDS = INFO.bool("Koordinaten", true);
	public static final Setting.Bool INFO_DIR = INFO.bool("Richtung", true);
	public static final Setting.Bool INFO_FPS = INFO.bool("FPS", true);

	private Modules() {}
}
