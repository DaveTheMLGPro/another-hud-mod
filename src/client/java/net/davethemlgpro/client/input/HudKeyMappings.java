package net.davethemlgpro.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.AnotherHUDModClient;
import net.davethemlgpro.client.config.HudEditSession;
import net.davethemlgpro.client.module.miningsession.MiningSessionHudModule;
import net.davethemlgpro.client.screen.HudLayoutEditorScreen;
import net.davethemlgpro.client.translation.TranslationKey;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class HudKeyMappings {
	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(AnotherHUDMod.id("general"));
	private static final KeyMapping OPEN_LAYOUT_EDITOR = new KeyMapping(
		TranslationKey.KEY_OPEN_LAYOUT_EDITOR.key(), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, CATEGORY);
	private static final KeyMapping TOGGLE_MINING_SESSION = new KeyMapping(
		TranslationKey.KEY_TOGGLE_MINING_SESSION.key(), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);
	private static final KeyMapping RESET_MINING_SESSION = new KeyMapping(
		TranslationKey.KEY_RESET_MINING_SESSION.key(), InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);

	private HudKeyMappings() {
	}

	public static void register() {
		KeyMappingHelper.registerKeyMapping(OPEN_LAYOUT_EDITOR);
		KeyMappingHelper.registerKeyMapping(TOGGLE_MINING_SESSION);
		KeyMappingHelper.registerKeyMapping(RESET_MINING_SESSION);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (OPEN_LAYOUT_EDITOR.consumeClick()) {
				if (client.player == null || client.gui.screen() != null) {
					continue;
				}

				HudEditSession session = HudEditSession.beginEdit(AnotherHUDModClient.getHudConfigManager(), true);
				client.gui.setScreen(new HudLayoutEditorScreen(null, session,
					AnotherHUDModClient.getHudModuleRegistry(), AnotherHUDModClient.getHudRenderDispatcher()));
			}
			while (TOGGLE_MINING_SESSION.consumeClick()) {
				if (client.player != null && client.gui.screen() == null
					&& AnotherHUDModClient.getHudModuleRegistry().isModuleEnabled(
						MiningSessionHudModule.ID)) {
					AnotherHUDModClient.getMiningSessionHudModule().toggleSession();
				}
			}
			while (RESET_MINING_SESSION.consumeClick()) {
				if (client.player != null && client.gui.screen() == null
					&& AnotherHUDModClient.getHudModuleRegistry().isModuleEnabled(
						MiningSessionHudModule.ID)) {
					AnotherHUDModClient.getMiningSessionHudModule().resetSession();
				}
			}
			if (client.player == null) {
				AnotherHUDModClient.getMiningSessionHudModule().pauseSession();
			} else if (AnotherHUDModClient.getHudModuleRegistry().isModuleEnabled(
				MiningSessionHudModule.ID)) {
				AnotherHUDModClient.getMiningSessionHudModule().updateInventory(client.player,
					AnotherHUDModClient.getMiningSessionHudConfig());
			}
		});
	}
}
