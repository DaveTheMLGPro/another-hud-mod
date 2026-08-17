package net.davethemlgpro.client.screen.popover;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public interface HudPopoverControl {
	int DEFAULT_HEIGHT = 30;

	Component description();

	default void onAdded(HudPopoverContext context) {
	}

	default boolean visible() {
		return true;
	}

	default int height() {
		return DEFAULT_HEIGHT;
	}

	void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, boolean hovered, int accentColor);

	boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width);

	default boolean mouseDragged(double mouseX, double mouseY, int x, int y, int width) {
		return false;
	}

	default boolean mouseScrolled(double mouseX, double mouseY, double scrollY, int x, int y, int width) {
		return false;
	}

	default void mouseReleased() {
	}

	default boolean keyPressed(KeyEvent event) {
		return false;
	}

	default boolean charTyped(CharacterEvent event) {
		return false;
	}

	default void focusLost() {
	}

	default int withAlpha(int color, int alpha) {
		return alpha << 24 | color & 0x00FFFFFF;
	}
}
