package net.davethemlgpro.client.screen.popover;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class HudToggleControl implements HudPopoverControl {
	private final Component label;
	private final Component description;
	private final BooleanSupplier getter;
	private final Consumer<Boolean> setter;

	public HudToggleControl(Component label, Component description, BooleanSupplier getter, Consumer<Boolean> setter) {
		this.label = label;
		this.description = description;
		this.getter = getter;
		this.setter = setter;
	}

	@Override
	public Component description() {
		return description;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width,
					   boolean hovered, int accentColor) {
		if (hovered) {
			graphics.fill(x, y, x + width, y + height(), 0x3355AAFF);
		}
		graphics.text(font, label, x + 4, y + 3, 0xFFFFFFFF);

		boolean enabled = getter.getAsBoolean();
		int buttonY = y + 15;
		graphics.fill(x + 3, buttonY, x + width - 3, buttonY + 12, 0xFF151515);
		graphics.fill(x + 4, buttonY + 1, x + width - 4, buttonY + 11,
			enabled ? withAlpha(accentColor, 0xAA) : 0xFF383838);
		graphics.centeredText(font, enabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF,
			x + width / 2, buttonY + 2, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		if (button != 0 || !contains(mouseX, mouseY, x, y, width, height())) {
			return false;
		}
		setter.accept(!getter.getAsBoolean());
		return true;
	}

	private static int withAlpha(int color, int alpha) {
		return alpha << 24 | color & 0x00FFFFFF;
	}

	private static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
}
