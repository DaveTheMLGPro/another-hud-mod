package net.davethemlgpro.client.screen;

import net.davethemlgpro.client.config.EditorConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;

final class HudGridRenderer {
	public static final int MINOR_SPACING = 5;
	public static final int MAJOR_SPACING = 20;

	private HudGridRenderer() {
	}

	public static void render(GuiGraphicsExtractor graphics, int width, int editorBottom,
							  int screenCenterY, EditorConfig colors) {
		for (int x = 0; x < width; x += MINOR_SPACING) {
			int color = x % MAJOR_SPACING == 0
				? colors.getMajorGridColor() : colors.getMinorGridColor();
			graphics.fill(x, 0, x + 1, editorBottom, color);
		}
		for (int y = 0; y < editorBottom; y += MINOR_SPACING) {
			int color = y % MAJOR_SPACING == 0
				? colors.getMajorGridColor() : colors.getMinorGridColor();
			graphics.fill(0, y, width, y + 1, color);
		}

		int centerX = width / 2;
		graphics.fill(centerX, 0, centerX + 1, editorBottom, colors.getCenterGuideColor());
		if (screenCenterY < editorBottom) {
			graphics.fill(0, screenCenterY, width, screenCenterY + 1, colors.getCenterGuideColor());
		}
	}
}
