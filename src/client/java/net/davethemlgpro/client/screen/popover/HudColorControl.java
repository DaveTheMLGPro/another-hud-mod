package net.davethemlgpro.client.screen.popover;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class HudColorControl implements HudPopoverControl {
	private static final int SWATCH_SIZE = 18;
	private static final int ACTION_SIZE = 18;
	private static final int CONTROL_GAP = 4;
	private static final int RIGHT_MARGIN = 4;
	private static final int CHECKER_SIZE = 3;
	private static final Component UNDO_LABEL = Component.literal("U");
	private static final Component RESET_LABEL = Component.literal("⟲");

	private final Component label;
	private final Component description;
	private final IntSupplier getter;
	private final IntConsumer setter;
	private final int openingColor;
	private final int defaultColor;
	private HudPopoverContext context;

	public HudColorControl(Component label, Component description, IntSupplier getter, IntConsumer setter,
						   int defaultColor) {
		this.label = label;
		this.description = description;
		this.getter = getter;
		this.setter = setter;
		openingColor = getter.getAsInt();
		this.defaultColor = defaultColor;
	}

	@Override
	public Component description() {
		return description;
	}

	@Override
	public void onAdded(HudPopoverContext context) {
		this.context = context;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width,
					   boolean hovered, int accentColor) {
		if (hovered) {
			graphics.fill(x, y, x + width, y + height(), 0x3355AAFF);
		}
		int resetX = x + width - RIGHT_MARGIN - ACTION_SIZE;
		int undoX = resetX - CONTROL_GAP - ACTION_SIZE;
		int swatchX = undoX - CONTROL_GAP - SWATCH_SIZE;
		int swatchY = y + 6;
		graphics.enableScissor(x + 4, y, swatchX - 6, y + height());
		graphics.text(font, label, x + 4, y + 11, 0xFFFFFFFF);
		graphics.disableScissor();
		drawCheckerboard(graphics, swatchX, swatchY, SWATCH_SIZE, SWATCH_SIZE);
		graphics.fill(swatchX, swatchY, swatchX + SWATCH_SIZE, swatchY + SWATCH_SIZE, getter.getAsInt());
		graphics.outline(swatchX, swatchY, SWATCH_SIZE, SWATCH_SIZE, hovered ? accentColor : 0xFF888888);
		renderActionButton(graphics, font, undoX, swatchY, UNDO_LABEL,
			getter.getAsInt() != openingColor, false);
		renderActionButton(graphics, font, resetX, swatchY, RESET_LABEL,
			getter.getAsInt() != defaultColor, true);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		if (button != 0 || context == null || !contains(mouseX, mouseY, x, y, width, height())) {
			return false;
		}
		int resetX = x + width - RIGHT_MARGIN - ACTION_SIZE;
		int undoX = resetX - CONTROL_GAP - ACTION_SIZE;
		int buttonY = y + 6;
		if (contains(mouseX, mouseY, undoX, buttonY, ACTION_SIZE, ACTION_SIZE)) {
			setter.accept(openingColor);
			return true;
		}
		if (contains(mouseX, mouseY, resetX, buttonY, ACTION_SIZE, ACTION_SIZE)) {
			setter.accept(defaultColor);
			return true;
		}
		context.openColorPicker(label, getter, setter);
		return true;
	}

	private static void renderActionButton(GuiGraphicsExtractor graphics, Font font, int x, int y,
										   Component label, boolean active, boolean reset) {
		int border = reset ? 0xFF663333 : 0xFF555555;
		int text = active ? reset ? 0xFFFF6666 : 0xFFFFFFFF : 0xFF777777;
		if (active) {
			graphics.fill(x, y, x + ACTION_SIZE, y + ACTION_SIZE, reset ? 0x55331111 : 0x55333333);
		}
		graphics.outline(x, y, ACTION_SIZE, ACTION_SIZE, border);
		graphics.centeredText(font, label, x + ACTION_SIZE / 2, y + 5, text);
	}

	private static void drawCheckerboard(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
		for (int row = 0; row < height; row += CHECKER_SIZE) {
			for (int column = 0; column < width; column += CHECKER_SIZE) {
				int color = ((row / CHECKER_SIZE + column / CHECKER_SIZE) & 1) == 0
					? 0xFFAAAAAA : 0xFF666666;
				graphics.fill(x + column, y + row, Math.min(x + column + CHECKER_SIZE, x + width),
					Math.min(y + row + CHECKER_SIZE, y + height), color);
			}
		}
	}

	private static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
}
