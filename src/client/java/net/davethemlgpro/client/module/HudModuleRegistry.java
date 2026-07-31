package net.davethemlgpro.client.module;

import net.minecraft.resources.Identifier;
import java.util.*;
import java.util.function.Supplier;

public class HudModuleRegistry {
	private final Map<Identifier, HudModuleEntry<?>> modules = new LinkedHashMap<>();
	private List<HudModuleEntry<?>> entries = List.of();

	public <C extends HudModuleConfig<C>> HudModuleEntry<C> register(HudModule<C> module, Class<C> configType, Supplier<C> defaults) {
		return register(module, configType, defaults, null);
	}

	public <C extends HudModuleConfig<C>> HudModuleEntry<C> register(HudModule<C> module, Class<C> configType,
																	Supplier<C> defaults, HudModulePopoverFactory<C> popoverFactory) {
		Identifier id = module.id();
		if (modules.containsKey(id)) {
			throw new IllegalArgumentException("Duplicate HUD module: " + id.toString());
		}
		HudModuleEntry<C> entry = new HudModuleEntry<>(module, configType, defaults, popoverFactory);
		modules.put(id, entry);
		entries = List.copyOf(modules.values());
		return entry;
	}

	public HudModuleEntry<?> getModule(Identifier id) {
		return modules.get(id);
	}

	public boolean isModuleEnabled(Identifier id) {
		HudModuleEntry<?> entry = modules.get(id);
		return entry != null && entry.isEnabled();
	}

	public List<HudModuleEntry<?>> getEntries() {
		return entries;
	}
}
