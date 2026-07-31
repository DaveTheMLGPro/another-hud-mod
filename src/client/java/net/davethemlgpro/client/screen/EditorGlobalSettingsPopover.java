package net.davethemlgpro.client.screen;

import net.davethemlgpro.client.config.EditorConfig;
import net.davethemlgpro.client.config.HudConfigSnapshot;
import net.davethemlgpro.client.module.HudModuleEntry;
import net.davethemlgpro.client.module.HudModuleRegistry;
import net.davethemlgpro.client.screen.popover.HudActionControl;
import net.davethemlgpro.client.screen.popover.HudColorControl;
import net.davethemlgpro.client.screen.popover.HudPopoverControl;
import net.davethemlgpro.client.screen.popover.HudPopoverTab;
import net.davethemlgpro.client.screen.popover.HudSectionControl;
import net.davethemlgpro.client.screen.popover.HudToggleControl;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class EditorGlobalSettingsPopover {
	private static final int DESTRUCTIVE_TEXT_COLOR = 0xFFFF5555;
	private static final int DESTRUCTIVE_BORDER_COLOR = 0xFF883333;
	private static final int DESTRUCTIVE_HOVER_COLOR = 0x55331111;

	private EditorGlobalSettingsPopover() {
	}

	public static List<HudPopoverTab> create(EditorConfig config, HudConfigSnapshot draft,
											HudModuleRegistry registry, Consumer<Identifier> resetModule,
											Runnable resetAllModules) {
		return List.of(
			new HudPopoverTab(TranslationKey.EDITOR_SETTINGS_TAB_GENERAL.component(), List.of(
				new HudColorControl(
					TranslationKey.EDITOR_SETTINGS_ACCENT_COLOR.component(),
					TranslationKey.EDITOR_SETTINGS_ACCENT_COLOR_DESCRIPTION.component(),
					config::getAccentColor,
					config::setAccentColor,
					EditorConfig.DEFAULT_ACCENT_COLOR),
				new HudColorControl(
					TranslationKey.EDITOR_SETTINGS_MODULE_BORDER_COLOR.component(),
					TranslationKey.EDITOR_SETTINGS_MODULE_BORDER_COLOR_DESCRIPTION.component(),
					config::getSelectionColor,
					config::setSelectionColor,
					EditorConfig.DEFAULT_SELECTION_COLOR),
				new HudColorControl(
					TranslationKey.EDITOR_SETTINGS_MODULE_HIGHLIGHT_COLOR.component(),
					TranslationKey.EDITOR_SETTINGS_MODULE_HIGHLIGHT_COLOR_DESCRIPTION.component(),
					config::getHoveredSelectionColor,
					config::setHoveredSelectionColor,
					EditorConfig.DEFAULT_HOVERED_SELECTION_COLOR)
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
			)),
			new HudPopoverTab(TranslationKey.EDITOR_SETTINGS_TAB_MODULES.component(),
				moduleControls(draft, registry, resetModule, resetAllModules))
		);
	}

	private static List<HudPopoverControl> moduleControls(HudConfigSnapshot draft, HudModuleRegistry registry,
												 Consumer<Identifier> resetModule, Runnable resetAllModules) {
		List<HudPopoverControl> controls = new ArrayList<>();
		for (HudModuleEntry<?> entry : registry.getEntries()) {
			Identifier moduleId = entry.getModule().id();
			controls.add(new HudSectionControl(entry.getModule().displayName()));
			controls.add(new HudToggleControl(
				TranslationKey.EDITOR_SETTINGS_MODULE_ENABLED.component(entry.getModule().displayName()),
				TranslationKey.EDITOR_SETTINGS_MODULE_ENABLED_DESCRIPTION.component(entry.getModule().displayName()),
				() -> draft.isModuleEnabled(moduleId),
				enabled -> draft.setModuleEnabled(moduleId, enabled)));
			controls.add(new HudActionControl(
				TranslationKey.EDITOR_SETTINGS_MODULE_RESET.component(entry.getModule().displayName()),
				TranslationKey.EDITOR_SETTINGS_MODULE_RESET_DESCRIPTION.component(entry.getModule().displayName()),
				() -> resetModule.accept(moduleId),
				DESTRUCTIVE_TEXT_COLOR,
				DESTRUCTIVE_BORDER_COLOR,
				DESTRUCTIVE_HOVER_COLOR));
		}
		controls.add(new HudActionControl(
			TranslationKey.EDITOR_SETTINGS_MODULES_RESET_ALL.component(),
			TranslationKey.EDITOR_SETTINGS_MODULES_RESET_ALL_DESCRIPTION.component(),
			resetAllModules,
			DESTRUCTIVE_TEXT_COLOR,
			DESTRUCTIVE_BORDER_COLOR,
			DESTRUCTIVE_HOVER_COLOR));
		return List.copyOf(controls);
	}
}
