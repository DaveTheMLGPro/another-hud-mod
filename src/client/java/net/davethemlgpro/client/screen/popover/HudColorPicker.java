package net.davethemlgpro.client.screen.popover;

import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

final class HudColorPicker {
	private static final int WIDTH = 196;
	private static final int HEIGHT = 150;
	private static final int HEADER_HEIGHT = 22;
	private static final int MARGIN = 4;
	private static final int GAP = 6;
	private static final int SV_WIDTH = 112;
	private static final int SV_HEIGHT = 64;
	private static final int HUE_WIDTH = 10;
	private static final int SIDE_X = 140;
	private static final int SIDE_WIDTH = 48;
	private static final int INPUT_WIDTH = 126;
	private static final int INPUT_HEIGHT = 20;
	private static final int ALPHA_Y = 108;
	private static final int INPUT_Y = 126;
	private static final int CHECKER_SIZE = 3;
	private static final int CLOSE_BUTTON_WIDTH = 20;
	private static final Component CLOSE_LABEL = Component.literal("X");

	private Component title = Component.empty();
	private IntSupplier getter;
	private IntConsumer setter;
	private EditBox input;
	private InputMode inputMode = InputMode.HEX;
	private DragTarget dragTarget = DragTarget.NONE;
	private int openingColor;
	private int x;
	private int y;
	private int dragOffsetX;
	private int dragOffsetY;
	private int screenWidth;
	private int screenHeight;
	private float hue;
	private float saturation;
	private float brightness;
	private boolean open;
	private boolean positioned;
	private boolean updatingInput;
	private boolean docked;

	public static int width() {
		return WIDTH;
	}

	public static int height() {
		return HEIGHT;
	}

	public void open(Component title, IntSupplier getter, IntConsumer setter) {
		this.title = title;
		this.getter = getter;
		this.setter = setter;
		openingColor = getter.getAsInt();
		updateHsv(openingColor);
		input = null;
		inputMode = InputMode.HEX;
		dragTarget = DragTarget.NONE;
		positioned = false;
		docked = false;
		open = true;
	}

	public void finish() {
		if (input != null) {
			input.setFocused(false);
		}
		input = null;
		dragTarget = DragTarget.NONE;
		open = false;
		docked = false;
	}

	public void cancel() {
		if (open && setter != null) {
			setter.accept(openingColor);
		}
		finish();
	}

	public boolean isOpen() {
		return open;
	}

	public boolean contains(double mouseX, double mouseY) {
		return open && contains(mouseX, mouseY, x, y, WIDTH, HEIGHT);
	}

	public void render(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY,
					   int anchorX, int anchorY, int anchorWidth, int screenWidth, int screenHeight) {
		if (!open) {
			return;
		}
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		layout(anchorX, anchorY, anchorWidth);
		ensureInput(font);
		positionInput();

		graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xF02A2A2A);
		graphics.outline(x, y, WIDTH, HEIGHT, 0xFF888888);
		graphics.fill(x + 1, y + HEADER_HEIGHT - 1, x + WIDTH - 1, y + HEADER_HEIGHT, 0xFF555555);
		graphics.enableScissor(x + GAP, y, x + WIDTH - GAP, y + HEADER_HEIGHT);
		graphics.text(font, title, x + GAP, y + 7, 0xFFFFFFFF);
		graphics.disableScissor();

		renderSaturationValue(graphics);
		renderHue(graphics);
		renderPreview(graphics);
		renderButton(graphics, font, inputMode.label, sideX(), y + 57, SIDE_WIDTH, 16,
			contains(mouseX, mouseY, sideX(), y + 57, SIDE_WIDTH, 16), 0xFF3A3A3A);
		renderButton(graphics, font, CommonComponents.GUI_DONE, sideX(), y + 78, SIDE_WIDTH, 18,
			contains(mouseX, mouseY, sideX(), y + 78, SIDE_WIDTH, 18), 0xFF356B35);
		renderButton(graphics, font, CommonComponents.GUI_CANCEL, sideX(), y + 100, SIDE_WIDTH, 18,
			contains(mouseX, mouseY, sideX(), y + 100, SIDE_WIDTH, 18), 0xFF6B3535);
		renderAlpha(graphics, font);
		input.extractRenderState(graphics, mouseX, mouseY, 0.0F);
	}

	public void renderDocked(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY,
							 int x, int y) {
		if (!open) {
			return;
		}
		this.x = x;
		this.y = y;
		this.screenWidth = x + WIDTH;
		this.screenHeight = y + HEIGHT;
		positioned = true;
		docked = true;
		ensureInput(font);
		positionInput();

		graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xF02A2A2A);
		graphics.outline(x, y, WIDTH, HEIGHT, 0xFF888888);
		graphics.fill(x + 1, y + HEADER_HEIGHT - 1, x + WIDTH - 1, y + HEADER_HEIGHT, 0xFF555555);
		graphics.enableScissor(x + GAP, y, x + WIDTH - CLOSE_BUTTON_WIDTH, y + HEADER_HEIGHT);
		graphics.text(font, title, x + GAP, y + 7, 0xFFFFFFFF);
		graphics.disableScissor();
		boolean closeHovered = contains(mouseX, mouseY, x + WIDTH - CLOSE_BUTTON_WIDTH, y,
			CLOSE_BUTTON_WIDTH, HEADER_HEIGHT);
		if (closeHovered) {
			graphics.fill(x + WIDTH - CLOSE_BUTTON_WIDTH, y + 1, x + WIDTH - 1,
				y + HEADER_HEIGHT - 1, 0xFFB33A3A);
		}
		graphics.centeredText(font, CLOSE_LABEL, x + WIDTH - CLOSE_BUTTON_WIDTH / 2, y + 7,
			closeHovered ? 0xFFFFFFFF : 0xFFCCCCCC);

		renderSaturationValue(graphics);
		renderHue(graphics);
		renderPreview(graphics);
		renderButton(graphics, font, inputMode.label, sideX(), y + 57, SIDE_WIDTH, 16,
			contains(mouseX, mouseY, sideX(), y + 57, SIDE_WIDTH, 16), 0xFF3A3A3A);
		renderAlpha(graphics, font);
		input.extractRenderState(graphics, mouseX, mouseY, 0.0F);
	}

	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (!open || !contains(event.x(), event.y())) {
			return false;
		}
		if (input != null && input.mouseClicked(event, doubleClick)) {
			input.setFocused(true);
			return true;
		}
		if (input != null) {
			input.setFocused(false);
		}
		if (event.button() != 0) {
			return true;
		}
		if (docked && contains(event.x(), event.y(), x + WIDTH - CLOSE_BUTTON_WIDTH, y,
			CLOSE_BUTTON_WIDTH, HEADER_HEIGHT)) {
			finish();
			return true;
		}
		if (contains(event.x(), event.y(), x, y, WIDTH, HEADER_HEIGHT)) {
			if (docked) {
				return true;
			}
			dragTarget = DragTarget.PANEL;
			dragOffsetX = (int) event.x() - x;
			dragOffsetY = (int) event.y() - y;
			return true;
		}
		if (contains(event.x(), event.y(), svX(), svY(), SV_WIDTH, SV_HEIGHT)) {
			dragTarget = DragTarget.SATURATION_VALUE;
			updateSaturationValue(event.x(), event.y());
			return true;
		}
		if (contains(event.x(), event.y(), hueX(), svY(), HUE_WIDTH, SV_HEIGHT)) {
			dragTarget = DragTarget.HUE;
			updateHue(event.y());
			return true;
		}
		if (contains(event.x(), event.y(), x + GAP, y + ALPHA_Y, INPUT_WIDTH, 10)) {
			dragTarget = DragTarget.ALPHA;
			updateAlpha(event.x());
			return true;
		}
		if (contains(event.x(), event.y(), sideX(), y + 57, SIDE_WIDTH, 16)) {
			inputMode = inputMode.next();
			syncInput();
			return true;
		}
		if (!docked && contains(event.x(), event.y(), sideX(), y + 78, SIDE_WIDTH, 18)) {
			finish();
			return true;
		}
		if (!docked && contains(event.x(), event.y(), sideX(), y + 100, SIDE_WIDTH, 18)) {
			cancel();
			return true;
		}
		return true;
	}

	public boolean mouseDragged(MouseButtonEvent event) {
		if (!open || dragTarget == DragTarget.NONE) {
			return false;
		}
		switch (dragTarget) {
			case PANEL -> {
				x = Math.clamp((int) event.x() - dragOffsetX, MARGIN,
					Math.max(MARGIN, screenWidth - MARGIN - WIDTH));
				y = Math.clamp((int) event.y() - dragOffsetY, MARGIN,
					Math.max(MARGIN, screenHeight - MARGIN - HEIGHT));
				positionInput();
			}
			case SATURATION_VALUE -> updateSaturationValue(event.x(), event.y());
			case HUE -> updateHue(event.y());
			case ALPHA -> updateAlpha(event.x());
			case NONE -> {
			}
		}
		return true;
	}

	public boolean mouseReleased() {
		if (!open || dragTarget == DragTarget.NONE) {
			return false;
		}
		dragTarget = DragTarget.NONE;
		return true;
	}

	public boolean keyPressed(KeyEvent event) {
		if (!open) {
			return false;
		}
		if (event.key() == 256) {
			if (docked) {
				finish();
			} else {
				cancel();
			}
			return true;
		}
		if (event.key() == 257 || event.key() == 335) {
			finish();
			return true;
		}
		return input != null && input.keyPressed(event);
	}

	public boolean charTyped(CharacterEvent event) {
		return open && input != null && input.charTyped(event);
	}

	private void layout(int anchorX, int anchorY, int anchorWidth) {
		if (positioned) {
			x = Math.clamp(x, MARGIN, Math.max(MARGIN, screenWidth - MARGIN - WIDTH));
			y = Math.clamp(y, MARGIN, Math.max(MARGIN, screenHeight - MARGIN - HEIGHT));
			return;
		}
		positioned = true;
		int right = anchorX + anchorWidth + GAP;
		x = right + WIDTH <= screenWidth - MARGIN ? right : anchorX - WIDTH - GAP;
		x = Math.clamp(x, MARGIN, Math.max(MARGIN, screenWidth - MARGIN - WIDTH));
		y = Math.clamp(anchorY, MARGIN, Math.max(MARGIN, screenHeight - MARGIN - HEIGHT));
	}

	private void ensureInput(Font font) {
		if (input != null) {
			return;
		}
		input = new EditBox(font, x + GAP, y + INPUT_Y, INPUT_WIDTH, INPUT_HEIGHT,
			TranslationKey.COLOR_PICKER_INPUT.component());
		input.setMaxLength(24);
		input.setResponder(this::updateFromInput);
		syncInput();
	}

	private void positionInput() {
		if (input == null) {
			return;
		}
		input.setX(x + GAP);
		input.setY(y + INPUT_Y);
		input.setWidth(INPUT_WIDTH);
	}

	private void renderSaturationValue(GuiGraphicsExtractor graphics) {
		int stepsX = 16;
		int stepsY = 8;
		for (int row = 0; row < stepsY; row++) {
			float value = 1.0F - (row + 0.5F) / stepsY;
			int top = svY() + row * SV_HEIGHT / stepsY;
			int bottom = svY() + (row + 1) * SV_HEIGHT / stepsY;
			for (int column = 0; column < stepsX; column++) {
				float saturation = (column + 0.5F) / stepsX;
				int left = svX() + column * SV_WIDTH / stepsX;
				int right = svX() + (column + 1) * SV_WIDTH / stepsX;
				graphics.fill(left, top, right, bottom,
					0xFF000000 | Mth.hsvToRgb(hue, saturation, value) & 0x00FFFFFF);
			}
		}
		graphics.outline(svX(), svY(), SV_WIDTH, SV_HEIGHT, 0xFF888888);
		int markerX = svX() + Math.round(saturation * SV_WIDTH);
		int markerY = svY() + Math.round((1.0F - brightness) * SV_HEIGHT);
		graphics.outline(markerX - 2, markerY - 2, 5, 5, 0xFFFFFFFF);
	}

	private void renderHue(GuiGraphicsExtractor graphics) {
		int steps = 12;
		for (int step = 0; step < steps; step++) {
			int top = svY() + step * SV_HEIGHT / steps;
			int bottom = svY() + (step + 1) * SV_HEIGHT / steps;
			int color = 0xFF000000 | Mth.hsvToRgb((step + 0.5F) / steps, 1.0F, 1.0F) & 0x00FFFFFF;
			graphics.fill(hueX(), top, hueX() + HUE_WIDTH, bottom, color);
		}
		int markerY = svY() + Math.round(hue * SV_HEIGHT);
		graphics.outline(hueX() - 1, markerY - 1, HUE_WIDTH + 2, 3, 0xFFFFFFFF);
	}

	private void renderPreview(GuiGraphicsExtractor graphics) {
		int previewX = sideX();
		int previewY = y + 28;
		drawCheckerboard(graphics, previewX, previewY, SIDE_WIDTH, 24);
		graphics.fill(previewX, previewY, previewX + SIDE_WIDTH / 2, previewY + 24, openingColor);
		graphics.fill(previewX + SIDE_WIDTH / 2, previewY, previewX + SIDE_WIDTH, previewY + 24,
			getter.getAsInt());
		graphics.outline(previewX, previewY, SIDE_WIDTH, 24, 0xFF888888);
	}

	private void renderAlpha(GuiGraphicsExtractor graphics, Font font) {
		int alphaX = x + GAP;
		int alphaY = y + ALPHA_Y;
		int color = getter.getAsInt();
		int rgb = color & 0x00FFFFFF;
		drawCheckerboard(graphics, alphaX, alphaY, INPUT_WIDTH, 10);
		for (int step = 0; step < 16; step++) {
			int left = alphaX + step * INPUT_WIDTH / 16;
			int right = alphaX + (step + 1) * INPUT_WIDTH / 16;
			graphics.fill(left, alphaY, right, alphaY + 10, step * 17 << 24 | rgb);
		}
		int markerX = alphaX + Math.round((color >>> 24) / 255.0F * INPUT_WIDTH);
		graphics.outline(markerX - 1, alphaY - 1, 3, 12, 0xFFFFFFFF);
		graphics.text(font, TranslationKey.COLOR_PICKER_ALPHA.component(), alphaX, alphaY - 10, 0xFFCCCCCC);
	}

	private void renderButton(GuiGraphicsExtractor graphics, Font font, Component label, int x, int y,
							  int width, int height, boolean hovered, int background) {
		graphics.fill(x, y, x + width, y + height, hovered ? 0xFF555555 : background);
		graphics.outline(x, y, width, height, hovered ? 0xFFAAAAAA : 0xFF666666);
		graphics.centeredText(font, label, x + width / 2, y + (height - 8) / 2, 0xFFFFFFFF);
	}

	private void updateSaturationValue(double mouseX, double mouseY) {
		saturation = (float) Math.clamp((mouseX - svX()) / SV_WIDTH, 0.0, 1.0);
		brightness = 1.0F - (float) Math.clamp((mouseY - svY()) / SV_HEIGHT, 0.0, 1.0);
		applyHsv();
	}

	private void updateHue(double mouseY) {
		hue = Math.min((float) Math.clamp((mouseY - svY()) / SV_HEIGHT, 0.0, 1.0),
			Math.nextDown(1.0F));
		applyHsv();
	}

	private void updateAlpha(double mouseX) {
		int alpha = (int) Math.round(Math.clamp((mouseX - x - GAP) / INPUT_WIDTH, 0.0, 1.0) * 255.0);
		setColor(alpha << 24 | getter.getAsInt() & 0x00FFFFFF);
	}

	private void applyHsv() {
		int alpha = getter.getAsInt() & 0xFF000000;
		setColor(alpha | Mth.hsvToRgb(hue, saturation, brightness) & 0x00FFFFFF);
	}

	private void setColor(int color) {
		setter.accept(color);
		syncInput();
	}

	private void updateHsv(int color) {
		float red = (color >> 16 & 0xFF) / 255.0F;
		float green = (color >> 8 & 0xFF) / 255.0F;
		float blue = (color & 0xFF) / 255.0F;
		float maximum = Math.max(red, Math.max(green, blue));
		float minimum = Math.min(red, Math.min(green, blue));
		float range = maximum - minimum;
		brightness = maximum;
		saturation = maximum == 0.0F ? 0.0F : range / maximum;
		if (range == 0.0F) {
			hue = 0.0F;
		} else if (maximum == red) {
			hue = (green - blue) / range / 6.0F;
		} else if (maximum == green) {
			hue = ((blue - red) / range + 2.0F) / 6.0F;
		} else {
			hue = ((red - green) / range + 4.0F) / 6.0F;
		}
		if (hue < 0.0F) {
			hue += 1.0F;
		}
	}

	private void updateFromInput(String value) {
		if (updatingInput) {
			return;
		}
		Integer parsed = inputMode.parse(value, getter.getAsInt() >>> 24);
		input.setTextColor(parsed == null ? 0xFFFF5555 : 0xFFFFFFFF);
		if (parsed != null) {
			setter.accept(parsed);
			updateHsv(parsed);
		}
	}

	private void syncInput() {
		if (input == null) {
			return;
		}
		updatingInput = true;
		input.setValue(inputMode.format(getter.getAsInt()));
		input.setTextColor(0xFFFFFFFF);
		updatingInput = false;
	}

	private int svX() {
		return x + GAP;
	}

	private int svY() {
		return y + 28;
	}

	private int hueX() {
		return svX() + SV_WIDTH + 4;
	}

	private int sideX() {
		return x + SIDE_X;
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

	private enum DragTarget {
		NONE,
		PANEL,
		SATURATION_VALUE,
		HUE,
		ALPHA
	}

	private enum InputMode {
		HEX(Component.literal("HEX")) {
			@Override
			String format(int color) {
				return String.format("#%08X", color);
			}

			@Override
			Integer parse(String value, int currentAlpha) {
				String normalized = value.trim().replace("#", "");
				if (normalized.length() == 6) {
					normalized = "FF" + normalized;
				}
				if (normalized.length() != 8) {
					return null;
				}
				try {
					return (int) Long.parseLong(normalized, 16);
				} catch (NumberFormatException ignored) {
					return null;
				}
			}
		},
		RGB(Component.literal("RGB")) {
			@Override
			String format(int color) {
				return (color >> 16 & 0xFF) + ", " + (color >> 8 & 0xFF) + ", "
					+ (color & 0xFF) + ", " + (color >>> 24);
			}

			@Override
			Integer parse(String value, int currentAlpha) {
				String[] channels = value.trim().split("[,\\s]+");
				if (channels.length != 3 && channels.length != 4) {
					return null;
				}
				try {
					int red = Integer.parseInt(channels[0]);
					int green = Integer.parseInt(channels[1]);
					int blue = Integer.parseInt(channels[2]);
					int alpha = channels.length == 4 ? Integer.parseInt(channels[3]) : currentAlpha;
					if ((red | green | blue | alpha) < 0
						|| red > 255 || green > 255 || blue > 255 || alpha > 255) {
						return null;
					}
					return alpha << 24 | red << 16 | green << 8 | blue;
				} catch (NumberFormatException ignored) {
					return null;
				}
			}
		};

		private final Component label;

		InputMode(Component label) {
			this.label = label;
		}

		abstract String format(int color);

		abstract Integer parse(String value, int currentAlpha);

		InputMode next() {
			return this == HEX ? RGB : HEX;
		}
	}
}
