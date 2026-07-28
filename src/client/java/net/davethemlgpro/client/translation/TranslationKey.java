package net.davethemlgpro.client.translation;

import net.minecraft.network.chat.Component;

public enum TranslationKey {
	KEY_CATEGORY_GENERAL("key.category.another-hud-mod.general"),
	KEY_OPEN_LAYOUT_EDITOR("key.another-hud-mod.open_layout_editor"),
	MODULE_ARMOR("module.another-hud-mod.armor"),
	EDITOR_TITLE("screen.another-hud-mod.editor.title"),
	EDITOR_INSTRUCTIONS("screen.another-hud-mod.editor.instructions"),
	EDITOR_RESET("screen.another-hud-mod.editor.reset"),
	EDITOR_SAVE_FAILED("screen.another-hud-mod.editor.save_failed"),
	POPOVER_NO_SETTINGS("screen.another-hud-mod.popover.no_settings"),
	SETTINGS_ARMOR_ORIENTATION("screen.another-hud-mod.settings.armor.orientation"),
	SETTINGS_ARMOR_ORIENTATION_DESCRIPTION("screen.another-hud-mod.settings.armor.orientation.description"),
	SETTINGS_ARMOR_SCALE("screen.another-hud-mod.settings.armor.scale"),
	SETTINGS_ARMOR_SCALE_DESCRIPTION("screen.another-hud-mod.settings.armor.scale.description"),
	SETTINGS_ARMOR_SPACING("screen.another-hud-mod.settings.armor.spacing"),
	SETTINGS_ARMOR_SPACING_DESCRIPTION("screen.another-hud-mod.settings.armor.spacing.description"),
	SETTINGS_ARMOR_SLOT_STYLE("screen.another-hud-mod.settings.armor.slot_style"),
	SETTINGS_ARMOR_SLOT_STYLE_DESCRIPTION("screen.another-hud-mod.settings.armor.slot_style.description"),
	SETTINGS_ARMOR_SHOW_EMPTY("screen.another-hud-mod.settings.armor.show_empty"),
	SETTINGS_ARMOR_SHOW_EMPTY_DESCRIPTION("screen.another-hud-mod.settings.armor.show_empty.description"),
	SETTINGS_ARMOR_CENTER_VISIBLE("screen.another-hud-mod.settings.armor.center_visible"),
	SETTINGS_ARMOR_CENTER_VISIBLE_DESCRIPTION("screen.another-hud-mod.settings.armor.center_visible.description"),
	SETTINGS_VALUE_HORIZONTAL("screen.another-hud-mod.settings.value.horizontal"),
	SETTINGS_VALUE_VERTICAL("screen.another-hud-mod.settings.value.vertical"),
	SETTINGS_VALUE_CLEAR("screen.another-hud-mod.settings.value.clear"),
	SETTINGS_VALUE_INVENTORY("screen.another-hud-mod.settings.value.inventory"),
	SETTINGS_VALUE_HOTBAR("screen.another-hud-mod.settings.value.hotbar");

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
