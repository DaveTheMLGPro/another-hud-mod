package net.davethemlgpro.client.screen.popover;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class HudSectionControl implements HudPopoverControl {
	private static final int HEIGHT = 20;

	private final Component title;

	public HudSectionControl(Component title) {
		this.title = title;
	}

	@Override
	public Component description() {
		return CommonComponents.EMPTY;
	}

	@Override
	public int height() {
		return HEIGHT;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width,
					   boolean hovered, int accentColor) {
		int titleWidth = Math.min(font.width(title), width - 8);
		graphics.enableScissor(x + 4, y, x + width - 4, y + height());
		graphics.text(font, title, x + 4, y + 6, accentColor);
		graphics.disableScissor();
		int lineX = x + titleWidth + 10;
		if (lineX < x + width - 4) {
			graphics.fill(lineX, y + 10, x + width - 4, y + 11, 0xFF555555);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		return false;
	}
}
