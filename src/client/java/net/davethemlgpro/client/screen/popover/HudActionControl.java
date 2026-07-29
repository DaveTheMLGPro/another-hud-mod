package net.davethemlgpro.client.screen.popover;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class HudActionControl implements HudPopoverControl {
	private static final int BUTTON_MARGIN_X = 3;
	private static final int BUTTON_MARGIN_Y = 5;
	private static final int BUTTON_HEIGHT = 20;

	private final Component label;
	private final Component description;
	private final Runnable action;
	private final int color;
	private final int borderColor;
	private final int hoveredBackgroundColor;

	public HudActionControl(Component label, Component description, Runnable action,
							int color, int borderColor, int hoveredBackgroundColor) {
		this.label = label;
		this.description = description;
		this.action = action;
		this.color = color;
		this.borderColor = borderColor;
		this.hoveredBackgroundColor = hoveredBackgroundColor;
	}

	@Override
	public Component description() {
		return description;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width,
					   boolean hovered, int accentColor) {
		int buttonX = x + BUTTON_MARGIN_X;
		int buttonY = y + BUTTON_MARGIN_Y;
		int buttonWidth = width - BUTTON_MARGIN_X * 2;
		if (hovered) {
			graphics.fill(buttonX, buttonY, buttonX + buttonWidth,
				buttonY + BUTTON_HEIGHT, hoveredBackgroundColor);
		}
		graphics.outline(buttonX, buttonY, buttonWidth, BUTTON_HEIGHT, borderColor);
		graphics.centeredText(font, label, x + width / 2, buttonY + 6, color);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		int buttonX = x + BUTTON_MARGIN_X;
		int buttonY = y + BUTTON_MARGIN_Y;
		int buttonWidth = width - BUTTON_MARGIN_X * 2;
		if (button != 0 || mouseX < buttonX || mouseX >= buttonX + buttonWidth
			|| mouseY < buttonY || mouseY >= buttonY + BUTTON_HEIGHT) {
			return false;
		}
		action.run();
		return true;
	}
}
