package net.davethemlgpro.client.translation;

import net.minecraft.network.chat.Component;

public enum TranslationKey {
	KEY_CATEGORY_GENERAL("key.category.another-hud-mod.general"),
	KEY_OPEN_LAYOUT_EDITOR("key.another-hud-mod.open_layout_editor"),
	MODULE_ARMOR("module.another-hud-mod.armor"),
	EDITOR_TITLE("screen.another-hud-mod.editor.title"),
	EDITOR_INSTRUCTIONS("screen.another-hud-mod.editor.instructions"),
	EDITOR_RESET("screen.another-hud-mod.editor.reset"),
	EDITOR_SAVE_FAILED("screen.another-hud-mod.editor.save_failed");

	private final String key;

	TranslationKey(String key) {
		this.key = key;
	}

	public String key() {
		return key;
	}

	public Component component() {
		return Component.translatable(key);
	}
}
