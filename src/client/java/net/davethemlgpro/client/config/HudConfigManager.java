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
	private static final int SCHEMA_VERSION = 2;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final HudModuleRegistry registry;
	private final Path path;
	private EditorConfig editor = new EditorConfig();
	private JsonObject unknownModules = new JsonObject();
	private JsonObject unknownModuleEnabled = new JsonObject();

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
		Map<HudModuleEntry<?>, HudModuleConfig<?>> replacementModules = new LinkedHashMap<>();
		Map<HudModuleEntry<?>, Boolean> replacementEnabled = new LinkedHashMap<>();
		for (HudModuleEntry<?> entry : registry.getEntries()) {
			HudModuleConfig<?> config = entry.prepareConfigUntyped(
				snapshot.getConfig(entry.getModule().id()));
			replacementModules.put(entry, config);
			replacementEnabled.put(entry, snapshot.isModuleEnabled(entry.getModule().id()));
		}
		for (HudModuleEntry<?> entry : registry.getEntries()) {
			entry.replacePreparedConfigUntyped(replacementModules.get(entry));
			entry.setEnabled(replacementEnabled.get(entry));
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

			JsonObject serializedModules = getSerializedModules(root);
			JsonObject serializedModuleEnabled = getSerializedModuleEnabled(root);
			Map<HudModuleEntry<?>, HudModuleConfig<?>> loadedModules = new LinkedHashMap<>();
			Map<HudModuleEntry<?>, Boolean> loadedModuleEnabled = new LinkedHashMap<>();
			JsonObject loadedUnknown = serializedModules.deepCopy();
			JsonObject loadedUnknownEnabled = serializedModuleEnabled.deepCopy();
			for (HudModuleEntry<?> entry : registry.getEntries()) {
				String id = entry.getModule().id().toString();
				HudModuleConfig<?> config = serializedModules.has(id) ? entry.deserialize(GSON, serializedModules.get(id)) : entry.newDefaultConfig();
				loadedModules.put(entry, config);
				loadedModuleEnabled.put(entry, readModuleEnabled(serializedModuleEnabled, id));
				loadedUnknown.remove(id);
				loadedUnknownEnabled.remove(id);
			}

			for (Map.Entry<HudModuleEntry<?>, HudModuleConfig<?>> entry : loadedModules.entrySet()) {
				entry.getKey().applyUntyped(entry.getValue());
				entry.getKey().setEnabled(loadedModuleEnabled.get(entry.getKey()));
			}
			editor = loadedEditor;
			unknownModules = loadedUnknown;
			unknownModuleEnabled = loadedUnknownEnabled;
			AnotherHUDMod.LOGGER.info("Loaded modular hud config from {}", path);
			return true;

		} catch (IOException | JsonParseException | IllegalStateException e) {
			AnotherHUDMod.LOGGER.error("Failed to load config file {}", path, e);
			return false;
		}
	}

	private int getSchemaVersion(JsonObject root) {
		JsonElement element = root.get("schemaVersion");
		if (element == null) {
			return 1;
		}
		if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
			throw new JsonParseException("Config schema version must be a positive integer.");
		}
		try {
			int schemaVersion = element.getAsBigDecimal().intValueExact();
			if (schemaVersion < 1) {
				throw new JsonParseException("Config schema version must be a positive integer.");
			}
			return schemaVersion;
		} catch (ArithmeticException | NumberFormatException e) {
			throw new JsonParseException("Config schema version must be a positive integer.", e);
		}
	}

	private JsonObject getSerializedModules(JsonObject root) {
		JsonElement element = root.get("modules");
		if (element == null || element.isJsonNull()) {
			return new JsonObject();
		}
		if (!element.isJsonObject()) {
			throw new JsonParseException("Modules config must be a JSON object.");
		}
		return element.getAsJsonObject();
	}

	private JsonObject getSerializedModuleEnabled(JsonObject root) {
		JsonElement element = root.get("moduleEnabled");
		if (element == null || element.isJsonNull()) {
			return new JsonObject();
		}
		if (!element.isJsonObject()) {
			throw new JsonParseException("Module enabled state must be a JSON object.");
		}
		return element.getAsJsonObject();
	}

	private boolean readModuleEnabled(JsonObject states, String id) {
		JsonElement element = states.get(id);
		if (element == null) {
			return true;
		}
		if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
			throw new JsonParseException("Module enabled state must be a boolean: " + id);
		}
		return element.getAsBoolean();
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
			AnotherHUDMod.LOGGER.info("Saved modular hud config to {}", path);
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
		root.addProperty("schemaVersion", SCHEMA_VERSION);
		root.add("editor", GSON.toJsonTree(editor));
		JsonObject modules = unknownModules.deepCopy();
		for (HudModuleEntry<?> entry : registry.getEntries()) {
			entry.getConfig().validate();
			modules.add(entry.getModule().id().toString(), entry.serializeUntyped(GSON));
		}
		root.add("modules", modules);
		JsonObject moduleEnabled = unknownModuleEnabled.deepCopy();
		for (HudModuleEntry<?> entry : registry.getEntries()) {
			moduleEnabled.addProperty(entry.getModule().id().toString(), entry.isEnabled());
		}
		root.add("moduleEnabled", moduleEnabled);
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
