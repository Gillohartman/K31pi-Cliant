package com.greenmod;

import java.util.ArrayList;
import java.util.List;

public final class Module {
	public final String id, name, category;
	public boolean enabled;
	/** Key that toggles the module on/off (-1 = none). */
	public int key = -1;
	public boolean lastDown;
	public final List<Setting> settings = new ArrayList<>();

	public Module(String id, String name, String category, boolean enabled) {
		this.id = id;
		this.name = name;
		this.category = category;
		this.enabled = enabled;
	}

	public void toggle() { enabled = !enabled; }

	public Setting.Bool bool(String n, boolean def) {
		Setting.Bool s = new Setting.Bool(n, def);
		settings.add(s);
		return s;
	}

	public Setting.Num num(String n, double min, double max, double def, double step) {
		Setting.Num s = new Setting.Num(n, min, max, def, step);
		settings.add(s);
		return s;
	}

	public Setting.Color color(String n, int rgb) {
		Setting.Color s = new Setting.Color(n, rgb);
		settings.add(s);
		return s;
	}

	public Setting.Key keybind(String n, int key) {
		Setting.Key s = new Setting.Key(n, key);
		settings.add(s);
		return s;
	}
}
