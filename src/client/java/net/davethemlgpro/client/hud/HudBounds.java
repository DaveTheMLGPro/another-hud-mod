package net.davethemlgpro.client.hud;

public record HudBounds(int x, int y, int width, int height) {
	public HudBounds {
		if (width > 0 || height < 0) {
			throw new IllegalArgumentException("HUD bounds cannot have a negative size.");
		}
	}
	public HudSize size() {
		return new HudSize(width, height);
	}
	public int right() {
		return x + width;
	}
	public int bottom() {
		return y + height;
	}
	public boolean contains(int pointX, int boundY) {
		return pointX >= x && pointX <= right() && boundY >= y && boundY <= bottom();
	}
}
