package net.davethemlgpro.client;

import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.config.HudConfigManager;
import net.davethemlgpro.client.hud.HudRenderDispatcher;
import net.davethemlgpro.client.input.HudKeyMappings;
import net.davethemlgpro.client.module.HudModuleRegistry;
import net.davethemlgpro.client.module.armor.ArmorHudConfig;
import net.davethemlgpro.client.module.armor.ArmorHudModule;
import net.davethemlgpro.client.module.armor.ArmorHudPopover;
import net.davethemlgpro.client.module.containersearch.ContainerSearchHudConfig;
import net.davethemlgpro.client.module.containersearch.ContainerSearchHudModule;
import net.davethemlgpro.client.module.containersearch.ContainerSearchHudPopover;
import net.davethemlgpro.client.module.containersearch.ContainerSearchOverlay;
import net.davethemlgpro.client.module.itempickup.ItemPickupHudConfig;
import net.davethemlgpro.client.module.itempickup.ItemPickupHudModule;
import net.davethemlgpro.client.module.itempickup.ItemPickupHudPopover;
import net.davethemlgpro.client.module.miningsession.MiningSessionHudConfig;
import net.davethemlgpro.client.module.miningsession.MiningSessionHudModule;
import net.davethemlgpro.client.module.miningsession.MiningSessionHudPopover;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;

public class AnotherHUDModClient implements ClientModInitializer {
	private static final HudModuleRegistry MODULES = new HudModuleRegistry();
	private static HudConfigManager configManager;
	private static HudRenderDispatcher renderDispatcher;
	private static ItemPickupHudModule itemPickupHudModule;
	private static MiningSessionHudModule miningSessionHudModule;
	private static ArmorHudModule armorHudModule;
	private static ContainerSearchHudModule containerSearchHudModule;

	@Override
	public void onInitializeClient() {
		armorHudModule = new ArmorHudModule();
		MODULES.register(armorHudModule, ArmorHudConfig.class, ArmorHudConfig::new,
			ArmorHudPopover::create);
		itemPickupHudModule = new ItemPickupHudModule();
		MODULES.register(itemPickupHudModule, ItemPickupHudConfig.class, ItemPickupHudConfig::new,
			ItemPickupHudPopover::create);
		miningSessionHudModule = new MiningSessionHudModule();
		MODULES.register(miningSessionHudModule, MiningSessionHudConfig.class, MiningSessionHudConfig::new,
			MiningSessionHudPopover::create);
		containerSearchHudModule = new ContainerSearchHudModule();
		MODULES.register(containerSearchHudModule, ContainerSearchHudConfig.class,
			ContainerSearchHudConfig::new, ContainerSearchHudPopover::create);

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
		ClientPlayerBlockBreakEvents.AFTER.register((level, player, pos, state) -> {
			if (MODULES.isModuleEnabled(MiningSessionHudModule.ID)) {
				miningSessionHudModule.recordBlock(state, getMiningSessionHudConfig());
			}
		});
		ContainerSearchOverlay.register();
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

	public static MiningSessionHudModule getMiningSessionHudModule() {
		return miningSessionHudModule;
	}

	public static MiningSessionHudConfig getMiningSessionHudConfig() {
		return (MiningSessionHudConfig) MODULES.getModule(MiningSessionHudModule.ID).getConfig();
	}

	public static ContainerSearchHudModule getContainerSearchHudModule() {
		return containerSearchHudModule;
	}

	public static ContainerSearchHudConfig getContainerSearchHudConfig() {
		return (ContainerSearchHudConfig) MODULES.getModule(ContainerSearchHudModule.ID).getConfig();
	}
}
