package net.davethemlgpro.client.screen;

import net.davethemlgpro.client.config.EditorConfig;
import net.davethemlgpro.client.screen.popover.HudColorControl;
import net.davethemlgpro.client.screen.popover.HudPopoverTab;
import net.davethemlgpro.client.translation.TranslationKey;

import java.util.List;

final class EditorGridSettingsPopover {
	private EditorGridSettingsPopover() {
	}

	public static List<HudPopoverTab> create(EditorConfig config) {
		return List.of(new HudPopoverTab(TranslationKey.EDITOR_SETTINGS_TAB_GRID.component(), List.of(
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
		)));
	}
}
