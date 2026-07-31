package net.davethemlgpro.client.module.itempickup;

import net.davethemlgpro.client.screen.popover.HudColorControl;
import net.davethemlgpro.client.screen.popover.HudConditionalControl;
import net.davethemlgpro.client.screen.popover.HudCycleControl;
import net.davethemlgpro.client.screen.popover.HudPopoverControl;
import net.davethemlgpro.client.screen.popover.HudPopoverTab;
import net.davethemlgpro.client.screen.popover.HudSectionControl;
import net.davethemlgpro.client.screen.popover.HudSliderControl;
import net.davethemlgpro.client.screen.popover.HudToggleControl;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ItemPickupHudPopover {
	private ItemPickupHudPopover() {
	}

	public static List<HudPopoverTab> create(ItemPickupHudConfig config) {
		return List.of(
			new HudPopoverTab(TranslationKey.SETTINGS_ITEM_PICKUP_TAB_APPEARANCE.component(),
				appearanceControls(config)),
			new HudPopoverTab(TranslationKey.SETTINGS_ITEM_PICKUP_TAB_BEHAVIOR.component(),
				behaviorControls(config)),
			new HudPopoverTab(TranslationKey.SETTINGS_ITEM_PICKUP_TAB_FILTER.component(),
				filterControls(config))
		);
	}

	private static List<HudPopoverControl> filterControls(ItemPickupHudConfig config) {
		return List.of(
			new HudCycleControl<>(TranslationKey.SETTINGS_ITEM_PICKUP_FILTER_MODE.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_FILTER_MODE_DESCRIPTION.component(),
				List.of(ItemPickupFilterMode.values()), config::getFilterMode, config::setFilterMode,
				ItemPickupHudPopover::filterModeName),
			new ItemPickupFilterListControl(config)
		);
	}

	private static List<HudPopoverControl> appearanceControls(ItemPickupHudConfig config) {
		return List.of(
			new HudCycleControl<>(TranslationKey.SETTINGS_ITEM_PICKUP_PRESENTATION.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_PRESENTATION_DESCRIPTION.component(),
				List.of(ItemPickupPresentation.values()), config::getPresentation, config::setPresentation,
				ItemPickupHudPopover::presentationName),
			new HudConditionalControl(new HudCycleControl<>(TranslationKey.SETTINGS_ITEM_PICKUP_STYLE.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_STYLE_DESCRIPTION.component(),
				List.of(ItemPickupHudStyle.values()), config::getStyle, config::setStyle,
				ItemPickupHudPopover::styleName),
				() -> config.getPresentation() == ItemPickupPresentation.LIST),
			new HudConditionalControl(new HudToggleControl(
				TranslationKey.SETTINGS_ITEM_PICKUP_SHOW_ITEM_ICON.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_SHOW_ITEM_ICON_DESCRIPTION.component(),
				config::isShowItemIcon, config::setShowItemIcon),
				() -> config.getPresentation() == ItemPickupPresentation.LIST
					&& config.getStyle() == ItemPickupHudStyle.NORMAL),
			new HudSectionControl(TranslationKey.SETTINGS_ITEM_PICKUP_SECTION_LAYOUT.component()),
			new HudConditionalControl(new HudToggleControl(
				TranslationKey.SETTINGS_ITEM_PICKUP_STABLE_WIDTH.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_STABLE_WIDTH_DESCRIPTION.component(),
				config::isStableWidth, config::setStableWidth),
				() -> config.getPresentation() == ItemPickupPresentation.LIST),
			new HudSliderControl(TranslationKey.SETTINGS_ITEM_PICKUP_UI_SCALE.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_UI_SCALE_DESCRIPTION.component(),
				ItemPickupHudConfig.MIN_UI_SCALE, ItemPickupHudConfig.MAX_UI_SCALE, 0.01D,
				config::getUiScale, config::setUiScale,
				value -> Component.literal(Math.round(value * 100.0D) + "%")),
			new HudSliderControl(TranslationKey.SETTINGS_ITEM_PICKUP_ROW_SPACING.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_ROW_SPACING_DESCRIPTION.component(),
				ItemPickupHudConfig.MIN_ROW_SPACING, ItemPickupHudConfig.MAX_ROW_SPACING, 1.0D,
				config::getRowSpacing, value -> config.setRowSpacing((int) Math.round(value)),
				value -> Component.literal(Integer.toString((int) Math.round(value)))),
			new HudCycleControl<>(TranslationKey.SETTINGS_ITEM_PICKUP_BACKGROUND_STYLE.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_BACKGROUND_STYLE_DESCRIPTION.component(),
				List.of(ItemPickupBackgroundStyle.values()), config::getBackgroundStyle,
				config::setBackgroundStyle, ItemPickupHudPopover::backgroundStyleName),
			new HudConditionalControl(new HudColorControl(
				TranslationKey.SETTINGS_ITEM_PICKUP_BACKGROUND_COLOR.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_BACKGROUND_COLOR_DESCRIPTION.component(),
				config::getBackgroundColor, config::setBackgroundColor,
				ItemPickupHudConfig.DEFAULT_BACKGROUND_COLOR),
				() -> config.getBackgroundStyle() != ItemPickupBackgroundStyle.CLEAR),
			new HudSectionControl(TranslationKey.SETTINGS_ITEM_PICKUP_SECTION_TEXT.component()),
			new HudCycleControl<>(TranslationKey.SETTINGS_ITEM_PICKUP_COUNT_FORMAT.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_COUNT_FORMAT_DESCRIPTION.component(),
				List.of(ItemPickupCountFormat.values()), config::getCountFormat, config::setCountFormat,
				ItemPickupHudPopover::countFormatName),
			new HudColorControl(TranslationKey.SETTINGS_ITEM_PICKUP_TEXT_COLOR.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_TEXT_COLOR_DESCRIPTION.component(),
				config::getTextColor, config::setTextColor, ItemPickupHudConfig.DEFAULT_TEXT_COLOR)
		);
	}

	private static List<HudPopoverControl> behaviorControls(ItemPickupHudConfig config) {
		return List.of(
			new HudConditionalControl(new HudCycleControl<>(
				TranslationKey.SETTINGS_ITEM_PICKUP_GROWTH_DIRECTION.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_GROWTH_DIRECTION_DESCRIPTION.component(),
				List.of(ItemPickupGrowthDirection.AUTO, ItemPickupGrowthDirection.UP,
					ItemPickupGrowthDirection.DOWN),
				config::getGrowthDirection, config::setGrowthDirection,
				ItemPickupHudPopover::growthDirectionName),
				() -> config.getPresentation() == ItemPickupPresentation.LIST),
			new HudConditionalControl(new HudCycleControl<>(
				TranslationKey.SETTINGS_ITEM_PICKUP_GROWTH_DIRECTION.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_GROWTH_DIRECTION_DESCRIPTION.component(),
				List.of(ItemPickupGrowthDirection.AUTO, ItemPickupGrowthDirection.LEFT,
					ItemPickupGrowthDirection.RIGHT),
				config::getGrowthDirection, config::setGrowthDirection,
				ItemPickupHudPopover::growthDirectionName),
				() -> config.getPresentation() == ItemPickupPresentation.CARDS),
			new HudSliderControl(TranslationKey.SETTINGS_ITEM_PICKUP_MAX_VISIBLE.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_MAX_VISIBLE_DESCRIPTION.component(),
				ItemPickupHudConfig.MIN_VISIBLE_ITEMS, ItemPickupHudConfig.MAX_VISIBLE_ITEMS, 1.0D,
				config::getMaxVisibleItems, value -> config.setMaxVisibleItems((int) Math.round(value)),
				value -> Component.literal(Integer.toString((int) Math.round(value)))),
			new HudSliderControl(TranslationKey.SETTINGS_ITEM_PICKUP_DISPLAY_TIME.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_DISPLAY_TIME_DESCRIPTION.component(),
				ItemPickupHudConfig.MIN_DISPLAY_TIME_SECONDS, ItemPickupHudConfig.MAX_DISPLAY_TIME_SECONDS, 0.5D,
				config::getDisplayTimeSeconds, config::setDisplayTimeSeconds,
				ItemPickupHudPopover::seconds),
			new HudSliderControl(TranslationKey.SETTINGS_ITEM_PICKUP_REMOVE_DELAY.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_REMOVE_DELAY_DESCRIPTION.component(),
				ItemPickupHudConfig.MIN_REMOVE_DELAY_SECONDS, ItemPickupHudConfig.MAX_REMOVE_DELAY_SECONDS, 0.1D,
				config::getRemoveDelaySeconds, config::setRemoveDelaySeconds,
				ItemPickupHudPopover::seconds),
			new HudCycleControl<>(TranslationKey.SETTINGS_ITEM_PICKUP_REMOVAL_MODE.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_REMOVAL_MODE_DESCRIPTION.component(),
				List.of(ItemPickupRemovalMode.values()), config::getRemovalMode, config::setRemovalMode,
				ItemPickupHudPopover::removalModeName),
			new HudConditionalControl(new HudSliderControl(
				TranslationKey.SETTINGS_ITEM_PICKUP_FADE_DURATION.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_FADE_DURATION_DESCRIPTION.component(),
				ItemPickupHudConfig.MIN_FADE_DURATION_SECONDS, ItemPickupHudConfig.MAX_FADE_DURATION_SECONDS, 0.1D,
				config::getFadeDurationSeconds, config::setFadeDurationSeconds,
				ItemPickupHudPopover::seconds),
				() -> config.getRemovalMode() == ItemPickupRemovalMode.FADE_OUT),
			new HudSliderControl(TranslationKey.SETTINGS_ITEM_PICKUP_MERGE_WINDOW.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_MERGE_WINDOW_DESCRIPTION.component(),
				ItemPickupHudConfig.MIN_MERGE_WINDOW_SECONDS, ItemPickupHudConfig.MAX_MERGE_WINDOW_SECONDS, 0.1D,
				config::getMergeWindowSeconds, config::setMergeWindowSeconds,
				ItemPickupHudPopover::seconds),
			new HudSectionControl(TranslationKey.SETTINGS_ITEM_PICKUP_SECTION_ANIMATION.component()),
			new HudCycleControl<>(TranslationKey.SETTINGS_ITEM_PICKUP_ENTRY_ANIMATION.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_ENTRY_ANIMATION_DESCRIPTION.component(),
				List.of(ItemPickupEntryAnimation.values()), config::getEntryAnimation,
				config::setEntryAnimation, ItemPickupHudPopover::entryAnimationName),
			new HudConditionalControl(new HudSliderControl(
				TranslationKey.SETTINGS_ITEM_PICKUP_ENTRY_ANIMATION_DURATION.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_ENTRY_ANIMATION_DURATION_DESCRIPTION.component(),
				ItemPickupHudConfig.MIN_ENTRY_ANIMATION_SECONDS,
				ItemPickupHudConfig.MAX_ENTRY_ANIMATION_SECONDS, 0.05D,
				config::getEntryAnimationSeconds, config::setEntryAnimationSeconds,
				ItemPickupHudPopover::seconds),
				() -> config.getEntryAnimation() == ItemPickupEntryAnimation.SLIDE),
			new HudCycleControl<>(TranslationKey.SETTINGS_ITEM_PICKUP_MERGE_FEEDBACK.component(),
				TranslationKey.SETTINGS_ITEM_PICKUP_MERGE_FEEDBACK_DESCRIPTION.component(),
				List.of(ItemPickupMergeFeedback.values()), config::getMergeFeedback,
				config::setMergeFeedback, ItemPickupHudPopover::mergeFeedbackName)
		);
	}

	private static Component presentationName(ItemPickupPresentation presentation) {
		return switch (presentation) {
			case LIST -> TranslationKey.SETTINGS_VALUE_LIST.component();
			case CARDS -> TranslationKey.SETTINGS_VALUE_CARDS.component();
		};
	}

	private static Component growthDirectionName(ItemPickupGrowthDirection direction) {
		return switch (direction) {
			case AUTO -> TranslationKey.SETTINGS_VALUE_AUTO.component();
			case UP -> TranslationKey.SETTINGS_VALUE_TOP.component();
			case DOWN -> TranslationKey.SETTINGS_VALUE_BOTTOM.component();
			case LEFT -> TranslationKey.SETTINGS_VALUE_LEFT.component();
			case RIGHT -> TranslationKey.SETTINGS_VALUE_RIGHT.component();
		};
	}

	private static Component entryAnimationName(ItemPickupEntryAnimation animation) {
		return switch (animation) {
			case INSTANT -> TranslationKey.SETTINGS_VALUE_INSTANT.component();
			case SLIDE -> TranslationKey.SETTINGS_VALUE_SLIDE.component();
		};
	}

	private static Component mergeFeedbackName(ItemPickupMergeFeedback feedback) {
		return switch (feedback) {
			case NONE -> TranslationKey.SETTINGS_VALUE_NONE.component();
			case PULSE -> TranslationKey.SETTINGS_VALUE_PULSE.component();
		};
	}

	private static Component filterModeName(ItemPickupFilterMode mode) {
		return switch (mode) {
			case SHOW_ALL -> TranslationKey.SETTINGS_VALUE_SHOW_ALL.component();
			case HIDE_LISTED -> TranslationKey.SETTINGS_VALUE_HIDE_LISTED.component();
			case ONLY_LISTED -> TranslationKey.SETTINGS_VALUE_ONLY_LISTED.component();
		};
	}

	private static Component seconds(double value) {
		return Component.literal((Math.round(value * 10.0D) / 10.0D) + "s");
	}

	private static Component styleName(ItemPickupHudStyle style) {
		return switch (style) {
			case NORMAL -> TranslationKey.SETTINGS_VALUE_NORMAL.component();
			case COMPACT -> TranslationKey.SETTINGS_VALUE_COMPACT.component();
		};
	}

	private static Component backgroundStyleName(ItemPickupBackgroundStyle style) {
		return switch (style) {
			case TINTED_ROWS -> TranslationKey.SETTINGS_VALUE_TINTED_ROWS.component();
			case CLEAR -> TranslationKey.SETTINGS_VALUE_BACKGROUND_CLEAR.component();
			case UNIFIED_PANEL -> TranslationKey.SETTINGS_VALUE_UNIFIED_PANEL.component();
		};
	}

	private static Component countFormatName(ItemPickupCountFormat format) {
		return switch (format) {
			case PLUS -> TranslationKey.SETTINGS_VALUE_PLUS_COUNT.component();
			case MULTIPLY -> TranslationKey.SETTINGS_VALUE_MULTIPLY_COUNT.component();
		};
	}

	private static Component removalModeName(ItemPickupRemovalMode mode) {
		return switch (mode) {
			case INSTANT -> TranslationKey.SETTINGS_VALUE_INSTANT.component();
			case FADE_OUT -> TranslationKey.SETTINGS_VALUE_FADE_OUT.component();
		};
	}
}
