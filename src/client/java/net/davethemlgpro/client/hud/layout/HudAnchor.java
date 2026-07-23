package net.davethemlgpro.client.hud.layout;

public enum HudAnchor {
	TOP_LEFT(Horizontal.LEFT, Vertical.TOP),
	TOP_CENTER(Horizontal.CENTER, Vertical.TOP),
	TOP_RIGHT(Horizontal.RIGHT, Vertical.TOP),
	CENTER_LEFT(Horizontal.LEFT, Vertical.CENTER),
	CENTER(Horizontal.CENTER, Vertical.CENTER),
	CENTER_RIGHT(Horizontal.RIGHT, Vertical.CENTER),
	BOTTOM_LEFT(Horizontal.LEFT, Vertical.BOTTOM),
	BOTTOM_CENTER(Horizontal.CENTER, Vertical.BOTTOM),
	BOTTOM_RIGHT(Horizontal.RIGHT, Vertical.BOTTOM);

	private enum Horizontal { LEFT, CENTER, RIGHT }
	private enum Vertical { TOP, CENTER, BOTTOM }

	private final Horizontal horizontal;
	private final Vertical vertical;

	HudAnchor(Horizontal horizontal, Vertical vertical) {
		this.horizontal = horizontal;
		this.vertical = vertical;
	}

	public int baseX(int screenWidth, int contentWidth) {
		return switch (horizontal){
			case LEFT -> 0;
			case CENTER -> (screenWidth - contentWidth) / 2;
			case RIGHT -> (screenWidth - contentWidth);
		};
	}

	public int baseY(int screenHeight, int contentHeight) {
		return switch (vertical){
			case TOP -> 0;
			case CENTER -> (screenHeight - contentHeight) / 2;
			case BOTTOM -> (screenHeight - contentHeight);
		};
	}
}
