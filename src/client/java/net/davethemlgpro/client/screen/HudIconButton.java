package net.davethemlgpro.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

final class HudIconButton extends Button {
	private final Identifier icon;
	private final int iconSize;

	public HudIconButton(int x, int y, int width, int height, Component message,
						 OnPress onPress, Identifier icon, int iconSize) {
		super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
		this.icon = icon;
		this.iconSize = iconSize;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		extractDefaultSprite(graphics);
		int iconX = getX() + (getWidth() - iconSize) / 2;
		int iconY = getY() + (getHeight() - iconSize) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, icon, iconX, iconY, 0.0F, 0.0F,
			iconSize, iconSize, iconSize, iconSize);
	}

	@Override
	public boolean shouldTakeFocusAfterInteraction() {
		return false;
	}
}
