package net.davethemlgpro.client.config;

import net.davethemlgpro.client.module.HudModuleRegistry;
import net.davethemlgpro.client.module.armor.ArmorHudConfig;
import net.davethemlgpro.client.module.armor.ArmorHudModule;
import net.davethemlgpro.client.module.itempickup.ItemPickupHudConfig;
import net.davethemlgpro.client.module.itempickup.ItemPickupHudModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HudModuleStateTest {
	@TempDir
	Path temporaryDirectory;

	@AfterEach
	void closeEditSession() {
		HudEditSession active = HudEditSession.getActive();
		if (active != null) {
			active.cancelEdit();
		}
	}

	@Test
	void moduleEnabledStateRoundTripsWithoutChangingFeatureSettings() {
		Path configPath = temporaryDirectory.resolve("hud.json");
		HudModuleRegistry savedRegistry = registry();
		HudConfigManager savedManager = new HudConfigManager(savedRegistry, configPath);
		HudConfigSnapshot changed = savedManager.getSnapshot();
		changed.setModuleEnabled(ArmorHudModule.ID, false);
		ArmorHudConfig armor = (ArmorHudConfig) changed.getRawConfig(ArmorHudModule.ID);
		armor.setSpacing(7);
		savedManager.applySnapshot(changed);

		assertTrue(savedManager.save());

		HudModuleRegistry loadedRegistry = registry();
		HudConfigManager loadedManager = new HudConfigManager(loadedRegistry, configPath);
		assertTrue(loadedManager.load());
		assertFalse(loadedRegistry.getModule(ArmorHudModule.ID).isEnabled());
		ArmorHudConfig loadedArmor = (ArmorHudConfig) loadedRegistry.getModule(ArmorHudModule.ID).getConfig();
		assertEquals(7, loadedArmor.getSpacing());
	}

	@Test
	void resettingOneModuleLeavesOtherModulesAndEditorSettingsAlone() {
		HudConfigManager manager = new HudConfigManager(registry(), temporaryDirectory.resolve("hud.json"));
		HudEditSession session = HudEditSession.beginEdit(manager, true);
		HudConfigSnapshot draft = session.getDraft();
		draft.getRawEditor().setAccentColor(0xFF123456);
		draft.setModuleEnabled(ArmorHudModule.ID, false);
		((ArmorHudConfig) draft.getRawConfig(ArmorHudModule.ID)).setSpacing(7);
		((ItemPickupHudConfig) draft.getRawConfig(ItemPickupHudModule.ID)).setMaxVisibleItems(8);

		session.resetModuleToDefaults(ArmorHudModule.ID);

		assertTrue(draft.isModuleEnabled(ArmorHudModule.ID));
		assertEquals(2, ((ArmorHudConfig) draft.getRawConfig(ArmorHudModule.ID)).getSpacing());
		assertEquals(8, ((ItemPickupHudConfig) draft.getRawConfig(ItemPickupHudModule.ID)).getMaxVisibleItems());
		assertEquals(0xFF123456, draft.getRawEditor().getAccentColor());
	}

	@Test
	void resettingAllModulesPreservesEditorSettings() {
		HudConfigManager manager = new HudConfigManager(registry(), temporaryDirectory.resolve("hud.json"));
		HudEditSession session = HudEditSession.beginEdit(manager, true);
		HudConfigSnapshot draft = session.getDraft();
		draft.getRawEditor().setAccentColor(0xFF123456);
		draft.setModuleEnabled(ArmorHudModule.ID, false);
		draft.setModuleEnabled(ItemPickupHudModule.ID, false);
		((ArmorHudConfig) draft.getRawConfig(ArmorHudModule.ID)).setSpacing(7);
		((ItemPickupHudConfig) draft.getRawConfig(ItemPickupHudModule.ID)).setMaxVisibleItems(8);

		session.resetModulesToDefaults();

		assertTrue(draft.isModuleEnabled(ArmorHudModule.ID));
		assertTrue(draft.isModuleEnabled(ItemPickupHudModule.ID));
		assertEquals(2, ((ArmorHudConfig) draft.getRawConfig(ArmorHudModule.ID)).getSpacing());
		assertEquals(3, ((ItemPickupHudConfig) draft.getRawConfig(ItemPickupHudModule.ID)).getMaxVisibleItems());
		assertEquals(0xFF123456, draft.getRawEditor().getAccentColor());
	}

	@Test
	void nestedModuleManagerCancelRestoresItsOpeningDraftWithoutDiscardingEarlierEditorChanges() {
		HudConfigManager manager = new HudConfigManager(registry(), temporaryDirectory.resolve("hud.json"));
		HudEditSession session = HudEditSession.beginEdit(manager, true);
		HudConfigSnapshot draft = session.getDraft();
		draft.getRawEditor().setAccentColor(0xFF123456);
		((ArmorHudConfig) draft.getRawConfig(ArmorHudModule.ID)).setSpacing(5);
		HudConfigSnapshot managerOpeningState = draft.copy();

		draft.setModuleEnabled(ArmorHudModule.ID, false);
		((ArmorHudConfig) draft.getRawConfig(ArmorHudModule.ID)).setSpacing(9);
		((ItemPickupHudConfig) draft.getRawConfig(ItemPickupHudModule.ID)).setMaxVisibleItems(8);
		draft.copyFrom(managerOpeningState);

		assertTrue(draft.isModuleEnabled(ArmorHudModule.ID));
		assertEquals(5, ((ArmorHudConfig) draft.getRawConfig(ArmorHudModule.ID)).getSpacing());
		assertEquals(3, ((ItemPickupHudConfig) draft.getRawConfig(ItemPickupHudModule.ID)).getMaxVisibleItems());
		assertEquals(0xFF123456, draft.getRawEditor().getAccentColor());
	}

	private static HudModuleRegistry registry() {
		HudModuleRegistry registry = new HudModuleRegistry();
		registry.register(new ArmorHudModule(), ArmorHudConfig.class, ArmorHudConfig::new);
		registry.register(new ItemPickupHudModule(), ItemPickupHudConfig.class, ItemPickupHudConfig::new);
		return registry;
	}
}
