package net.davethemlgpro.client.screen.popover;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;

public final class HudSliderControl implements HudPopoverControl {
	private static final int TRACK_INSET = 4;

	private final Component label;
	private final Component description;
	private final double minimum;
	private final double maximum;
	private final double step;
	private final DoubleSupplier getter;
	private final DoubleConsumer setter;
	private final DoubleFunction<Component> formatter;
	private boolean dragging;

	public HudSliderControl(Component label, Component description, double minimum, double maximum, double step,
							DoubleSupplier getter, DoubleConsumer setter, DoubleFunction<Component> formatter) {
		if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || minimum >= maximum || step <= 0.0) {
			throw new IllegalArgumentException("Invalid slider range.");
		}
		this.label = label;
		this.description = description;
		this.minimum = minimum;
		this.maximum = maximum;
		this.step = step;
		this.getter = getter;
		this.setter = setter;
		this.formatter = formatter;
	}

	@Override
	public Component description() {
		return description;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width,
					   boolean hovered, int accentColor) {
		if (hovered || dragging) {
			graphics.fill(x, y, x + width, y + height(), withAlpha(accentColor, 0x33));
		}

		Component value = formatter.apply(getter.getAsDouble());
		int valueWidth = font.width(value);
		graphics.enableScissor(x + 4, y, Math.max(x + 5, x + width - valueWidth - 8), y + 15);
		graphics.text(font, label, x + 4, y + 3, 0xFFFFFFFF);
		graphics.disableScissor();
		graphics.text(font, value, x + width - valueWidth - 4, y + 3, 0xFFCCCCCC);

		int trackX = x + TRACK_INSET;
		int trackWidth = width - TRACK_INSET * 2;
		int trackY = y + 21;
		double progress = Math.clamp((getter.getAsDouble() - minimum) / (maximum - minimum), 0.0, 1.0);
		int knobX = trackX + (int) Math.round(progress * trackWidth);
		graphics.fill(trackX, trackY, trackX + trackWidth, trackY + 3, 0xFF151515);
		graphics.fill(trackX, trackY, knobX, trackY + 3, accentColor);
		graphics.fill(knobX - 2, trackY - 3, knobX + 2, trackY + 6, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		if (button != 0 || !contains(mouseX, mouseY, x, y, width, height())) {
			return false;
		}
		dragging = true;
		updateValue(mouseX, x, width);
		return true;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int x, int y, int width) {
		if (!dragging) {
			return false;
		}
		updateValue(mouseX, x, width);
		return true;
	}

	@Override
	public void mouseReleased() {
		dragging = false;
	}

	private void updateValue(double mouseX, int x, int width) {
		int trackX = x + TRACK_INSET;
		int trackWidth = Math.max(1, width - TRACK_INSET * 2);
		double progress = Math.clamp((mouseX - trackX) / trackWidth, 0.0, 1.0);
		double rawValue = minimum + progress * (maximum - minimum);
		double steppedValue = minimum + Math.round((rawValue - minimum) / step) * step;
		setter.accept(Math.clamp(steppedValue, minimum, maximum));
	}

	private boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
}
