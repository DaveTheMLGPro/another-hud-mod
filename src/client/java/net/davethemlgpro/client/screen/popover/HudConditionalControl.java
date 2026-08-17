package net.davethemlgpro.client.screen.popover;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;

public final class HudConditionalControl implements HudPopoverControl {
	private final HudPopoverControl control;
	private final BooleanSupplier visible;

	public HudConditionalControl(HudPopoverControl control, BooleanSupplier visible) {
		this.control = control;
		this.visible = visible;
	}

	@Override
	public Component description() {
		return control.description();
	}

	@Override
	public void onAdded(HudPopoverContext context) {
		control.onAdded(context);
	}

	@Override
	public boolean visible() {
		return visible.getAsBoolean();
	}

	@Override
	public int height() {
		return control.height();
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width,
					   boolean hovered, int accentColor) {
		control.render(graphics, font, x, y, width, hovered, accentColor);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		return control.mouseClicked(mouseX, mouseY, button, x, y, width);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int x, int y, int width) {
		return control.mouseDragged(mouseX, mouseY, x, y, width);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollY, int x, int y, int width) {
		return control.mouseScrolled(mouseX, mouseY, scrollY, x, y, width);
	}

	@Override
	public void mouseReleased() {
		control.mouseReleased();
	}
}
