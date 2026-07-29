package net.davethemlgpro.client.translation;

import net.minecraft.network.chat.Component;

public enum TranslationKey {
	KEY_CATEGORY_GENERAL("key.category.another-hud-mod.general"),
	KEY_OPEN_LAYOUT_EDITOR("key.another-hud-mod.open_layout_editor"),
	MODULE_ARMOR("module.another-hud-mod.armor.name"),
	EDITOR_TITLE("screen.another-hud-mod.editor.title"),
	EDITOR_INSTRUCTIONS("screen.another-hud-mod.editor.instructions"),
	EDITOR_RESET("screen.another-hud-mod.editor.reset"),
	EDITOR_SAVE_FAILED("screen.another-hud-mod.editor.save_failed"),
	EDITOR_VANILLA_HUD_AREA("screen.another-hud-mod.editor.vanilla_hud_area"),
	EDITOR_SETTINGS_OPEN("screen.another-hud-mod.editor.settings.open"),
	EDITOR_SETTINGS_TITLE("screen.another-hud-mod.editor.settings.title"),
	EDITOR_SETTINGS_TAB_GRID("screen.another-hud-mod.editor.settings.tab.grid"),
	EDITOR_SETTINGS_MINOR_GRID_COLOR("screen.another-hud-mod.editor.settings.grid.minor_color"),
	EDITOR_SETTINGS_MINOR_GRID_COLOR_DESCRIPTION(
		"screen.another-hud-mod.editor.settings.grid.minor_color.description"),
	EDITOR_SETTINGS_MAJOR_GRID_COLOR("screen.another-hud-mod.editor.settings.grid.major_color"),
	EDITOR_SETTINGS_MAJOR_GRID_COLOR_DESCRIPTION(
		"screen.another-hud-mod.editor.settings.grid.major_color.description"),
	EDITOR_SETTINGS_CENTER_GUIDE_COLOR("screen.another-hud-mod.editor.settings.grid.center_guide_color"),
	EDITOR_SETTINGS_CENTER_GUIDE_COLOR_DESCRIPTION(
		"screen.another-hud-mod.editor.settings.grid.center_guide_color.description"),
	POPOVER_NO_SETTINGS("popover.another-hud-mod.no_settings"),
	SETTINGS_TAB_LAYOUT("module.another-hud-mod.armor.settings.tab.layout"),
	SETTINGS_TAB_BAR("module.another-hud-mod.armor.settings.tab.bar"),
	SETTINGS_TAB_TEXT("module.another-hud-mod.armor.settings.tab.text"),
	SETTINGS_SECTION_LAYOUT("module.another-hud-mod.armor.settings.section.layout"),
	SETTINGS_SECTION_DURABILITY_BAR("module.another-hud-mod.armor.settings.section.durability_bar"),
	SETTINGS_SECTION_DURABILITY_TEXT("module.another-hud-mod.armor.settings.section.durability_text"),
	SETTINGS_SECTION_LOW_DURABILITY_WARNING(
		"module.another-hud-mod.armor.settings.section.low_durability_warning"),
	SETTINGS_SECTION_COLORS("module.another-hud-mod.armor.settings.section.colors"),
	SETTINGS_ARMOR_ORIENTATION("module.another-hud-mod.armor.settings.orientation"),
	SETTINGS_ARMOR_ORIENTATION_DESCRIPTION("module.another-hud-mod.armor.settings.orientation.description"),
	SETTINGS_ARMOR_SCALE("module.another-hud-mod.armor.settings.scale"),
	SETTINGS_ARMOR_SCALE_DESCRIPTION("module.another-hud-mod.armor.settings.scale.description"),
	SETTINGS_ARMOR_SPACING("module.another-hud-mod.armor.settings.spacing"),
	SETTINGS_ARMOR_SPACING_DESCRIPTION("module.another-hud-mod.armor.settings.spacing.description"),
	SETTINGS_ARMOR_SLOT_STYLE("module.another-hud-mod.armor.settings.slot_style"),
	SETTINGS_ARMOR_SLOT_STYLE_DESCRIPTION("module.another-hud-mod.armor.settings.slot_style.description"),
	SETTINGS_ARMOR_SHOW_EMPTY("module.another-hud-mod.armor.settings.show_empty"),
	SETTINGS_ARMOR_SHOW_EMPTY_DESCRIPTION("module.another-hud-mod.armor.settings.show_empty.description"),
	SETTINGS_ARMOR_CENTER_VISIBLE("module.another-hud-mod.armor.settings.center_visible"),
	SETTINGS_ARMOR_CENTER_VISIBLE_DESCRIPTION("module.another-hud-mod.armor.settings.center_visible.description"),
	SETTINGS_ARMOR_DURABILITY_BAR_VISIBLE("module.another-hud-mod.armor.settings.durability_bar.visible"),
	SETTINGS_ARMOR_DURABILITY_BAR_VISIBLE_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_bar.visible.description"),
	SETTINGS_ARMOR_DURABILITY_BAR_HEIGHT("module.another-hud-mod.armor.settings.durability_bar.height"),
	SETTINGS_ARMOR_DURABILITY_BAR_HEIGHT_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_bar.height.description"),
	SETTINGS_ARMOR_DURABILITY_BAR_PADDING("module.another-hud-mod.armor.settings.durability_bar.padding"),
	SETTINGS_ARMOR_DURABILITY_BAR_PADDING_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_bar.padding.description"),
	SETTINGS_ARMOR_DURABILITY_BAR_BACKGROUND_COLOR(
		"module.another-hud-mod.armor.settings.durability_bar.background_color"),
	SETTINGS_ARMOR_DURABILITY_BAR_BACKGROUND_COLOR_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_bar.background_color.description"),
	SETTINGS_ARMOR_DURABILITY_BAR_HEALTHY_COLOR(
		"module.another-hud-mod.armor.settings.durability_bar.healthy_color"),
	SETTINGS_ARMOR_DURABILITY_BAR_HEALTHY_COLOR_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_bar.healthy_color.description"),
	SETTINGS_ARMOR_DURABILITY_BAR_WARNING_COLOR(
		"module.another-hud-mod.armor.settings.durability_bar.warning_color"),
	SETTINGS_ARMOR_DURABILITY_BAR_WARNING_COLOR_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_bar.warning_color.description"),
	SETTINGS_ARMOR_DURABILITY_BAR_CRITICAL_COLOR(
		"module.another-hud-mod.armor.settings.durability_bar.critical_color"),
	SETTINGS_ARMOR_DURABILITY_BAR_CRITICAL_COLOR_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_bar.critical_color.description"),
	SETTINGS_ARMOR_DURABILITY_MODE("module.another-hud-mod.armor.settings.durability_text.mode"),
	SETTINGS_ARMOR_DURABILITY_MODE_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_text.mode.description"),
	SETTINGS_ARMOR_DURABILITY_TEXT_POSITION(
		"module.another-hud-mod.armor.settings.durability_text.position"),
	SETTINGS_ARMOR_DURABILITY_TEXT_POSITION_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_text.position.description"),
	SETTINGS_ARMOR_DURABILITY_TEXT_SCALE("module.another-hud-mod.armor.settings.durability_text.scale"),
	SETTINGS_ARMOR_DURABILITY_TEXT_SCALE_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_text.scale.description"),
	SETTINGS_ARMOR_DURABILITY_TEXT_SHADOW("module.another-hud-mod.armor.settings.durability_text.shadow"),
	SETTINGS_ARMOR_DURABILITY_TEXT_SHADOW_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_text.shadow.description"),
	SETTINGS_ARMOR_COLOR_BASED_DURABILITY_TEXT(
		"module.another-hud-mod.armor.settings.durability_text.color_based"),
	SETTINGS_ARMOR_COLOR_BASED_DURABILITY_TEXT_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_text.color_based.description"),
	SETTINGS_ARMOR_DURABILITY_TEXT_COLOR("module.another-hud-mod.armor.settings.durability_text.color"),
	SETTINGS_ARMOR_DURABILITY_TEXT_COLOR_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_text.color.description"),
	SETTINGS_ARMOR_DURABILITY_TEXT_HEALTHY_COLOR(
		"module.another-hud-mod.armor.settings.durability_text.healthy_color"),
	SETTINGS_ARMOR_DURABILITY_TEXT_HEALTHY_COLOR_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_text.healthy_color.description"),
	SETTINGS_ARMOR_DURABILITY_TEXT_WARNING_COLOR(
		"module.another-hud-mod.armor.settings.durability_text.warning_color"),
	SETTINGS_ARMOR_DURABILITY_TEXT_WARNING_COLOR_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_text.warning_color.description"),
	SETTINGS_ARMOR_DURABILITY_TEXT_CRITICAL_COLOR(
		"module.another-hud-mod.armor.settings.durability_text.critical_color"),
	SETTINGS_ARMOR_DURABILITY_TEXT_CRITICAL_COLOR_DESCRIPTION(
		"module.another-hud-mod.armor.settings.durability_text.critical_color.description"),
	SETTINGS_ARMOR_LOW_DURABILITY_WARNING_VISIBLE(
		"module.another-hud-mod.armor.settings.low_durability_warning.visible"),
	SETTINGS_ARMOR_LOW_DURABILITY_WARNING_VISIBLE_DESCRIPTION(
		"module.another-hud-mod.armor.settings.low_durability_warning.visible.description"),
	SETTINGS_ARMOR_LOW_DURABILITY_THRESHOLD(
		"module.another-hud-mod.armor.settings.low_durability_warning.threshold"),
	SETTINGS_ARMOR_LOW_DURABILITY_THRESHOLD_DESCRIPTION(
		"module.another-hud-mod.armor.settings.low_durability_warning.threshold.description"),
	SETTINGS_ARMOR_LOW_DURABILITY_WARNING_STYLE(
		"module.another-hud-mod.armor.settings.low_durability_warning.style"),
	SETTINGS_ARMOR_LOW_DURABILITY_WARNING_STYLE_DESCRIPTION(
		"module.another-hud-mod.armor.settings.low_durability_warning.style.description"),
	SETTINGS_ARMOR_LOW_DURABILITY_WARNING_COLOR(
		"module.another-hud-mod.armor.settings.low_durability_warning.color"),
	SETTINGS_ARMOR_LOW_DURABILITY_WARNING_COLOR_DESCRIPTION(
		"module.another-hud-mod.armor.settings.low_durability_warning.color.description"),
	COLOR_PICKER_ALPHA("popover.another-hud-mod.color_picker.alpha"),
	COLOR_PICKER_INPUT("popover.another-hud-mod.color_picker.input"),
	SETTINGS_VALUE_HORIZONTAL("option.another-hud-mod.horizontal"),
	SETTINGS_VALUE_VERTICAL("option.another-hud-mod.vertical"),
	SETTINGS_VALUE_CLEAR("option.another-hud-mod.clear"),
	SETTINGS_VALUE_INVENTORY("option.another-hud-mod.inventory"),
	SETTINGS_VALUE_HOTBAR("option.another-hud-mod.hotbar"),
	SETTINGS_VALUE_NONE("option.another-hud-mod.none"),
	SETTINGS_VALUE_VALUE("option.another-hud-mod.value"),
	SETTINGS_VALUE_PERCENT("option.another-hud-mod.percent"),
	SETTINGS_VALUE_TOP("option.another-hud-mod.top"),
	SETTINGS_VALUE_BOTTOM("option.another-hud-mod.bottom"),
	SETTINGS_VALUE_LEFT("option.another-hud-mod.left"),
	SETTINGS_VALUE_RIGHT("option.another-hud-mod.right"),
	SETTINGS_VALUE_CENTER("option.another-hud-mod.center"),
	SETTINGS_VALUE_COLOR("option.another-hud-mod.color"),
	SETTINGS_VALUE_PULSE("option.another-hud-mod.pulse"),
	SETTINGS_VALUE_FLASH("option.another-hud-mod.flash");

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
