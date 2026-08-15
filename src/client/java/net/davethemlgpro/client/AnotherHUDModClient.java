package net.davethemlgpro.client;

import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.config.HudConfigManager;
import net.davethemlgpro.client.hud.HudRenderDispatcher;
import net.davethemlgpro.client.input.HudKeyMappings;
import net.davethemlgpro.client.module.HudModuleRegistry;
import net.davethemlgpro.client.module.armor.ArmorHudConfig;
import net.davethemlgpro.client.module.armor.ArmorHudModule;
import net.davethemlgpro.client.module.armor.ArmorHudPopover;
import net.davethemlgpro.client.module.itempickup.ItemPickupHudConfig;
import net.davethemlgpro.client.module.itempickup.ItemPickupHudModule;
import net.davethemlgpro.client.module.itempickup.ItemPickupHudPopover;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public class AnotherHUDModClient implements ClientModInitializer {
	private static final HudModuleRegistry MODULES = new HudModuleRegistry();
	private static HudConfigManager configManager;
	private static HudRenderDispatcher renderDispatcher;
	private static ItemPickupHudModule itemPickupHudModule;
	private static ArmorHudModule armorHudModule;

	@Override
	public void onInitializeClient() {
		armorHudModule = new ArmorHudModule();
		MODULES.register(armorHudModule, ArmorHudConfig.class, ArmorHudConfig::new,
			ArmorHudPopover::create);
		itemPickupHudModule = new ItemPickupHudModule();
		MODULES.register(itemPickupHudModule, ItemPickupHudConfig.class, ItemPickupHudConfig::new,
			ItemPickupHudPopover::create);

		configManager = HudConfigManager.createDefault(MODULES);
		if (!configManager.load())
		{
			AnotherHUDMod.LOGGER.warn("Hud config could not be loaded or created.");
		}

		renderDispatcher = new HudRenderDispatcher(MODULES);
		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
			AnotherHUDMod.id("modules"), renderDispatcher);
		HudKeyMappings.register();
		ClientTickEvents.END_CLIENT_TICK.register(client -> armorHudModule.tickSoundWarning(client,
			getArmorHudConfig(), MODULES.isModuleEnabled(ArmorHudModule.ID)));
	}

	public static HudModuleRegistry getHudModuleRegistry() {
		return MODULES;
	}

	public static HudConfigManager getHudConfigManager() {
		return configManager;
	}

	public static HudRenderDispatcher getHudRenderDispatcher() {
		return renderDispatcher;
	}

	public static ItemPickupHudModule getItemPickupHudModule() {
		return itemPickupHudModule;
	}

	public static ItemPickupHudConfig getItemPickupHudConfig() {
		return (ItemPickupHudConfig) MODULES.getModule(ItemPickupHudModule.ID).getConfig();
	}

	public static ArmorHudConfig getArmorHudConfig() {
		return (ArmorHudConfig) MODULES.getModule(ArmorHudModule.ID).getConfig();
	}
}
