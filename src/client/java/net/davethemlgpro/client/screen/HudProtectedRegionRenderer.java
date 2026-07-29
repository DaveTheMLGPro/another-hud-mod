package net.davethemlgpro.client.screen;

import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

final class HudProtectedRegionRenderer {
	private static final int FILL_COLOR = 0x22FF5555;
	private static final int OUTLINE_COLOR = 0xAAFF5555;

	private HudProtectedRegionRenderer() {
	}

	public static void render(GuiGraphicsExtractor graphics, Font font, HudBounds bounds) {
		graphics.fill(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), FILL_COLOR);
		graphics.outline(bounds.x(), bounds.y(), bounds.width(), bounds.height(), OUTLINE_COLOR);
		graphics.centeredText(font, TranslationKey.EDITOR_VANILLA_HUD_AREA.component(),
			bounds.x() + bounds.width() / 2, bounds.y() + 4, OUTLINE_COLOR);
	}
}
