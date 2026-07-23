package net.davethemlgpro.client.hud.layout;

import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudSize;

public class HudLayoutEngine {

	public HudLayoutEngine() {
	}

	public HudBounds resolve(ModuleLayout layout, HudSize size, int screenWidth, int screenHeight) {
		layout.checkAndRepair();

		int baseX = layout.getAnchor().baseX(screenWidth, size.width()) + layout.getOffsetX();
		int baseY = layout.getAnchor().baseY(screenHeight, size.height()) + layout.getOffsetY();

		int x = clampAxis(baseX, screenWidth, size.width());
		int y = clampAxis(baseY, screenHeight, size.height());

		return new HudBounds(x, y, size.width(), size.height());
	}

	public HudBounds applyDrag(ModuleLayout layout, HudSize size, int desiredX, int desiredY, int screenWidth, int screenHeight) {
		layout.checkAndRepair();
		int x = clampAxis(desiredX, screenWidth, size.width());
		int y = clampAxis(desiredY, screenHeight, size.height());

		layout.setOffset(
			x - layout.getAnchor().baseX(screenWidth, size.width()),
			y - layout.getAnchor().baseY(screenHeight, size.height()));

		return new HudBounds(x, y, size.width(),  size.height());
	}

	private int clampAxis(int desired, int screenSize, int contentSize) {
		return Math.clamp(desired, 0, Math.max(0, screenSize - contentSize));
	}
}
