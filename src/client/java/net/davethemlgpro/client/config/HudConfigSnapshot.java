package net.davethemlgpro.client.config;

import net.davethemlgpro.client.module.HudModuleConfig;
import net.davethemlgpro.client.module.HudModuleEntry;
import net.davethemlgpro.client.module.HudModuleRegistry;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class HudConfigSnapshot {
	private final EditorConfig editorConfig;
	private final Map<Identifier, HudModuleConfig<?>> modules;

	private HudConfigSnapshot(EditorConfig editorConfig, Map<Identifier, HudModuleConfig<?>> modules) {
		this.editorConfig = editorConfig;
		this.modules = modules;
	}

	public static HudConfigSnapshot capture(EditorConfig editor, HudModuleRegistry registry) {
		Map<Identifier, HudModuleConfig<?>> modules = new LinkedHashMap<>();
		for (HudModuleEntry<?> entry : registry.getModules()) {
			modules.put(entry.getModule().id(), entry.copyConfig());
		}
		return new HudConfigSnapshot(editor.copy(), modules);
	}

	public static HudConfigSnapshot defaults(HudModuleRegistry registry) {
		Map<Identifier, HudModuleConfig<?>> modules = new LinkedHashMap<>();
		for (HudModuleEntry<?> entry : registry.getModules()) {
			modules.put(entry.getModule().id(), entry.newDefaultConfig());
		}
		return new HudConfigSnapshot(new EditorConfig(), modules);
	}

	public HudConfigSnapshot copy() {
		Map<Identifier, HudModuleConfig<?>> copiedModules = new LinkedHashMap<>();
		for (Map.Entry<Identifier, HudModuleConfig<?>> entry : modules.entrySet()) {
			copiedModules.put(entry.getKey(), entry.getValue().copy());
		}
		return new HudConfigSnapshot(editorConfig.copy(), copiedModules);
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

	public <C extends HudModuleConfig<C>> C getModule(Identifier id, Class<C> type) {
		return type.cast(getConfig(id));
	}

	public Set<Identifier> getModuleIds() {
		return Set.copyOf(modules.keySet());
	}

	public EditorConfig getEditor() { return editorConfig.copy(); }
}
