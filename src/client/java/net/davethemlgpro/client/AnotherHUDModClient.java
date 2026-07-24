package net.davethemlgpro.client;

import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.config.HudConfigManager;
import net.davethemlgpro.client.hud.HudRenderDispatcher;
import net.davethemlgpro.client.module.HudModuleRegistry;
import net.davethemlgpro.client.module.armor.ArmorHudConfig;
import net.davethemlgpro.client.module.armor.ArmorHudModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public class AnotherHUDModClient implements ClientModInitializer {
	private static final HudModuleRegistry MODULES = new HudModuleRegistry();
	private static HudConfigManager configManager;

	@Override
	public void onInitializeClient() {
		MODULES.register(new ArmorHudModule(), ArmorHudConfig.class, ArmorHudConfig::new);

		configManager = HudConfigManager.createDefault(MODULES);
		if (!configManager.load())
		{
			AnotherHUDMod.LOGGER.warn("Hud config could not be loaded or created.");
		}

		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
			AnotherHUDMod.id("modules"), new HudRenderDispatcher(MODULES)
		);
	}

	public static HudModuleRegistry getHudModuleRegistry() {
		return MODULES;
	}

	public static HudConfigManager getHudConfigManager() {
		return configManager;
	}
}
