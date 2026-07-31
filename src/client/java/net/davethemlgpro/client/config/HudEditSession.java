package net.davethemlgpro.client.config;

import net.minecraft.resources.Identifier;

public final class HudEditSession {
	private static HudEditSession active;

	private final HudConfigManager manager;
	private final HudConfigSnapshot openingState;
	private final HudConfigSnapshot draft;
	private final boolean editorPreview;

	private HudEditSession(HudConfigManager manager, boolean editorPreview) {
		this.manager = manager;
		this.editorPreview = editorPreview;
		openingState = manager.getSnapshot();
		draft = manager.getSnapshot();
	}

	public static HudEditSession beginEdit(HudConfigManager manager) {
		return beginEdit(manager, false);
	}

	public static HudEditSession beginEdit(HudConfigManager manager, boolean editorPreview) {
		if (active != null) {
			active.cancelEdit();
		}
		active = new HudEditSession(manager, editorPreview);
		return active;
	}

	public static HudEditSession getActive() {
		return active;
	}

	public HudConfigSnapshot getDraft() {
		return draft;
	}

	public boolean isEditorPreview() {
		return editorPreview;
	}

	public void resetToDefaults() {
		draft.copyFrom(manager.defaultSnapshot());
	}

	public void resetModuleToDefaults(Identifier moduleId) {
		HudConfigSnapshot defaults = manager.defaultSnapshot();
		draft.setConfig(moduleId, defaults.getRawConfig(moduleId));
		draft.setModuleEnabled(moduleId, defaults.isModuleEnabled(moduleId));
	}

	public void resetModulesToDefaults() {
		HudConfigSnapshot defaults = manager.defaultSnapshot();
		for (Identifier moduleId : defaults.getModuleIds()) {
			draft.setConfig(moduleId, defaults.getRawConfig(moduleId));
			draft.setModuleEnabled(moduleId, defaults.isModuleEnabled(moduleId));
		}
	}

	public void resetToOpeningState() {
		draft.copyFrom(openingState);
	}

	public boolean applyAndSave() {
		HudConfigSnapshot liveState = manager.getSnapshot();
		manager.applySnapshot(draft);
		if (!manager.save()) {
			manager.applySnapshot(liveState);
			return false;
		}
		endEdit();
		return true;
	}

	public void cancelEdit() {
		endEdit();
	}

	private void endEdit() {
		if (active == this) {
			active = null;
		}
	}
}
