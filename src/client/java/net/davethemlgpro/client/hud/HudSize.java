package net.davethemlgpro.client.hud;

public record HudSize(int width, int height) {
	public HudSize{
		if (width < 0 || height < 0) {
			throw new IllegalArgumentException("HUD size cannot be negative.");
		}
	}
}
