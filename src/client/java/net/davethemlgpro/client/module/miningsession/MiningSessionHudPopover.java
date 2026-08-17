package net.davethemlgpro.client.module.miningsession;

import net.davethemlgpro.client.AnotherHUDModClient;
import net.davethemlgpro.client.screen.popover.HudActionControl;
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

public final class MiningSessionHudPopover {
	private MiningSessionHudPopover() {
	}

	public static List<HudPopoverTab> create(MiningSessionHudConfig config) {
		return List.of(
			new HudPopoverTab(TranslationKey.SETTINGS_MINING_SESSION_TAB_SESSION.component(),
				sessionControls(config)),
			new HudPopoverTab(TranslationKey.SETTINGS_MINING_SESSION_TAB_APPEARANCE.component(),
				appearanceControls(config)),
			new HudPopoverTab(TranslationKey.SETTINGS_MINING_SESSION_TAB_INVENTORY.component(),
				inventoryControls(config))
		);
	}

	private static List<HudPopoverControl> inventoryControls(MiningSessionHudConfig config) {
		return List.of(
			new HudCycleControl<>(TranslationKey.SETTINGS_MINING_SESSION_INVENTORY_ORDER.component(),
				TranslationKey.SETTINGS_MINING_SESSION_INVENTORY_ORDER_DESCRIPTION.component(),
				List.of(MiningSessionInventoryOrder.values()), config::getInventoryOrder, config::setInventoryOrder,
				MiningSessionHudPopover::inventoryOrderName),
			new MiningSessionTrackedItemsControl(config)
		);
	}

	private static List<HudPopoverControl> sessionControls(MiningSessionHudConfig config) {
		MiningSessionHudModule module = AnotherHUDModClient.getMiningSessionHudModule();
		return List.of(
			new HudActionControl(TranslationKey.SETTINGS_MINING_SESSION_TOGGLE.component(),
				TranslationKey.SETTINGS_MINING_SESSION_TOGGLE_DESCRIPTION.component(),
				module::toggleSession, 0xFFFFFFFF, 0xFF55AA55, 0x4033AA33),
			new HudActionControl(TranslationKey.SETTINGS_MINING_SESSION_RESET.component(),
				TranslationKey.SETTINGS_MINING_SESSION_RESET_DESCRIPTION.component(),
				module::resetSession, 0xFFFF9999, 0xFFAA5555, 0x40AA3333),
			new HudToggleControl(TranslationKey.SETTINGS_MINING_SESSION_AUTO_START.component(),
				TranslationKey.SETTINGS_MINING_SESSION_AUTO_START_DESCRIPTION.component(),
				config::isAutoStartOnBlockBreak, config::setAutoStartOnBlockBreak),
			new HudToggleControl(TranslationKey.SETTINGS_MINING_SESSION_ORES_ONLY.component(),
				TranslationKey.SETTINGS_MINING_SESSION_ORES_ONLY_DESCRIPTION.component(),
				config::isOresOnly, config::setOresOnly),
			new HudSectionControl(TranslationKey.SETTINGS_MINING_SESSION_SECTION_GOAL.component()),
			new HudCycleControl<>(TranslationKey.SETTINGS_MINING_SESSION_GOAL_MODE.component(),
				TranslationKey.SETTINGS_MINING_SESSION_GOAL_MODE_DESCRIPTION.component(),
				List.of(MiningSessionGoalMode.values()), config::getGoalMode, config::setGoalMode,
				MiningSessionHudPopover::goalModeName),
			new HudConditionalControl(new HudSliderControl(
				TranslationKey.SETTINGS_MINING_SESSION_MAX_VISIBLE_GOALS.component(),
				TranslationKey.SETTINGS_MINING_SESSION_MAX_VISIBLE_GOALS_DESCRIPTION.component(),
				MiningSessionHudConfig.MIN_VISIBLE_GOALS, MiningSessionHudConfig.MAX_VISIBLE_GOALS, 1.0D,
				config::getMaxVisibleGoals, value -> config.setMaxVisibleGoals((int) Math.round(value)),
				value -> Component.literal(Integer.toString((int) Math.round(value)))),
				() -> config.getGoalMode() == MiningSessionGoalMode.ITEMS),
			new MiningSessionItemGoalsControl(config),
			new MiningSessionValueGoalControl(config)
		);
	}

	private static List<HudPopoverControl> appearanceControls(MiningSessionHudConfig config) {
		return List.of(
			new HudCycleControl<>(TranslationKey.SETTINGS_MINING_SESSION_ROW_STYLE.component(),
				TranslationKey.SETTINGS_MINING_SESSION_ROW_STYLE_DESCRIPTION.component(),
				List.of(MiningSessionRowStyle.values()), config::getRowStyle, config::setRowStyle,
				MiningSessionHudPopover::rowStyleName),
			new HudSliderControl(TranslationKey.SETTINGS_MINING_SESSION_MAX_BLOCKS.component(),
				TranslationKey.SETTINGS_MINING_SESSION_MAX_BLOCKS_DESCRIPTION.component(),
				MiningSessionHudConfig.MIN_VISIBLE_BLOCKS, MiningSessionHudConfig.MAX_VISIBLE_BLOCKS, 1.0D,
				config::getMaxVisibleBlocks,
				value -> config.setMaxVisibleBlocks((int) Math.round(value)),
				value -> Component.literal(Integer.toString((int) Math.round(value)))),
			new HudSliderControl(TranslationKey.SETTINGS_MINING_SESSION_UI_SCALE.component(),
				TranslationKey.SETTINGS_MINING_SESSION_UI_SCALE_DESCRIPTION.component(),
				MiningSessionHudConfig.MIN_UI_SCALE, MiningSessionHudConfig.MAX_UI_SCALE, 0.01D,
				config::getUiScale, config::setUiScale,
				value -> Component.literal(Math.round(value * 100.0D) + "%")),
			new HudSliderControl(TranslationKey.SETTINGS_MINING_SESSION_ROW_SPACING.component(),
				TranslationKey.SETTINGS_MINING_SESSION_ROW_SPACING_DESCRIPTION.component(),
				MiningSessionHudConfig.MIN_ROW_SPACING, MiningSessionHudConfig.MAX_ROW_SPACING, 1.0D,
				config::getRowSpacing, value -> config.setRowSpacing((int) Math.round(value)),
				value -> Component.literal(Integer.toString((int) Math.round(value)))),
			new HudSectionControl(TranslationKey.SETTINGS_MINING_SESSION_SECTION_STATISTICS.component()),
			new HudToggleControl(TranslationKey.SETTINGS_MINING_SESSION_SHOW_STATISTICS.component(),
				TranslationKey.SETTINGS_MINING_SESSION_SHOW_STATISTICS_DESCRIPTION.component(),
				config::isShowStatisticsFooter, config::setShowStatisticsFooter),
			new HudConditionalControl(new HudToggleControl(
				TranslationKey.SETTINGS_MINING_SESSION_SHOW_BLOCK_TOTAL.component(),
				TranslationKey.SETTINGS_MINING_SESSION_SHOW_BLOCK_TOTAL_DESCRIPTION.component(),
				config::isShowFooterBlocks, config::setShowFooterBlocks), config::isShowStatisticsFooter),
			new HudConditionalControl(new HudToggleControl(
				TranslationKey.SETTINGS_MINING_SESSION_SHOW_ITEM_TOTAL.component(),
				TranslationKey.SETTINGS_MINING_SESSION_SHOW_ITEM_TOTAL_DESCRIPTION.component(),
				config::isShowFooterItems, config::setShowFooterItems), config::isShowStatisticsFooter),
			new HudConditionalControl(new HudToggleControl(
				TranslationKey.SETTINGS_MINING_SESSION_SHOW_VALUE_TOTAL.component(),
				TranslationKey.SETTINGS_MINING_SESSION_SHOW_VALUE_TOTAL_DESCRIPTION.component(),
				config::isShowFooterValue, config::setShowFooterValue), config::isShowStatisticsFooter),
			new HudSectionControl(TranslationKey.SETTINGS_MINING_SESSION_SECTION_COLORS.component()),
			new HudColorControl(TranslationKey.SETTINGS_MINING_SESSION_BACKGROUND_COLOR.component(),
				TranslationKey.SETTINGS_MINING_SESSION_BACKGROUND_COLOR_DESCRIPTION.component(),
				config::getBackgroundColor, config::setBackgroundColor,
				MiningSessionHudConfig.DEFAULT_BACKGROUND_COLOR),
			new HudColorControl(TranslationKey.SETTINGS_MINING_SESSION_TEXT_COLOR.component(),
				TranslationKey.SETTINGS_MINING_SESSION_TEXT_COLOR_DESCRIPTION.component(),
				config::getTextColor, config::setTextColor, MiningSessionHudConfig.DEFAULT_TEXT_COLOR),
			new HudColorControl(TranslationKey.SETTINGS_MINING_SESSION_ACCENT_COLOR.component(),
				TranslationKey.SETTINGS_MINING_SESSION_ACCENT_COLOR_DESCRIPTION.component(),
				config::getAccentColor, config::setAccentColor, MiningSessionHudConfig.DEFAULT_ACCENT_COLOR)
		);
	}

	private static Component rowStyleName(MiningSessionRowStyle style) {
		return switch (style) {
			case ICON_AMOUNT -> TranslationKey.SETTINGS_VALUE_ICON_AMOUNT.component();
			case ICON_NAME_AMOUNT -> TranslationKey.SETTINGS_VALUE_ICON_NAME_AMOUNT.component();
		};
	}

	private static Component inventoryOrderName(MiningSessionInventoryOrder order) {
		return switch (order) {
			case CUSTOM -> TranslationKey.SETTINGS_VALUE_CUSTOM.component();
			case AMOUNT -> TranslationKey.SETTINGS_VALUE_AMOUNT.component();
			case CUSTOM_VALUE -> TranslationKey.SETTINGS_VALUE_CUSTOM_VALUE.component();
			case TOTAL_VALUE -> TranslationKey.SETTINGS_VALUE_TOTAL_VALUE.component();
			case ALPHABETICAL -> TranslationKey.SETTINGS_VALUE_ALPHABETICAL.component();
		};
	}

	private static Component goalModeName(MiningSessionGoalMode mode) {
		return switch (mode) {
			case NONE -> TranslationKey.SETTINGS_VALUE_NONE.component();
			case ITEMS -> TranslationKey.SETTINGS_VALUE_ITEMS.component();
			case VALUE -> TranslationKey.SETTINGS_VALUE_VALUE.component();
		};
	}
}
