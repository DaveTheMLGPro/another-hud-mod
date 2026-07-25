package net.davethemlgpro.client.screen;

import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.config.EditorConfig;
import net.davethemlgpro.client.hud.HudBounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

final class HudSelectionRenderer {
	public static final int ICON_SIZE = 16;
	private static final int SELECTION_PADDING = 4;
	private static final int CORNER_RADIUS = 3;
	private static final int ICON_CORNER_OVERLAP = 8;
	private static final Identifier VISIBLE_ICON = AnotherHUDMod.id("textures/visible_icon.png");
	private static final Identifier INVISIBLE_ICON = AnotherHUDMod.id("textures/invisible_icon.png");

	private HudSelectionRenderer() {
	}

	public static void render(GuiGraphicsExtractor graphics, HudBounds bounds, boolean enabled, boolean selected,
							  boolean hovered, EditorConfig colors, int mouseX, int mouseY, int screenWidth, int screenHeight) {
		int selectionX = bounds.x() - SELECTION_PADDING;
		int selectionY = bounds.y() - SELECTION_PADDING;
		int selectionWidth = bounds.width() + SELECTION_PADDING * 2;
		int selectionHeight = bounds.height() + SELECTION_PADDING * 2;
		if (!enabled) {
			drawRoundedFill(graphics, selectionX, selectionY, selectionWidth, selectionHeight,
				colors.getHiddenOverlayColor(), CORNER_RADIUS);
		}

		int outlineColor = hovered ? colors.getHoveredSelectionColor() : colors.getSelectionColor();
		drawRoundedOutline(graphics, selectionX, selectionY, selectionWidth, selectionHeight,
			outlineColor, CORNER_RADIUS);
		if (selected) {
			drawRoundedOutline(graphics, selectionX + 1, selectionY + 1, selectionWidth - 2, selectionHeight - 2,
				colors.getSelectionColor(), Math.max(0, CORNER_RADIUS - 1));
		}

		long buttonPosition = visibilityButtonPosition(bounds, screenWidth, screenHeight);
		int buttonX = unpackX(buttonPosition);
		int buttonY = unpackY(buttonPosition);
		if (contains(buttonX, buttonY, ICON_SIZE, ICON_SIZE, mouseX, mouseY)) {
			drawRoundedFill(graphics, buttonX, buttonY, ICON_SIZE, ICON_SIZE, 0x44BBBBBB, CORNER_RADIUS);
		}
		graphics.blit(RenderPipelines.GUI_TEXTURED, enabled ? VISIBLE_ICON : INVISIBLE_ICON,
			buttonX, buttonY, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
	}

	public static boolean containsSelection(HudBounds bounds, int x, int y) {
		return contains(bounds.x() - SELECTION_PADDING, bounds.y() - SELECTION_PADDING,
			bounds.width() + SELECTION_PADDING * 2, bounds.height() + SELECTION_PADDING * 2, x, y);
	}

	public static boolean containsVisibilityButton(HudBounds bounds, int x, int y,
												   int screenWidth, int screenHeight) {
		long position = visibilityButtonPosition(bounds, screenWidth, screenHeight);
		return contains(unpackX(position), unpackY(position), ICON_SIZE, ICON_SIZE, x, y);
	}

	private static long visibilityButtonPosition(HudBounds bounds, int screenWidth, int screenHeight) {
		int selectionX = bounds.x() - SELECTION_PADDING;
		int selectionY = bounds.y() - SELECTION_PADDING;
		int selectionRight = bounds.right() + SELECTION_PADDING;
		int selectionBottom = bounds.bottom() + SELECTION_PADDING;

		int x = selectionRight - ICON_CORNER_OVERLAP;
		int y = selectionY - ICON_SIZE + ICON_CORNER_OVERLAP;
		if (fitsScreen(x, y, screenWidth, screenHeight)) {
			return packPosition(x, y);
		}

		x = selectionX - ICON_SIZE + ICON_CORNER_OVERLAP;
		if (fitsScreen(x, y, screenWidth, screenHeight)) {
			return packPosition(x, y);
		}

		x = selectionRight - ICON_CORNER_OVERLAP;
		y = selectionBottom - ICON_CORNER_OVERLAP;
		if (fitsScreen(x, y, screenWidth, screenHeight)) {
			return packPosition(x, y);
		}

		x = selectionX - ICON_SIZE + ICON_CORNER_OVERLAP;
		if (fitsScreen(x, y, screenWidth, screenHeight)) {
			return packPosition(x, y);
		}

		x = Math.clamp(selectionRight - ICON_SIZE, 0, Math.max(0, screenWidth - ICON_SIZE));
		y = Math.clamp(selectionBottom, 0, Math.max(0, screenHeight - ICON_SIZE));
		return packPosition(x, y);
	}

	private static boolean fitsScreen(int x, int y, int screenWidth, int screenHeight) {
		return x >= 0 && y >= 0 && x + ICON_SIZE <= screenWidth && y + ICON_SIZE <= screenHeight;
	}

	private static boolean contains(int boundsX, int boundsY, int width, int height, int x, int y) {
		return x >= boundsX && x < boundsX + width && y >= boundsY && y < boundsY + height;
	}

	private static long packPosition(int x, int y) {
		return (long) x << 32 | y & 0xFFFFFFFFL;
	}

	private static int unpackX(long position) {
		return (int) (position >> 32);
	}

	private static int unpackY(long position) {
		return (int) position;
	}

	private static void drawRoundedOutline(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
										   int color, int cornerRadius) {
		int right = x + width;
		int bottom = y + height;
		int radius = Math.min(cornerRadius, Math.min(width, height) / 2);
		graphics.fill(x + radius, y, right - radius, y + 1, color);
		graphics.fill(x + radius, bottom - 1, right - radius, bottom, color);
		graphics.fill(x, y + radius, x + 1, bottom - radius, color);
		graphics.fill(right - 1, y + radius, right, bottom - radius, color);

		for (int offset = 1; offset < radius; offset++) {
			graphics.fill(x + offset, y + radius - offset, x + offset + 1, y + radius - offset + 1, color);
			graphics.fill(right - offset - 1, y + radius - offset, right - offset, y + radius - offset + 1, color);
			graphics.fill(x + offset, bottom - radius + offset - 1, x + offset + 1, bottom - radius + offset, color);
			graphics.fill(right - offset - 1, bottom - radius + offset - 1,
				right - offset, bottom - radius + offset, color);
		}
	}

	private static void drawRoundedFill(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
										int color, int cornerRadius) {
		int right = x + width;
		int bottom = y + height;
		int radius = Math.min(cornerRadius, Math.min(width, height) / 2);
		graphics.fill(x + radius, y, right - radius, bottom, color);
		graphics.fill(x, y + radius, right, bottom - radius, color);
		for (int offset = 1; offset < radius; offset++) {
			graphics.fill(x + offset, y + radius - offset, right - offset, bottom - radius + offset, color);
		}
	}
}
