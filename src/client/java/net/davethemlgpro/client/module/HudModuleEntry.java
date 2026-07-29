package net.davethemlgpro.client.module;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudSize;
import net.davethemlgpro.client.hud.layout.ModuleLayout;
import net.davethemlgpro.client.screen.popover.HudPopoverTab;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class HudModuleEntry<C extends HudModuleConfig<C>> {
	private HudModule<C> module;
	private Class<C> configType;
	private Supplier<C> defaults;
	private HudModulePopoverFactory<C> popoverFactory;
	private C config;

	public HudModuleEntry(HudModule<C> module, Class<C> configType, Supplier<C> defaults) {
		this(module, configType, defaults, null);
	}

	public HudModuleEntry(HudModule<C> module, Class<C> configType, Supplier<C> defaults,
						  HudModulePopoverFactory<C> popoverFactory) {
		this.module = module;
		this.configType = configType;
		this.defaults = defaults;
		this.popoverFactory = popoverFactory;
		this.config = newDefaultConfig();
	}

	public HudModule<C> getModule() {
		return module;
	}

	public Class<C> getConfigType() {
		return configType;
	}

	public Supplier<C> getDefaults() {
		return defaults;
	}

	public C getConfig() {
		return config;
	}

	public C copyConfig() {
		return config.copy();
	}

	public boolean hasPopoverControls() {
		return popoverFactory != null;
	}

	public List<HudPopoverTab> createPopoverTabsUntyped(HudModuleConfig<?> value) {
		if (popoverFactory == null) {
			return List.of();
		}
		return popoverFactory.create(configType.cast(value));
	}

	public C newDefaultConfig() {
		C value = Objects.requireNonNull(defaults.get(), "Default config cannot be null.");
		value.validate();
		return value;
	}

	public void replaceConfig(C newConfig) {
		newConfig.validate();
		this.config = newConfig;
	}

	public JsonElement serialize(Gson gson, C value) {
		return gson.toJsonTree(value, configType);
	}

	public JsonElement serializeUntyped(Gson gson, HudModuleConfig<?> value) {
		return serialize(gson, configType.cast(value));
	}

	public JsonElement serializeUntyped(Gson gson) {
		return gson.toJsonTree(config, configType);
	}

	public C deserialize(Gson gson, JsonElement json) {
		C value = gson.fromJson(json, configType);
		if (value == null) {
			throw new JsonParseException("Config cannot be null: " + module.id());
		}
		value.validate();
		return value;
	}

	public void applyUntyped(HudModuleConfig<?> value) {
		replaceConfig(configType.cast(value).copy());
	}

	public void renderUntyped(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft, HudModuleConfig<?> value, HudBounds bounds) {
		module.render(graphics, deltaTracker, minecraft, configType.cast(value), bounds);
	}

	public void renderEditorPreviewUntyped(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker,
										   Minecraft minecraft, HudModuleConfig<?> value, HudBounds bounds) {
		module.renderEditorPreview(graphics, deltaTracker, minecraft, configType.cast(value), bounds);
	}

	public HudSize measureUntyped(Minecraft minecraft, HudModuleConfig<?> value) {
		return module.measure(minecraft, configType.cast(value));
	}

	public HudSize measureEditorPreviewUntyped(Minecraft minecraft, HudModuleConfig<?> value) {
		return module.measureEditorPreview(minecraft, configType.cast(value));
	}

	public int elementCountUntyped(HudModuleConfig<?> value) {
		return module.elementCount(configType.cast(value));
	}

	public ModuleLayout elementLayoutUntyped(HudModuleConfig<?> value, int elementIndex) {
		return module.elementLayout(configType.cast(value), elementIndex);
	}

	public boolean elementVisibleUntyped(HudModuleConfig<?> value, int elementIndex) {
		return module.elementVisible(configType.cast(value), elementIndex);
	}

	public void setElementVisibleUntyped(HudModuleConfig<?> value, int elementIndex, boolean visible) {
		module.setElementVisible(configType.cast(value), elementIndex, visible);
	}

	public HudSize measureElementUntyped(Minecraft minecraft, HudModuleConfig<?> value, int elementIndex,
										 boolean editorPreview) {
		return module.measureElement(minecraft, configType.cast(value), elementIndex, editorPreview);
	}

	public void renderElementUntyped(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft,
									 HudModuleConfig<?> value, int elementIndex, HudBounds bounds,
									 boolean editorPreview) {
		module.renderElement(graphics, deltaTracker, minecraft, configType.cast(value), elementIndex, bounds,
			editorPreview);
	}
}
