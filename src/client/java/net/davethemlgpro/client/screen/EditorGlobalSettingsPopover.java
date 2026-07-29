package net.davethemlgpro.client.screen;

import net.davethemlgpro.client.config.EditorConfig;
import net.davethemlgpro.client.screen.popover.HudActionControl;
import net.davethemlgpro.client.screen.popover.HudColorControl;
import net.davethemlgpro.client.screen.popover.HudPopoverTab;
import net.davethemlgpro.client.translation.TranslationKey;

import java.util.List;

final class EditorGlobalSettingsPopover {
	private static final int DESTRUCTIVE_TEXT_COLOR = 0xFFFF5555;
	private static final int DESTRUCTIVE_BORDER_COLOR = 0xFF883333;
	private static final int DESTRUCTIVE_HOVER_COLOR = 0x55331111;

	private EditorGlobalSettingsPopover() {
	}

	public static List<HudPopoverTab> create(EditorConfig config, Runnable resetAll) {
		return List.of(
			new HudPopoverTab(TranslationKey.EDITOR_SETTINGS_TAB_GENERAL.component(), List.of(
				new HudActionControl(
					TranslationKey.EDITOR_SETTINGS_RESET_ALL.component(),
					TranslationKey.EDITOR_SETTINGS_RESET_ALL_DESCRIPTION.component(),
					resetAll,
					DESTRUCTIVE_TEXT_COLOR,
					DESTRUCTIVE_BORDER_COLOR,
					DESTRUCTIVE_HOVER_COLOR)
			)),
			new HudPopoverTab(TranslationKey.EDITOR_SETTINGS_TAB_GRID.component(), List.of(
			new HudColorControl(
				TranslationKey.EDITOR_SETTINGS_MINOR_GRID_COLOR.component(),
				TranslationKey.EDITOR_SETTINGS_MINOR_GRID_COLOR_DESCRIPTION.component(),
				config::getMinorGridColor,
				config::setMinorGridColor,
				EditorConfig.DEFAULT_MINOR_GRID_COLOR),
			new HudColorControl(
				TranslationKey.EDITOR_SETTINGS_MAJOR_GRID_COLOR.component(),
				TranslationKey.EDITOR_SETTINGS_MAJOR_GRID_COLOR_DESCRIPTION.component(),
				config::getMajorGridColor,
				config::setMajorGridColor,
				EditorConfig.DEFAULT_MAJOR_GRID_COLOR),
			new HudColorControl(
				TranslationKey.EDITOR_SETTINGS_CENTER_GUIDE_COLOR.component(),
				TranslationKey.EDITOR_SETTINGS_CENTER_GUIDE_COLOR_DESCRIPTION.component(),
				config::getCenterGuideColor,
				config::setCenterGuideColor,
				EditorConfig.DEFAULT_CENTER_GUIDE_COLOR)
			))
		);
	}
}
