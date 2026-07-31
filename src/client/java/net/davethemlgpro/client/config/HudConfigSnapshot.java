package net.davethemlgpro.client.config;

import net.davethemlgpro.client.module.HudModuleConfig;
import net.davethemlgpro.client.module.HudModuleEntry;
import net.davethemlgpro.client.module.HudModuleRegistry;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class HudConfigSnapshot {
	private EditorConfig editorConfig;
	private final Map<Identifier, HudModuleConfig<?>> modules;
	private final Map<Identifier, Boolean> moduleEnabled;

	private HudConfigSnapshot(EditorConfig editorConfig, Map<Identifier, HudModuleConfig<?>> modules,
							  Map<Identifier, Boolean> moduleEnabled) {
		this.editorConfig = editorConfig;
		this.modules = modules;
		this.moduleEnabled = moduleEnabled;
	}

	public static HudConfigSnapshot capture(EditorConfig editor, HudModuleRegistry registry) {
		Map<Identifier, HudModuleConfig<?>> modules = new LinkedHashMap<>();
		Map<Identifier, Boolean> moduleEnabled = new LinkedHashMap<>();
		for (HudModuleEntry<?> entry : registry.getEntries()) {
			modules.put(entry.getModule().id(), entry.copyConfig());
			moduleEnabled.put(entry.getModule().id(), entry.isEnabled());
		}
		return new HudConfigSnapshot(editor.copy(), modules, moduleEnabled);
	}

	public static HudConfigSnapshot defaults(HudModuleRegistry registry) {
		Map<Identifier, HudModuleConfig<?>> modules = new LinkedHashMap<>();
		Map<Identifier, Boolean> moduleEnabled = new LinkedHashMap<>();
		for (HudModuleEntry<?> entry : registry.getEntries()) {
			modules.put(entry.getModule().id(), entry.newDefaultConfig());
			moduleEnabled.put(entry.getModule().id(), true);
		}
		return new HudConfigSnapshot(new EditorConfig(), modules, moduleEnabled);
	}

	public HudConfigSnapshot copy() {
		Map<Identifier, HudModuleConfig<?>> copiedModules = new LinkedHashMap<>();
		for (Map.Entry<Identifier, HudModuleConfig<?>> entry : modules.entrySet()) {
			copiedModules.put(entry.getKey(), entry.getValue().copy());
		}
		return new HudConfigSnapshot(editorConfig.copy(), copiedModules, new LinkedHashMap<>(moduleEnabled));
	}

	private HudModuleConfig<?> internalConfig(Identifier id) {
		HudModuleConfig<?> config = modules.get(id);
		if (config == null) {
			throw new IllegalArgumentException("Unknown HUD module: " + id);
		}
		return config;
	}

	public HudModuleConfig<?> getConfig(Identifier id) {
		return internalConfig(id).copy();
	}

	public HudModuleConfig<?> getRawConfig(Identifier id) {
		return internalConfig(id);
	}

	public boolean hasConfig(Identifier id) {
		return modules.containsKey(id);
	}

	public void setConfig(Identifier id, HudModuleConfig<?> config) {
		modules.put(id, config.copy());
	}

	public boolean isModuleEnabled(Identifier id) {
		Boolean enabled = moduleEnabled.get(id);
		if (enabled == null) {
			throw new IllegalArgumentException("Unknown HUD module: " + id);
		}
		return enabled;
	}

	public void setModuleEnabled(Identifier id, boolean enabled) {
		if (!modules.containsKey(id)) {
			throw new IllegalArgumentException("Unknown HUD module: " + id);
		}
		moduleEnabled.put(id, enabled);
	}

	public <C extends HudModuleConfig<C>> C getModule(Identifier id, Class<C> type) {
		return type.cast(getConfig(id));
	}

	public Set<Identifier> getModuleIds() {
		return Set.copyOf(modules.keySet());
	}

	public EditorConfig getEditor() { return editorConfig.copy(); }

	public EditorConfig getRawEditor() {
		return editorConfig;
	}

	public void setEditor(EditorConfig editor) {
		this.editorConfig = editor.copy();
	}

	public void copyFrom(HudConfigSnapshot other) {
		this.editorConfig = other.editorConfig.copy();
		this.modules.clear();
		other.modules.forEach((id, config) -> this.modules.put(id, config.copy()));
		this.moduleEnabled.clear();
		this.moduleEnabled.putAll(other.moduleEnabled);
	}
}
