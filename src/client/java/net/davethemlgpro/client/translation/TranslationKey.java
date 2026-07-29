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
	SETTINGS_TAB_LAYOUT("screen.another-hud-mod.settings.tab.layout"),
	SETTINGS_TAB_BAR("screen.another-hud-mod.settings.tab.bar"),
	SETTINGS_TAB_TEXT("screen.another-hud-mod.settings.tab.text"),
	SETTINGS_SECTION_LAYOUT("screen.another-hud-mod.settings.section.layout"),
	SETTINGS_SECTION_DURABILITY_BAR("screen.another-hud-mod.settings.section.durability_bar"),
	SETTINGS_SECTION_DURABILITY_TEXT("screen.another-hud-mod.settings.section.durability_text"),
	SETTINGS_SECTION_LOW_DURABILITY_WARNING("screen.another-hud-mod.settings.section.low_durability_warning"),
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
	SETTINGS_ARMOR_DURABILITY_BAR_VISIBLE("screen.another-hud-mod.settings.armor.durability_bar.visible"),
	SETTINGS_ARMOR_DURABILITY_BAR_VISIBLE_DESCRIPTION(
		"screen.another-hud-mod.settings.armor.durability_bar.visible.description"),
	SETTINGS_ARMOR_DURABILITY_BAR_HEIGHT("screen.another-hud-mod.settings.armor.durability_bar.height"),
	SETTINGS_ARMOR_DURABILITY_BAR_HEIGHT_DESCRIPTION(
		"screen.another-hud-mod.settings.armor.durability_bar.height.description"),
	SETTINGS_ARMOR_DURABILITY_BAR_PADDING("screen.another-hud-mod.settings.armor.durability_bar.padding"),
	SETTINGS_ARMOR_DURABILITY_BAR_PADDING_DESCRIPTION(
		"screen.another-hud-mod.settings.armor.durability_bar.padding.description"),
	SETTINGS_ARMOR_DURABILITY_MODE("screen.another-hud-mod.settings.armor.durability_text.mode"),
	SETTINGS_ARMOR_DURABILITY_MODE_DESCRIPTION(
		"screen.another-hud-mod.settings.armor.durability_text.mode.description"),
	SETTINGS_ARMOR_DURABILITY_TEXT_POSITION("screen.another-hud-mod.settings.armor.durability_text.position"),
	SETTINGS_ARMOR_DURABILITY_TEXT_POSITION_DESCRIPTION(
		"screen.another-hud-mod.settings.armor.durability_text.position.description"),
	SETTINGS_ARMOR_DURABILITY_TEXT_SCALE("screen.another-hud-mod.settings.armor.durability_text.scale"),
	SETTINGS_ARMOR_DURABILITY_TEXT_SCALE_DESCRIPTION(
		"screen.another-hud-mod.settings.armor.durability_text.scale.description"),
	SETTINGS_ARMOR_DURABILITY_TEXT_SHADOW("screen.another-hud-mod.settings.armor.durability_text.shadow"),
	SETTINGS_ARMOR_DURABILITY_TEXT_SHADOW_DESCRIPTION(
		"screen.another-hud-mod.settings.armor.durability_text.shadow.description"),
	SETTINGS_ARMOR_COLOR_BASED_DURABILITY_TEXT(
		"screen.another-hud-mod.settings.armor.durability_text.color_based"),
	SETTINGS_ARMOR_COLOR_BASED_DURABILITY_TEXT_DESCRIPTION(
		"screen.another-hud-mod.settings.armor.durability_text.color_based.description"),
	SETTINGS_ARMOR_LOW_DURABILITY_WARNING_VISIBLE(
		"screen.another-hud-mod.settings.armor.low_durability_warning.visible"),
	SETTINGS_ARMOR_LOW_DURABILITY_WARNING_VISIBLE_DESCRIPTION(
		"screen.another-hud-mod.settings.armor.low_durability_warning.visible.description"),
	SETTINGS_ARMOR_LOW_DURABILITY_THRESHOLD(
		"screen.another-hud-mod.settings.armor.low_durability_warning.threshold"),
	SETTINGS_ARMOR_LOW_DURABILITY_THRESHOLD_DESCRIPTION(
		"screen.another-hud-mod.settings.armor.low_durability_warning.threshold.description"),
	SETTINGS_ARMOR_LOW_DURABILITY_WARNING_STYLE(
		"screen.another-hud-mod.settings.armor.low_durability_warning.style"),
	SETTINGS_ARMOR_LOW_DURABILITY_WARNING_STYLE_DESCRIPTION(
		"screen.another-hud-mod.settings.armor.low_durability_warning.style.description"),
	SETTINGS_VALUE_HORIZONTAL("screen.another-hud-mod.settings.value.horizontal"),
	SETTINGS_VALUE_VERTICAL("screen.another-hud-mod.settings.value.vertical"),
	SETTINGS_VALUE_CLEAR("screen.another-hud-mod.settings.value.clear"),
	SETTINGS_VALUE_INVENTORY("screen.another-hud-mod.settings.value.inventory"),
	SETTINGS_VALUE_HOTBAR("screen.another-hud-mod.settings.value.hotbar"),
	SETTINGS_VALUE_NONE("screen.another-hud-mod.settings.value.none"),
	SETTINGS_VALUE_VALUE("screen.another-hud-mod.settings.value.value"),
	SETTINGS_VALUE_PERCENT("screen.another-hud-mod.settings.value.percent"),
	SETTINGS_VALUE_TOP("screen.another-hud-mod.settings.value.top"),
	SETTINGS_VALUE_BOTTOM("screen.another-hud-mod.settings.value.bottom"),
	SETTINGS_VALUE_LEFT("screen.another-hud-mod.settings.value.left"),
	SETTINGS_VALUE_RIGHT("screen.another-hud-mod.settings.value.right"),
	SETTINGS_VALUE_CENTER("screen.another-hud-mod.settings.value.center"),
	SETTINGS_VALUE_COLOR("screen.another-hud-mod.settings.value.color"),
	SETTINGS_VALUE_PULSE("screen.another-hud-mod.settings.value.pulse"),
	SETTINGS_VALUE_FLASH("screen.another-hud-mod.settings.value.flash");

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
