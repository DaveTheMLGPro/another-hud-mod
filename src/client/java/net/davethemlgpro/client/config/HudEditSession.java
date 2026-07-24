package net.davethemlgpro.client.config;

public final class HudEditSession {
	private static HudEditSession active;

	private final HudConfigManager manager;
	private final HudConfigSnapshot openingState;
	private final HudConfigSnapshot draft;

	private HudEditSession(HudConfigManager manager) {
		this.manager = manager;
		openingState = manager.getSnapshot();
		draft = manager.getSnapshot();
	}

	public static HudEditSession beginEdit(HudConfigManager manager) {
		if (active != null) {
			active.cancelEdit();
		}
		active = new HudEditSession(manager);
		return active;
	}

	public static HudEditSession getActive() {
		return active;
	}

	public HudConfigSnapshot getDraft() {
		return draft;
	}

	public void resetToDefaults() {
		draft.copyFrom(manager.defaultSnapshot());
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
