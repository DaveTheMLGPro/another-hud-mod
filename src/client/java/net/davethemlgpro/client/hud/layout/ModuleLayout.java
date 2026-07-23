package net.davethemlgpro.client.hud.layout;

public class ModuleLayout {
	private HudAnchor anchor = HudAnchor.TOP_LEFT;
	private int offsetX;
	private int offsetY;

	public ModuleLayout(HudAnchor anchor, int offsetX, int offsetY) {
		this.anchor = anchor;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	public ModuleLayout() {
	}

	public void copyFrom(ModuleLayout source) {
		this.anchor = source.anchor;
		this.offsetX = source.offsetX;
		this.offsetY = source.offsetY;
	}

	public void validate() {
		if (anchor == null) {
			anchor = HudAnchor.TOP_LEFT;
		}
	}

	public HudAnchor getAnchor() {
		return anchor;
	}

	public void setAnchor(HudAnchor anchor) {
		this.anchor = anchor == null ? HudAnchor.TOP_LEFT : anchor;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public void setOffset(int offsetX, int offsetY) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	@Override
	public String toString() {
		return "ModuleLayout[anchor=" + anchor + ", offset=(" + offsetX + ", " + offsetY + ")]";
	}
}
