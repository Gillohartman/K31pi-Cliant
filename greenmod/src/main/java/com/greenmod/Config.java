package com.greenmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Saves/loads module state to config/greenmod.json. */
public final class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private Config() {}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("greenmod.json");
	}

	public static void load() {
		try {
			Path p = file();
			if (!Files.exists(p)) return;
			JsonObject root = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
			for (Module m : Modules.ALL) {
				JsonObject o = root.getAsJsonObject(m.id);
				if (o == null) continue;
				if (o.has("enabled")) m.enabled = o.get("enabled").getAsBoolean();
				if (o.has("key")) m.key = o.get("key").getAsInt();
				JsonObject so = o.getAsJsonObject("settings");
				if (so == null) continue;
				for (Setting s : m.settings) {
					if (!so.has(s.name)) continue;
					if (s instanceof Setting.Bool b) b.value = so.get(s.name).getAsBoolean();
					else if (s instanceof Setting.Num n) n.value = Math.max(n.min, Math.min(n.max, so.get(s.name).getAsDouble()));
					else if (s instanceof Setting.Color c) c.rgb = so.get(s.name).getAsInt();
					else if (s instanceof Setting.Key k) k.key = so.get(s.name).getAsInt();
				}
			}
		} catch (Exception ignored) {
		}
	}

	public static void save() {
		try {
			JsonObject root = new JsonObject();
			for (Module m : Modules.ALL) {
				JsonObject o = new JsonObject();
				o.addProperty("enabled", m.enabled);
				o.addProperty("key", m.key);
				JsonObject so = new JsonObject();
				for (Setting s : m.settings) {
					if (s instanceof Setting.Bool b) so.addProperty(s.name, b.value);
					else if (s instanceof Setting.Num n) so.addProperty(s.name, n.value);
					else if (s instanceof Setting.Color c) so.addProperty(s.name, c.rgb);
					else if (s instanceof Setting.Key k) so.addProperty(s.name, k.key);
				}
				o.add("settings", so);
				root.add(m.id, o);
			}
			Files.writeString(file(), GSON.toJson(root));
		} catch (Exception ignored) {
		}
	}
}
