package net.davethemlgpro.client.config;

import com.google.gson.*;
import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.module.HudModuleConfig;
import net.davethemlgpro.client.module.HudModuleEntry;
import net.davethemlgpro.client.module.HudModuleRegistry;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class HudConfigManager {
	public final int SCHEMA_VERSION = 1;
	private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final HudModuleRegistry registry;
	private final Path path;
	private EditorConfig editor = new EditorConfig();
	private JsonObject unknownModules = new JsonObject();

	public HudConfigManager(HudModuleRegistry registry, Path path) {
		this.registry = registry;
		this.path = path;
	}

	public static HudConfigManager createDefault(HudModuleRegistry registry) {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(AnotherHUDMod.MOD_ID + ".json");
		return new HudConfigManager(registry, path);
	}

	public EditorConfig getEditorConfig() {
		return editor;
	}

	public HudConfigSnapshot getSnapshot() {
		return HudConfigSnapshot.capture(editor, registry);
	}

	public HudConfigSnapshot defaultSnapshot() {
		return HudConfigSnapshot.defaults(registry);
	}

	public void applySnapshot(HudConfigSnapshot snapshot) {
		EditorConfig replacementConfig = snapshot.getEditor();
		replacementConfig.validate();
		for (HudModuleEntry<?> entry : registry.getModules()) {
			HudModuleConfig<?> config = snapshot.getConfig(entry.getModule().id());
			entry.applyUntyped(config);
		}
		editor = replacementConfig;
	}

	public boolean load() {
		if (Files.notExists(path)) {
			return save();
		}

		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			JsonObject root = GSON.fromJson(reader, JsonObject.class);
			if  (root == null) {
				throw new JsonParseException("Config file contains no JSON object.");
			}

			int schemaVersion = getSchemaVersion(root);
			if (schemaVersion > SCHEMA_VERSION) {
				throw new JsonParseException("Config schema " + schemaVersion + " is newer than supported schema " + SCHEMA_VERSION);
			}

			EditorConfig loadedEditor = root.has("editor") ? GSON.fromJson(root.get("editor"), EditorConfig.class) : new EditorConfig();
			if (loadedEditor == null) {
				throw new JsonParseException("Editor config is null.");
			}
			loadedEditor.validate();

			JsonObject serializedModules = root.has("modules") ? GSON.fromJson(root.get("modules"), JsonObject.class) : new JsonObject();
			Map<HudModuleEntry<?>, HudModuleConfig<?>> loadedModules = new LinkedHashMap<>();
			JsonObject loadedUnknown = serializedModules.deepCopy();
			for (HudModuleEntry<?> entry : registry.getModules()) {
				String id = entry.getModule().id().toString();
				HudModuleConfig<?> config = serializedModules.has(id) ? entry.deserialize(GSON, serializedModules.get(id)) : entry.newDefaultConfig();
				loadedModules.put(entry, config);
				loadedUnknown.remove(id);
			}

			for (Map.Entry<HudModuleEntry<?>, HudModuleConfig<?>> entry : loadedModules.entrySet()) {
				entry.getKey().applyUntyped(entry.getValue());
			}
			editor = loadedEditor;
			unknownModules = loadedUnknown;
			AnotherHUDMod.LOGGER.info("Loaded modular hud config from {}", path);
			return true;

		} catch (IOException | JsonParseException | IllegalStateException e) {
			AnotherHUDMod.LOGGER.error("Failed to load config file {}", path, e);
			return false;
		}
	}

	private int getSchemaVersion(JsonObject root) {
		JsonElement element = root.get("schemaVersion");
		boolean isNumber = element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();

		return isNumber ? element.getAsInt() : 1;
	}

	public boolean save() {
		Path tmpPath = path.resolveSibling(path.getFileName() + ".tmp");
		try {
			Path parent = path.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			try (Writer writer = Files.newBufferedWriter(tmpPath, StandardCharsets.UTF_8)) {
				GSON.toJson(toJson(), writer);
			}
			moveIntoPlace(tmpPath, path);
			AnotherHUDMod.LOGGER.info("Saved modular hud config to {}", tmpPath);
			return true;
		} catch (IOException e) {
			AnotherHUDMod.LOGGER.error("Failed to save config file {}", tmpPath, e);
			return false;
		} finally {
			try {
				Files.deleteIfExists(tmpPath);
			} catch (IOException e) {
				AnotherHUDMod.LOGGER.error("Failed to delete config file {}", tmpPath, e);
			}
		}
	}

	public JsonObject toJson() {
		JsonObject root = new JsonObject();
		root.addProperty("schemaVersion", getSchemaVersion(root));
		root.add("editor", GSON.toJsonTree(editor));
		JsonObject modules = unknownModules.deepCopy();
		for (HudModuleEntry<?> entry : registry.getModules()) {
			entry.getConfig().validate();
			modules.add(entry.getModule().id().toString(), entry.serializeUntyped(GSON));
		}
		root.add("modules", modules);
		return root;
	}

	public void moveIntoPlace(Path tmpPath, Path newPath) throws IOException {
		try {
			Files.move(tmpPath, newPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(tmpPath, newPath, StandardCopyOption.REPLACE_EXISTING);
		}
	}

}
