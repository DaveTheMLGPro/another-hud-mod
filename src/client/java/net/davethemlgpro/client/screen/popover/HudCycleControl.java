package net.davethemlgpro.client.screen.popover;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class HudCycleControl<T> implements HudPopoverControl {
	private final Component label;
	private final Component description;
	private final List<T> values;
	private final Supplier<T> getter;
	private final Consumer<T> setter;
	private final Function<T, Component> formatter;

	public HudCycleControl(Component label, Component description, List<T> values, Supplier<T> getter,
						   Consumer<T> setter, Function<T, Component> formatter) {
		if (values.isEmpty()) {
			throw new IllegalArgumentException("Cycle controls require at least one value.");
		}
		this.label = label;
		this.description = description;
		this.values = List.copyOf(values);
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
		if (hovered) {
			graphics.fill(x, y, x + width, y + height(), 0x3355AAFF);
		}
		graphics.text(font, label, x + 4, y + 3, 0xFFFFFFFF);

		int buttonY = y + 15;
		graphics.fill(x + 3, buttonY, x + width - 3, buttonY + 12, 0xFF151515);
		graphics.text(font, "<", x + 7, buttonY + 2, accentColor);
		graphics.text(font, ">", x + width - 12, buttonY + 2, accentColor);
		graphics.centeredText(font, formatter.apply(getter.get()), x + width / 2, buttonY + 2, 0xFFFFFFFF);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
		if ((button != 0 && button != 1) || !contains(mouseX, mouseY, x, y, width, height())) {
			return false;
		}

		int currentIndex = values.indexOf(getter.get());
		int direction = button == 0 ? 1 : -1;
		int nextIndex = Math.floorMod(currentIndex + direction, values.size());
		setter.accept(values.get(nextIndex));
		return true;
	}

	private static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
}
