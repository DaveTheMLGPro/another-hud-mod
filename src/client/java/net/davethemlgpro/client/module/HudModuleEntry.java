package net.davethemlgpro.client.module;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudSize;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Objects;
import java.util.function.Supplier;

public class HudModuleEntry<C extends HudModuleConfig<C>> {
	private HudModule<C> module;
	private Class<C> configType;
	private Supplier<C> defaults;
	private C config;

	public HudModuleEntry(HudModule<C> module, Class<C> configType, Supplier<C> defaults) {
		this.module = module;
		this.configType = configType;
		this.defaults = defaults;
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

	public HudSize measureUntyped(Minecraft minecraft, HudModuleConfig<?> value) {
		return module.measure(minecraft, configType.cast(value));
	}
}
