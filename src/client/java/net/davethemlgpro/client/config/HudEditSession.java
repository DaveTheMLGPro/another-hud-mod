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

	public static HudEditSession begin(HudConfigManager manager) {
		if (active != null) {
			active.cancel();
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
		manager.applySnapshot(draft);
		boolean saved = manager.save();
		end();
		return saved;
	}

	public void cancel() {
		end();
	}

	private void end() {
		if (active == this) {
			active = null;
		}
	}
}
