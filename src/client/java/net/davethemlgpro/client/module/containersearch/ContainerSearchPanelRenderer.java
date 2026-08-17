package net.davethemlgpro.client.module.containersearch;

import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

final class ContainerSearchPanelRenderer {
	private static final Identifier SEARCH_ICON = AnotherHUDMod.id("textures/search_icon.png");
	private static final int SEARCH_ICON_SIZE = 16;
	static final int INPUT_X = 8;
	static final int INPUT_Y = 25;
	static final int INPUT_HEIGHT = 20;
	static final int INPUT_WIDTH = ContainerSearchHudModule.PANEL_WIDTH - 16;
	static final int CLEAR_SIZE = 16;
	static final int TOGGLE_WIDTH = 28;
	static final int TOGGLE_HEIGHT = 14;
	static final int TOGGLE_Y = 80;
	static final int EXACT_TOGGLE_Y = 102;

	private ContainerSearchPanelRenderer() {
	}

	static void renderPanel(GuiGraphicsExtractor graphics, Minecraft minecraft, ContainerSearchHudConfig config,
							HudBounds bounds, Component query, Component status, boolean focusedInput,
							int accentColor, boolean clearHovered, boolean clearPressed) {
		int x = bounds.x();
		int y = bounds.y();
		int right = x + bounds.width();
		int bottom = y + bounds.height();
		graphics.fill(x, y, right, bottom, config.getBackgroundColor());
		graphics.outline(x, y, bounds.width(), bounds.height(), 0xFF5A5A5A);
		graphics.blit(RenderPipelines.GUI_TEXTURED, SEARCH_ICON, x + 8, y + 4, 0.0F, 0.0F,
			SEARCH_ICON_SIZE, SEARCH_ICON_SIZE, SEARCH_ICON_SIZE, SEARCH_ICON_SIZE);
		graphics.text(minecraft.font, TranslationKey.CONTAINER_SEARCH_TITLE.component(), x + 29, y + 8,
			config.getTextColor());

		int inputX = x + INPUT_X;
		int inputY = y + INPUT_Y;
		int inputRight = inputX + INPUT_WIDTH;
		graphics.fill(inputX, inputY, inputRight, inputY + INPUT_HEIGHT, 0xE6080808);
		graphics.outline(inputX, inputY, INPUT_WIDTH, INPUT_HEIGHT,
			focusedInput ? config.getHighlightColor() : 0xFF666666);
		graphics.text(minecraft.font, query, inputX + 5, inputY + 6, config.getTextColor());
		graphics.text(minecraft.font, Component.literal("×"), inputRight - 13, inputY + 5,
			clearButtonColor(clearHovered, clearPressed));
		graphics.text(minecraft.font, status, x + 8, y + 56, 0xFFB8B8B8);
		graphics.fill(x + 8, y + 72, right - 8, y + 73, 0x35FFFFFF);
		graphics.text(minecraft.font, TranslationKey.CONTAINER_SEARCH_DIM_SLOTS.component(), x + 8, y + 83,
			config.getTextColor());
		renderToggle(graphics, bounds, TOGGLE_Y, config.isDimNonMatches(), accentColor);
		graphics.text(minecraft.font, TranslationKey.CONTAINER_SEARCH_EXACT_MATCH.component(), x + 8, y + 105,
			config.getTextColor());
		renderToggle(graphics, bounds, EXACT_TOGGLE_Y, config.isExactMatch(), accentColor);
	}

	private static void renderToggle(GuiGraphicsExtractor graphics, HudBounds bounds, int offsetY, boolean enabled,
								 int accentColor) {
		int x = bounds.right() - TOGGLE_WIDTH - 8;
		int y = bounds.y() + offsetY;
		int trackColor = enabled ? 0xFF000000 | (accentColor & 0x00FFFFFF) : 0xFF4C4C4C;
		graphics.fill(x + 2, y, x + TOGGLE_WIDTH - 2, y + TOGGLE_HEIGHT, trackColor);
		graphics.fill(x, y + 2, x + TOGGLE_WIDTH, y + TOGGLE_HEIGHT - 2, trackColor);
		int knobX = enabled ? x + TOGGLE_WIDTH - 12 : x + 2;
		graphics.fill(knobX, y + 2, knobX + 10, y + 12, 0xFFF0F0F0);
	}

	static int clearButtonColor(boolean hovered, boolean pressed) {
		if (pressed) {
			return 0xFFCC2222;
		}
		return hovered ? 0xFFFF5555 : 0xFFAAAAAA;
	}

	static boolean inputContains(HudBounds panel, double mouseX, double mouseY) {
		int left = panel.x() + INPUT_X;
		int top = panel.y() + INPUT_Y;
		int right = left + INPUT_WIDTH - CLEAR_SIZE;
		return mouseX >= left && mouseX < right
			&& mouseY >= top && mouseY < top + INPUT_HEIGHT;
	}

	static boolean clearButtonContains(HudBounds panel, double mouseX, double mouseY) {
		int right = panel.x() + INPUT_X + INPUT_WIDTH;
		int top = panel.y() + INPUT_Y + 2;
		return mouseX >= right - CLEAR_SIZE && mouseX < right
			&& mouseY >= top && mouseY < top + CLEAR_SIZE;
	}

	static boolean toggleContains(HudBounds panel, double mouseX, double mouseY) {
		return toggleContains(panel, TOGGLE_Y, mouseX, mouseY);
	}

	static boolean exactToggleContains(HudBounds panel, double mouseX, double mouseY) {
		return toggleContains(panel, EXACT_TOGGLE_Y, mouseX, mouseY);
	}

	private static boolean toggleContains(HudBounds panel, int offsetY, double mouseX, double mouseY) {
		int left = panel.right() - TOGGLE_WIDTH - 8;
		int top = panel.y() + offsetY;
		return mouseX >= left && mouseX < left + TOGGLE_WIDTH
			&& mouseY >= top && mouseY < top + TOGGLE_HEIGHT;
	}
}
