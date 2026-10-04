package com.greenmod;

/** One configurable option of a module. */
public abstract class Setting {
	public final String name;

	protected Setting(String name) {
		this.name = name;
	}

	public static final class Bool extends Setting {
		public boolean value;
		public Bool(String name, boolean value) { super(name); this.value = value; }
	}

	public static final class Num extends Setting {
		public final double min, max, step;
		public double value;
		public Num(String name, double min, double max, double value, double step) {
			super(name);
			this.min = min; this.max = max; this.value = value; this.step = step;
		}
	}

	public static final class Color extends Setting {
		public int rgb;
		public Color(String name, int rgb) { super(name); this.rgb = rgb; }
	}

	public static final class Key extends Setting {
		public int key;
		public Key(String name, int key) { super(name); this.key = key; }
	}
}
