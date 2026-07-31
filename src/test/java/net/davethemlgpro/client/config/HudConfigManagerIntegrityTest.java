package net.davethemlgpro.client.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.davethemlgpro.client.module.HudModuleRegistry;
import net.davethemlgpro.client.module.armor.ArmorHudConfig;
import net.davethemlgpro.client.module.armor.ArmorHudModule;
import net.davethemlgpro.client.module.itempickup.ItemPickupHudConfig;
import net.davethemlgpro.client.module.itempickup.ItemPickupHudModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HudConfigManagerIntegrityTest {
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
	void rejectsMalformedSchemaVersionsWithoutChangingLiveState() throws IOException {
		Path configPath = temporaryDirectory.resolve("hud.json");
		HudModuleRegistry registry = registry();
		HudConfigManager manager = new HudConfigManager(registry, configPath);
		((ArmorHudConfig) registry.getModule(ArmorHudModule.ID).getConfig()).setSpacing(7);

		for (String schemaVersion : List.of("\"999\"", "1.5", "0", "-1", "null")) {
			Files.writeString(configPath, """
				{"schemaVersion": %s, "editor": {}, "modules": {}, "moduleEnabled": {}}
				""".formatted(schemaVersion));

			assertFalse(manager.load(), "schemaVersion=" + schemaVersion);
			assertEquals(7,
				((ArmorHudConfig) registry.getModule(ArmorHudModule.ID).getConfig()).getSpacing());
		}
	}

	@Test
	void preservesUnknownModuleConfigAndEnabledState() throws IOException {
		Path configPath = temporaryDirectory.resolve("hud.json");
		Files.writeString(configPath, """
			{
			  "schemaVersion": 2,
			  "editor": {},
			  "modules": {"future-mod:widget": {"value": 42}},
			  "moduleEnabled": {"future-mod:widget": false}
			}
			""");
		HudConfigManager manager = new HudConfigManager(registry(), configPath);

		assertTrue(manager.load());
		assertTrue(manager.save());

		JsonObject saved = JsonParser.parseString(Files.readString(configPath)).getAsJsonObject();
		assertEquals(42, saved.getAsJsonObject("modules")
			.getAsJsonObject("future-mod:widget").get("value").getAsInt());
		assertFalse(saved.getAsJsonObject("moduleEnabled").get("future-mod:widget").getAsBoolean());
	}

	@Test
	void invalidSnapshotCannotPartiallyApplyEarlierModules() {
		HudModuleRegistry registry = registry();
		HudConfigManager manager = new HudConfigManager(registry, temporaryDirectory.resolve("hud.json"));
		HudConfigSnapshot draft = manager.getSnapshot();
		((ArmorHudConfig) draft.getRawConfig(ArmorHudModule.ID)).setSpacing(7);
		draft.setConfig(ItemPickupHudModule.ID, new ArmorHudConfig());

		assertThrows(ClassCastException.class, () -> manager.applySnapshot(draft));

		assertEquals(2, ((ArmorHudConfig) registry.getModule(ArmorHudModule.ID).getConfig()).getSpacing());
		assertTrue(registry.getModule(ItemPickupHudModule.ID).getConfig() instanceof ItemPickupHudConfig);
	}

	@Test
	void failedSaveRollsBackAppliedDraft() throws IOException {
		Path nonDirectory = temporaryDirectory.resolve("not-a-directory");
		Files.writeString(nonDirectory, "blocks config directory creation");
		HudModuleRegistry registry = registry();
		HudConfigManager manager = new HudConfigManager(registry, nonDirectory.resolve("hud.json"));
		HudEditSession session = HudEditSession.beginEdit(manager, true);
		((ArmorHudConfig) session.getDraft().getRawConfig(ArmorHudModule.ID)).setSpacing(7);

		assertFalse(session.applyAndSave());
		assertEquals(2, ((ArmorHudConfig) registry.getModule(ArmorHudModule.ID).getConfig()).getSpacing());
		assertEquals(session, HudEditSession.getActive());
	}

	private static HudModuleRegistry registry() {
		HudModuleRegistry registry = new HudModuleRegistry();
		registry.register(new ArmorHudModule(), ArmorHudConfig.class, ArmorHudConfig::new);
		registry.register(new ItemPickupHudModule(), ItemPickupHudConfig.class, ItemPickupHudConfig::new);
		return registry;
	}
}
