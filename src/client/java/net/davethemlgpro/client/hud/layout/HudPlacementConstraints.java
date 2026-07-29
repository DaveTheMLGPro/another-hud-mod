package net.davethemlgpro.client.hud.layout;

import net.davethemlgpro.client.hud.HudBounds;

public final class HudPlacementConstraints {
	// Vanilla renders these dimensions in logical GUI pixels. Screen width and height use the
	// same scaled coordinate system, so this region follows both GUI scale and window resizing.
	public static final int VANILLA_HUD_WIDTH = 182;
	public static final int VANILLA_HUD_HEIGHT = 49;

	private HudPlacementConstraints() {
	}

	public static HudBounds vanillaHudRegion(int screenWidth, int screenHeight) {
		int regionWidth = Math.min(VANILLA_HUD_WIDTH, Math.max(0, screenWidth));
		int regionHeight = Math.min(VANILLA_HUD_HEIGHT, Math.max(0, screenHeight));
		return new HudBounds((screenWidth - regionWidth) / 2, screenHeight - regionHeight,
			regionWidth, regionHeight);
	}

	public static HudBounds avoid(HudBounds desired, HudBounds protectedRegion,
								  int screenWidth, int screenHeight, int gridSpacing) {
		if (gridSpacing <= 0) {
			throw new IllegalArgumentException("Grid spacing must be positive.");
		}

		HudBounds clamped = clampToScreen(desired, screenWidth, screenHeight);
		if (!clamped.intersects(protectedRegion)) {
			return clamped;
		}

		HudBounds best = null;
		long bestDistance = Long.MAX_VALUE;
		int maxX = Math.max(0, screenWidth - clamped.width());
		int maxY = Math.max(0, screenHeight - clamped.height());
		int[] candidateX = {
			alignDown(protectedRegion.x() - clamped.width(), gridSpacing),
			alignUp(protectedRegion.right(), gridSpacing),
			clamped.x(),
			clamped.x()
		};
		int[] candidateY = {
			clamped.y(),
			clamped.y(),
			alignDown(protectedRegion.y() - clamped.height(), gridSpacing),
			alignUp(protectedRegion.bottom(), gridSpacing)
		};

		for (int i = 0; i < candidateX.length; i++) {
			int x = candidateX[i];
			int y = candidateY[i];
			if (x < 0 || x > maxX || y < 0 || y > maxY) {
				continue;
			}
			HudBounds candidate = clamped.withPosition(x, y);
			if (candidate.intersects(protectedRegion)) {
				continue;
			}
			long deltaX = (long) x - clamped.x();
			long deltaY = (long) y - clamped.y();
			long distance = deltaX * deltaX + deltaY * deltaY;
			if (distance < bestDistance) {
				best = candidate;
				bestDistance = distance;
			}
		}
		return best == null ? clamped : best;
	}

	private static HudBounds clampToScreen(HudBounds bounds, int screenWidth, int screenHeight) {
		int x = Math.clamp(bounds.x(), 0, Math.max(0, screenWidth - bounds.width()));
		int y = Math.clamp(bounds.y(), 0, Math.max(0, screenHeight - bounds.height()));
		return bounds.withPosition(x, y);
	}

	private static int alignDown(int value, int spacing) {
		return Math.floorDiv(value, spacing) * spacing;
	}

	private static int alignUp(int value, int spacing) {
		return -Math.floorDiv(-value, spacing) * spacing;
	}
}
