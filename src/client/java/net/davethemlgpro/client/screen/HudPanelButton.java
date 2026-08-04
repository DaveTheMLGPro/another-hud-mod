package net.davethemlgpro.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.IntSupplier;

final class HudPanelButton extends Button {
	private final IntSupplier accentColor;
	private final boolean primary;

	HudPanelButton(int x, int y, int width, int height, Component message, OnPress onPress,
				   IntSupplier accentColor, boolean primary) {
		super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
		this.accentColor = accentColor;
		this.primary = primary;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		boolean hovered = isFocused() || mouseX >= getX() && mouseX < getX() + getWidth()
			&& mouseY >= getY() && mouseY < getY() + getHeight();
		int accent = accentColor.getAsInt();
		int border = primary ? accent : hovered ? 0xFFAAAAAA : 0xFF666666;
		int background = hovered ? primary ? withAlpha(accent, 0x33) : 0xFF383838 : 0xFF292929;
		graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), background);
		graphics.outline(getX(), getY(), getWidth(), getHeight(), border);
		graphics.centeredText(Minecraft.getInstance().font, getMessage(), getX() + getWidth() / 2,
			getY() + (getHeight() - 8) / 2, active ? 0xFFFFFFFF : 0xFF777777);
	}

	private static int withAlpha(int color, int alpha) {
		return alpha << 24 | color & 0x00FFFFFF;
	}
}
