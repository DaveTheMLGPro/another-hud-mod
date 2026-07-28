package net.davethemlgpro.client.screen.popover;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public interface HudPopoverControl {
	int DEFAULT_HEIGHT = 30;

	Component description();

	default int height() {
		return DEFAULT_HEIGHT;
	}

	void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, boolean hovered, int accentColor);

	boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width);

	default boolean mouseDragged(double mouseX, double mouseY, int x, int y, int width) {
		return false;
	}

	default void mouseReleased() {
	}
}
