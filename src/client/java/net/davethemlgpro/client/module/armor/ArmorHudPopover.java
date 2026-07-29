package net.davethemlgpro.client.module.armor;

import net.davethemlgpro.client.screen.popover.HudConditionalControl;
import net.davethemlgpro.client.screen.popover.HudColorControl;
import net.davethemlgpro.client.screen.popover.HudCycleControl;
import net.davethemlgpro.client.screen.popover.HudPopoverControl;
import net.davethemlgpro.client.screen.popover.HudPopoverTab;
import net.davethemlgpro.client.screen.popover.HudSectionControl;
import net.davethemlgpro.client.screen.popover.HudSliderControl;
import net.davethemlgpro.client.screen.popover.HudToggleControl;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class ArmorHudPopover {
	private ArmorHudPopover() {
	}

	public static List<HudPopoverTab> create(ArmorHudConfig config) {
		return List.of(
			new HudPopoverTab(TranslationKey.SETTINGS_TAB_LAYOUT.component(), layoutControls(config)),
			new HudPopoverTab(TranslationKey.SETTINGS_TAB_BAR.component(), durabilityBarControls(config)),
			new HudPopoverTab(TranslationKey.SETTINGS_TAB_TEXT.component(), durabilityTextControls(config))
		);
	}

	private static List<HudPopoverControl> layoutControls(ArmorHudConfig config) {
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

	private static List<HudPopoverControl> durabilityBarControls(ArmorHudConfig config) {
		return List.of(
			new HudToggleControl(TranslationKey.SETTINGS_ARMOR_DURABILITY_BAR_VISIBLE.component(),
				TranslationKey.SETTINGS_ARMOR_DURABILITY_BAR_VISIBLE_DESCRIPTION.component(),
				config::isDurabilityBarVisible, config::setDurabilityBarVisible),
			durabilityBarHeight(config),
			durabilityBarPadding(config),
			conditionalSection(TranslationKey.SETTINGS_SECTION_COLORS, config::isDurabilityBarVisible),
			durabilityBarColor(config, TranslationKey.SETTINGS_ARMOR_DURABILITY_BAR_BACKGROUND_COLOR,
				TranslationKey.SETTINGS_ARMOR_DURABILITY_BAR_BACKGROUND_COLOR_DESCRIPTION, config::getDurabilityBackgroundColor,
				config::setDurabilityBackgroundColor, ArmorHudConfig.DEFAULT_DURABILITY_BACKGROUND_COLOR),
			durabilityBarColor(config, TranslationKey.SETTINGS_ARMOR_DURABILITY_BAR_HEALTHY_COLOR,
				TranslationKey.SETTINGS_ARMOR_DURABILITY_BAR_HEALTHY_COLOR_DESCRIPTION, config::getDurabilityHealthyColor,
				config::setDurabilityHealthyColor, ArmorHudConfig.DEFAULT_DURABILITY_HEALTHY_COLOR),
			durabilityBarColor(config, TranslationKey.SETTINGS_ARMOR_DURABILITY_BAR_WARNING_COLOR,
				TranslationKey.SETTINGS_ARMOR_DURABILITY_BAR_WARNING_COLOR_DESCRIPTION, config::getDurabilityWarningColor,
				config::setDurabilityWarningColor, ArmorHudConfig.DEFAULT_DURABILITY_WARNING_COLOR),
			durabilityBarColor(config, TranslationKey.SETTINGS_ARMOR_DURABILITY_BAR_CRITICAL_COLOR,
				TranslationKey.SETTINGS_ARMOR_DURABILITY_BAR_CRITICAL_COLOR_DESCRIPTION, config::getDurabilityCriticalColor,
				config::setDurabilityCriticalColor, ArmorHudConfig.DEFAULT_DURABILITY_CRITICAL_COLOR)
		);
	}

	private static List<HudPopoverControl> durabilityTextControls(ArmorHudConfig config) {
		return List.of(
			new HudCycleControl<>(TranslationKey.SETTINGS_ARMOR_DURABILITY_MODE.component(),
				TranslationKey.SETTINGS_ARMOR_DURABILITY_MODE_DESCRIPTION.component(),
				List.of(ArmorHudDurabilityMode.values()), config::getDurabilityMode, config::setDurabilityMode,
				ArmorHudPopover::durabilityModeName),
			durabilityTextPosition(config),
			durabilityTextScale(config),
			durabilityTextShadow(config),
			colorBasedDurabilityText(config),
			conditionalSection(TranslationKey.SETTINGS_SECTION_COLORS,
				() -> config.getDurabilityMode() != ArmorHudDurabilityMode.NONE),
			fixedDurabilityTextColor(config),
			durabilityTextColor(config, TranslationKey.SETTINGS_ARMOR_DURABILITY_TEXT_HEALTHY_COLOR,
				TranslationKey.SETTINGS_ARMOR_DURABILITY_TEXT_HEALTHY_COLOR_DESCRIPTION, config::getTextHealthyColor,
				config::setTextHealthyColor, ArmorHudConfig.DEFAULT_TEXT_HEALTHY_COLOR),
			durabilityTextColor(config, TranslationKey.SETTINGS_ARMOR_DURABILITY_TEXT_WARNING_COLOR,
				TranslationKey.SETTINGS_ARMOR_DURABILITY_TEXT_WARNING_COLOR_DESCRIPTION, config::getTextWarningColor,
				config::setTextWarningColor, ArmorHudConfig.DEFAULT_TEXT_WARNING_COLOR),
			durabilityTextColor(config, TranslationKey.SETTINGS_ARMOR_DURABILITY_TEXT_CRITICAL_COLOR,
				TranslationKey.SETTINGS_ARMOR_DURABILITY_TEXT_CRITICAL_COLOR_DESCRIPTION, config::getTextCriticalColor,
				config::setTextCriticalColor, ArmorHudConfig.DEFAULT_TEXT_CRITICAL_COLOR),
			new HudSectionControl(TranslationKey.SETTINGS_SECTION_LOW_DURABILITY_WARNING.component()),
			new HudToggleControl(TranslationKey.SETTINGS_ARMOR_LOW_DURABILITY_WARNING_VISIBLE.component(),
				TranslationKey.SETTINGS_ARMOR_LOW_DURABILITY_WARNING_VISIBLE_DESCRIPTION.component(),
				config::isLowDurabilityWarningEnabled, config::setLowDurabilityWarningEnabled),
			lowDurabilityThreshold(config),
			lowDurabilityWarningStyle(config),
			lowDurabilityWarningColor(config)
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

	private static Component durabilityModeName(ArmorHudDurabilityMode mode) {
		return switch (mode) {
			case NONE -> TranslationKey.SETTINGS_VALUE_NONE.component();
			case VALUE -> TranslationKey.SETTINGS_VALUE_VALUE.component();
			case PERCENT -> TranslationKey.SETTINGS_VALUE_PERCENT.component();
		};
	}

	private static Component textPositionName(ArmorHudTextPosition position) {
		return switch (position) {
			case TOP -> TranslationKey.SETTINGS_VALUE_TOP.component();
			case BOTTOM -> TranslationKey.SETTINGS_VALUE_BOTTOM.component();
			case LEFT -> TranslationKey.SETTINGS_VALUE_LEFT.component();
			case RIGHT -> TranslationKey.SETTINGS_VALUE_RIGHT.component();
			case CENTER -> TranslationKey.SETTINGS_VALUE_CENTER.component();
		};
	}

	private static Component warningStyleName(ArmorHudWarningStyle style) {
		return switch (style) {
			case COLOR -> TranslationKey.SETTINGS_VALUE_COLOR.component();
			case PULSE -> TranslationKey.SETTINGS_VALUE_PULSE.component();
			case FLASH -> TranslationKey.SETTINGS_VALUE_FLASH.component();
		};
	}

	private static HudPopoverControl durabilityBarHeight(ArmorHudConfig config) {
		HudSliderControl control = new HudSliderControl(TranslationKey.SETTINGS_ARMOR_DURABILITY_BAR_HEIGHT.component(),
			TranslationKey.SETTINGS_ARMOR_DURABILITY_BAR_HEIGHT_DESCRIPTION.component(),
			ArmorHudConfig.MIN_DURABILITY_BAR_HEIGHT, ArmorHudConfig.MAX_DURABILITY_BAR_HEIGHT, 1.0,
			config::getDurabilityBarHeight, value -> config.setDurabilityBarHeight((int) Math.round(value)),
			value -> Component.literal(Integer.toString((int) Math.round(value))));
		return new HudConditionalControl(control, config::isDurabilityBarVisible);
	}

	private static HudPopoverControl conditionalSection(TranslationKey title, BooleanSupplier visible) {
		return new HudConditionalControl(new HudSectionControl(title.component()), visible);
	}

	private static HudPopoverControl durabilityBarColor(ArmorHudConfig config, TranslationKey label,
														TranslationKey description, IntSupplier getter,
														IntConsumer setter, int defaultColor) {
		HudColorControl control = new HudColorControl(label.component(), description.component(), getter, setter,
			defaultColor);
		return new HudConditionalControl(control, config::isDurabilityBarVisible);
	}

	private static HudPopoverControl durabilityBarPadding(ArmorHudConfig config) {
		HudSliderControl control = new HudSliderControl(TranslationKey.SETTINGS_ARMOR_DURABILITY_BAR_PADDING.component(),
			TranslationKey.SETTINGS_ARMOR_DURABILITY_BAR_PADDING_DESCRIPTION.component(), 0.0,
			ArmorHudConfig.MAX_DURABILITY_BAR_HORIZONTAL_PADDING, 0.5, config::getDurabilityBarHorizontalPadding,
			value -> config.setDurabilityBarHorizontalPadding((float) value), ArmorHudPopover::decimalValue);
		return new HudConditionalControl(control, config::isDurabilityBarVisible);
	}

	private static HudPopoverControl durabilityTextPosition(ArmorHudConfig config) {
		HudCycleControl<ArmorHudTextPosition> control = new HudCycleControl<>(
			TranslationKey.SETTINGS_ARMOR_DURABILITY_TEXT_POSITION.component(),
			TranslationKey.SETTINGS_ARMOR_DURABILITY_TEXT_POSITION_DESCRIPTION.component(),
			List.of(ArmorHudTextPosition.values()), config::getTextPosition, config::setTextPosition,
			ArmorHudPopover::textPositionName);
		return new HudConditionalControl(control, () -> config.getDurabilityMode() != ArmorHudDurabilityMode.NONE);
	}

	private static HudPopoverControl durabilityTextScale(ArmorHudConfig config) {
		HudSliderControl control = new HudSliderControl(
			TranslationKey.SETTINGS_ARMOR_DURABILITY_TEXT_SCALE.component(),
			TranslationKey.SETTINGS_ARMOR_DURABILITY_TEXT_SCALE_DESCRIPTION.component(),
			ArmorHudConfig.MIN_DURABILITY_TEXT_SCALE, ArmorHudConfig.MAX_DURABILITY_TEXT_SCALE, 0.05,
			config::getDurabilityTextScale, value -> config.setDurabilityTextScale((float) value),
			value -> Component.literal(Math.round(value * 100.0) + "%"));
		return new HudConditionalControl(control, () -> config.getDurabilityMode() != ArmorHudDurabilityMode.NONE);
	}

	private static HudPopoverControl durabilityTextShadow(ArmorHudConfig config) {
		HudToggleControl control = new HudToggleControl(
			TranslationKey.SETTINGS_ARMOR_DURABILITY_TEXT_SHADOW.component(),
			TranslationKey.SETTINGS_ARMOR_DURABILITY_TEXT_SHADOW_DESCRIPTION.component(),
			config::isDurabilityTextShadow, config::setDurabilityTextShadow);
		return new HudConditionalControl(control, () -> config.getDurabilityMode() != ArmorHudDurabilityMode.NONE);
	}

	private static HudPopoverControl colorBasedDurabilityText(ArmorHudConfig config) {
		HudToggleControl control = new HudToggleControl(
			TranslationKey.SETTINGS_ARMOR_COLOR_BASED_DURABILITY_TEXT.component(),
			TranslationKey.SETTINGS_ARMOR_COLOR_BASED_DURABILITY_TEXT_DESCRIPTION.component(),
			config::isColorBasedDurabilityText, config::setColorBasedDurabilityText);
		return new HudConditionalControl(control, () -> config.getDurabilityMode() != ArmorHudDurabilityMode.NONE);
	}

	private static HudPopoverControl fixedDurabilityTextColor(ArmorHudConfig config) {
		HudColorControl control = new HudColorControl(TranslationKey.SETTINGS_ARMOR_DURABILITY_TEXT_COLOR.component(),
			TranslationKey.SETTINGS_ARMOR_DURABILITY_TEXT_COLOR_DESCRIPTION.component(),
			config::getDurabilityTextColor, config::setDurabilityTextColor,
			ArmorHudConfig.DEFAULT_DURABILITY_TEXT_COLOR);
		return new HudConditionalControl(control, () -> config.getDurabilityMode() != ArmorHudDurabilityMode.NONE
			&& !config.isColorBasedDurabilityText());
	}

	private static HudPopoverControl durabilityTextColor(ArmorHudConfig config, TranslationKey label,
														 TranslationKey description, IntSupplier getter,
														 IntConsumer setter, int defaultColor) {
		HudColorControl control = new HudColorControl(label.component(), description.component(), getter, setter,
			defaultColor);
		return new HudConditionalControl(control, () -> config.getDurabilityMode() != ArmorHudDurabilityMode.NONE
			&& config.isColorBasedDurabilityText());
	}

	private static HudPopoverControl lowDurabilityThreshold(ArmorHudConfig config) {
		HudSliderControl control = new HudSliderControl(
			TranslationKey.SETTINGS_ARMOR_LOW_DURABILITY_THRESHOLD.component(),
			TranslationKey.SETTINGS_ARMOR_LOW_DURABILITY_THRESHOLD_DESCRIPTION.component(),
			ArmorHudConfig.MIN_LOW_DURABILITY_THRESHOLD_PERCENT,
			ArmorHudConfig.MAX_LOW_DURABILITY_THRESHOLD_PERCENT, 1.0,
			config::getLowDurabilityThresholdPercent,
			value -> config.setLowDurabilityThresholdPercent((int) Math.round(value)),
			value -> Component.literal(Math.round(value) + "%"));
		return new HudConditionalControl(control, config::isLowDurabilityWarningEnabled);
	}

	private static HudPopoverControl lowDurabilityWarningStyle(ArmorHudConfig config) {
		HudCycleControl<ArmorHudWarningStyle> control = new HudCycleControl<>(
			TranslationKey.SETTINGS_ARMOR_LOW_DURABILITY_WARNING_STYLE.component(),
			TranslationKey.SETTINGS_ARMOR_LOW_DURABILITY_WARNING_STYLE_DESCRIPTION.component(),
			List.of(ArmorHudWarningStyle.values()), config::getWarningStyle, config::setWarningStyle,
			ArmorHudPopover::warningStyleName);
		return new HudConditionalControl(control, config::isLowDurabilityWarningEnabled);
	}

	private static HudPopoverControl lowDurabilityWarningColor(ArmorHudConfig config) {
		HudColorControl control = new HudColorControl(
			TranslationKey.SETTINGS_ARMOR_LOW_DURABILITY_WARNING_COLOR.component(),
			TranslationKey.SETTINGS_ARMOR_LOW_DURABILITY_WARNING_COLOR_DESCRIPTION.component(),
			config::getLowDurabilityWarningColor, config::setLowDurabilityWarningColor,
			ArmorHudConfig.DEFAULT_LOW_DURABILITY_WARNING_COLOR);
		return new HudConditionalControl(control, config::isLowDurabilityWarningEnabled);
	}

	private static Component decimalValue(double value) {
		long rounded = Math.round(value);
		return Component.literal(Math.abs(value - rounded) < 0.001
			? Long.toString(rounded) : Double.toString(Math.round(value * 10.0) / 10.0));
	}
}
