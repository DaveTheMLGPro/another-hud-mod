package net.davethemlgpro.client.module.armor;

import net.davethemlgpro.client.screen.popover.HudCycleControl;
import net.davethemlgpro.client.screen.popover.HudPopoverControl;
import net.davethemlgpro.client.screen.popover.HudSliderControl;
import net.davethemlgpro.client.screen.popover.HudToggleControl;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ArmorHudPopover {
	private ArmorHudPopover() {
	}

	public static List<HudPopoverControl> create(ArmorHudConfig config) {
		return List.of(
			new HudCycleControl<>(TranslationKey.SETTINGS_ARMOR_ORIENTATION.component(),
				TranslationKey.SETTINGS_ARMOR_ORIENTATION_DESCRIPTION.component(),
				List.of(ArmorHudOrientation.values()), config::getOrientation, config::setOrientation,
				ArmorHudPopover::orientationName),
			new HudSliderControl(TranslationKey.SETTINGS_ARMOR_SCALE.component(),
				TranslationKey.SETTINGS_ARMOR_SCALE_DESCRIPTION.component(),
				ArmorHudConfig.MIN_SCALE, ArmorHudConfig.MAX_SCALE, 0.05,
				config::getScale, value -> config.setScale((float) value),
				value -> Component.literal(Math.round(value * 100.0) + "%")),
			new HudSliderControl(TranslationKey.SETTINGS_ARMOR_SPACING.component(),
				TranslationKey.SETTINGS_ARMOR_SPACING_DESCRIPTION.component(),
				ArmorHudConfig.MIN_SPACING, ArmorHudConfig.MAX_SPACING, 1.0,
				config::getSpacing, value -> config.setSpacing((int) Math.round(value)),
				value -> Component.literal(Integer.toString((int) Math.round(value)))),
			new HudCycleControl<>(TranslationKey.SETTINGS_ARMOR_SLOT_STYLE.component(),
				TranslationKey.SETTINGS_ARMOR_SLOT_STYLE_DESCRIPTION.component(),
				List.of(ArmorHudSlotStyle.values()), config::getSlotStyle, config::setSlotStyle,
				ArmorHudPopover::slotStyleName),
			new HudToggleControl(TranslationKey.SETTINGS_ARMOR_SHOW_EMPTY.component(),
				TranslationKey.SETTINGS_ARMOR_SHOW_EMPTY_DESCRIPTION.component(),
				config::isShowEmptySlots, config::setShowEmptySlots),
			new HudToggleControl(TranslationKey.SETTINGS_ARMOR_CENTER_VISIBLE.component(),
				TranslationKey.SETTINGS_ARMOR_CENTER_VISIBLE_DESCRIPTION.component(),
				config::isCenterVisibleSlots, config::setCenterVisibleSlots)
		);
	}

	private static Component orientationName(ArmorHudOrientation orientation) {
		return switch (orientation) {
			case HORIZONTAL -> TranslationKey.SETTINGS_VALUE_HORIZONTAL.component();
			case VERTICAL -> TranslationKey.SETTINGS_VALUE_VERTICAL.component();
		};
	}

	private static Component slotStyleName(ArmorHudSlotStyle slotStyle) {
		return switch (slotStyle) {
			case CLEAR -> TranslationKey.SETTINGS_VALUE_CLEAR.component();
			case INVENTORY -> TranslationKey.SETTINGS_VALUE_INVENTORY.component();
			case HOTBAR -> TranslationKey.SETTINGS_VALUE_HOTBAR.component();
		};
	}
}
