package net.davethemlgpro.client.module.containersearch;

import net.davethemlgpro.client.screen.popover.*;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ContainerSearchHudPopover {
	private ContainerSearchHudPopover() {
	}

	public List<HudPopoverTab> create(ContainerSearchHudConfig config) {
		return List.of(
			new HudPopoverTab(TranslationKey.SETTINGS_CONTAINER_SEARCH_TAB_GENERAL.component(), List.of(
				new HudSliderControl(TranslationKey.SETTINGS_CONTAINER_SEARCH_UI_SCALE.component(),
					TranslationKey.SETTINGS_CONTAINER_SEARCH_UI_SCALE_DESCRIPTION.component(),
					ContainerSearchHudConfig.MIN_UI_SCALE, ContainerSearchHudConfig.MAX_UI_SCALE, 0.05D,
					config::getUiScale, config::setUiScale,
					value -> Component.literal(Math.round(value * 100.0D) + "%")),
				new HudToggleControl(TranslationKey.SETTINGS_CONTAINER_SEARCH_DIM_NON_MATCHES.component(),
					TranslationKey.SETTINGS_CONTAINER_SEARCH_DIM_NON_MATCHES_DESCRIPTION.component(),
					config::isDimNonMatches, config::setDimNonMatches),
				new HudToggleControl(TranslationKey.SETTINGS_CONTAINER_SEARCH_CLEAR_ON_CLOSE.component(),
					TranslationKey.SETTINGS_CONTAINER_SEARCH_CLEAR_ON_CLOSE_DESCRIPTION.component(),
					config::isClearOnClose, config::setClearOnClose)
			)),
			new HudPopoverTab(TranslationKey.SETTINGS_CONTAINER_SEARCH_TAB_APPEARANCE.component(), List.of(
				new HudCycleControl<>(TranslationKey.SETTINGS_CONTAINER_SEARCH_HIGHLIGHT_STYLE.component(),
					TranslationKey.SETTINGS_CONTAINER_SEARCH_HIGHLIGHT_STYLE_DESCRIPTION.component(),
					List.of(ContainerSearchHighlightStyle.values()), config::getHighlightStyle,
					config::setHighlightStyle, ContainerSearchHudPopover::highlightStyleName),
				new HudColorControl(TranslationKey.SETTINGS_CONTAINER_SEARCH_BACKGROUND_COLOR.component(),
					TranslationKey.SETTINGS_CONTAINER_SEARCH_BACKGROUND_COLOR_DESCRIPTION.component(),
					config::getBackgroundColor, config::setBackgroundColor,
					ContainerSearchHudConfig.DEFAULT_BACKGROUND_COLOR),
				new HudColorControl(TranslationKey.SETTINGS_CONTAINER_SEARCH_TEXT_COLOR.component(),
					TranslationKey.SETTINGS_CONTAINER_SEARCH_TEXT_COLOR_DESCRIPTION.component(),
					config::getTextColor, config::setTextColor,
					ContainerSearchHudConfig.DEFAULT_TEXT_COLOR),
				new HudColorControl(TranslationKey.SETTINGS_CONTAINER_SEARCH_HIGHLIGHT_COLOR.component(),
					TranslationKey.SETTINGS_CONTAINER_SEARCH_HIGHLIGHT_COLOR_DESCRIPTION.component(),
					config::getHighlightColor, config::setHighlightColor,
					ContainerSearchHudConfig.DEFAULT_HIGHLIGHT_COLOR),
				new HudColorControl(TranslationKey.SETTINGS_CONTAINER_SEARCH_DIM_COLOR.component(),
					TranslationKey.SETTINGS_CONTAINER_SEARCH_DIM_COLOR_DESCRIPTION.component(),
					config::getDimColor, config::setDimColor,
					ContainerSearchHudConfig.DEFAULT_DIM_COLOR)
			))
		);
	}

	private static Component highlightStyleName(ContainerSearchHighlightStyle style) {
		return switch (style) {
			case OUTLINE -> TranslationKey.SETTINGS_VALUE_OUTLINE.component();
			case FILLED -> TranslationKey.SETTINGS_VALUE_FILLED.component();
			case PULSE -> TranslationKey.SETTINGS_VALUE_PULSE.component();
			case CORNERS -> TranslationKey.SETTINGS_VALUE_CORNERS.component();
		};
	}
}
