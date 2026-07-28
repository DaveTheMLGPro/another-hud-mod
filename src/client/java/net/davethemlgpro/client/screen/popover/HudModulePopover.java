package net.davethemlgpro.client.screen.popover;

import net.davethemlgpro.client.config.EditorConfig;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class HudModulePopover {
	private static final int WIDTH = 196;
	private static final int HEADER_HEIGHT = 22;
	private static final int PADDING = 5;
	private static final int PLACEMENT_GAP = 16;
	private static final int SCREEN_MARGIN = 4;
	private static final int EMPTY_BODY_HEIGHT = 28;
	private static final int SCROLLBAR_WIDTH = 3;
	private static final int CLOSE_BUTTON_WIDTH = 20;
	private static final Component CLOSE_LABEL = Component.literal("X");

	private Component title = Component.empty();
	private List<HudPopoverControl> controls = List.of();
	private int x;
	private int y;
	private int width;
	private int height;
	private int scrollOffset;
	private int maxScroll;
	private int hoveredControl = -1;
	private int activeControl = -1;
	private int bodyHeight;
	private boolean open;
	private boolean positioned;
	private boolean dragging;
	private int dragOffsetX;
	private int dragOffsetY;
	private int screenWidth;
	private int editorTop;
	private int editorBottom;

	public void open(Component title, List<HudPopoverControl> controls) {
		releaseActiveControl();
		this.title = title;
		this.controls = List.copyOf(controls);
		bodyHeight = controls.isEmpty() ? EMPTY_BODY_HEIGHT : PADDING * 2;
		for (HudPopoverControl control : controls) {
			bodyHeight += control.height();
		}
		scrollOffset = 0;
		positioned = false;
		dragging = false;
		open = true;
	}

	public void close() {
		releaseActiveControl();
		controls = List.of();
		hoveredControl = -1;
		scrollOffset = 0;
		maxScroll = 0;
		positioned = false;
		dragging = false;
		open = false;
	}

	public boolean isOpen() {
		return open;
	}

	public void render(GuiGraphicsExtractor graphics, Font font, HudBounds moduleBounds, EditorConfig colors,
					   int mouseX, int mouseY, int screenWidth, int editorTop, int editorBottom) {
		if (!open) {
			return;
		}

		layout(moduleBounds, screenWidth, editorTop, editorBottom);
		graphics.fill(x, y, x + width, y + height, 0xE6282828);
		drawBorder(graphics, x, y, width, height, 0xFF707070);
		graphics.fill(x + 1, y + HEADER_HEIGHT - 1, x + width - 1, y + HEADER_HEIGHT, 0xFF555555);
		graphics.enableScissor(x + PADDING, y, x + width - CLOSE_BUTTON_WIDTH, y + HEADER_HEIGHT);
		graphics.text(font, title, x + PADDING, y + 7, 0xFFFFFFFF);
		graphics.disableScissor();
		boolean closeHovered = contains(mouseX, mouseY, x + width - CLOSE_BUTTON_WIDTH, y,
			CLOSE_BUTTON_WIDTH, HEADER_HEIGHT);
		if (closeHovered) {
			graphics.fill(x + width - CLOSE_BUTTON_WIDTH, y + 1, x + width - 1,
				y + HEADER_HEIGHT - 1, 0xFFB33A3A);
		}
		graphics.centeredText(font, CLOSE_LABEL, x + width - CLOSE_BUTTON_WIDTH / 2, y + 7,
			closeHovered ? 0xFFFFFFFF : 0xFFCCCCCC);

		int contentTop = y + HEADER_HEIGHT;
		int contentBottom = y + height - PADDING;
		int controlWidth = width - PADDING * 2 - (maxScroll > 0 ? SCROLLBAR_WIDTH + 2 : 0);
		hoveredControl = -1;
		graphics.enableScissor(x + 1, contentTop, x + width - 1, contentBottom);
		if (controls.isEmpty()) {
			graphics.centeredText(font, TranslationKey.POPOVER_NO_SETTINGS.component(),
				x + width / 2, contentTop + 9, 0xFFAAAAAA);
		} else {
			int controlY = contentTop + PADDING - scrollOffset;
			for (int i = 0; i < controls.size(); i++) {
				HudPopoverControl control = controls.get(i);
				boolean visible = controlY < contentBottom && controlY + control.height() > contentTop;
				boolean hovered = visible && contains(mouseX, mouseY, x + PADDING, controlY,
					controlWidth, control.height());
				if (hovered) {
					hoveredControl = i;
				}
				if (visible) {
					control.render(graphics, font, x + PADDING, controlY, controlWidth,
						hovered, colors.getSelectionColor());
				}
				controlY += control.height();
			}
		}
		graphics.disableScissor();

		if (maxScroll > 0) {
			renderScrollbar(graphics, contentTop, contentBottom);
		}
		if (hoveredControl >= 0) {
			Component description = controls.get(hoveredControl).description();
			if (!description.getString().isEmpty()) {
				graphics.setTooltipForNextFrame(font, description, mouseX, mouseY);
			}
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!open || !contains(mouseX, mouseY, x, y, width, height)) {
			return false;
		}

		if (button == 0 && contains(mouseX, mouseY, x + width - CLOSE_BUTTON_WIDTH, y,
			CLOSE_BUTTON_WIDTH, HEADER_HEIGHT)) {
			close();
			return true;
		}
		if (button == 0 && contains(mouseX, mouseY, x, y, width - CLOSE_BUTTON_WIDTH, HEADER_HEIGHT)) {
			releaseActiveControl();
			dragging = true;
			dragOffsetX = (int) mouseX - x;
			dragOffsetY = (int) mouseY - y;
			return true;
		}

		int contentTop = y + HEADER_HEIGHT;
		int contentBottom = y + height - PADDING;
		int controlWidth = width - PADDING * 2 - (maxScroll > 0 ? SCROLLBAR_WIDTH + 2 : 0);
		int controlY = contentTop + PADDING - scrollOffset;
		releaseActiveControl();
		for (int i = 0; i < controls.size(); i++) {
			HudPopoverControl control = controls.get(i);
			if (controlY < contentBottom && controlY + control.height() > contentTop
				&& control.mouseClicked(mouseX, mouseY, button, x + PADDING, controlY, controlWidth)) {
				activeControl = i;
				return true;
			}
			controlY += control.height();
		}
		return true;
	}

	public boolean mouseDragged(double mouseX, double mouseY) {
		if (open && dragging) {
			x = Math.clamp((int) mouseX - dragOffsetX, SCREEN_MARGIN,
				Math.max(SCREEN_MARGIN, screenWidth - SCREEN_MARGIN - width));
			y = Math.clamp((int) mouseY - dragOffsetY, editorTop,
				Math.max(editorTop, editorBottom - height));
			return true;
		}
		if (!open || activeControl < 0 || activeControl >= controls.size()) {
			return false;
		}

		int controlY = y + HEADER_HEIGHT + PADDING - scrollOffset;
		for (int i = 0; i < activeControl; i++) {
			controlY += controls.get(i).height();
		}
		int controlWidth = width - PADDING * 2 - (maxScroll > 0 ? SCROLLBAR_WIDTH + 2 : 0);
		return controls.get(activeControl).mouseDragged(mouseX, mouseY, x + PADDING, controlY, controlWidth);
	}

	public boolean mouseReleased() {
		if (dragging) {
			dragging = false;
			return true;
		}
		if (activeControl < 0) {
			return false;
		}
		releaseActiveControl();
		return true;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
		if (!open || maxScroll <= 0 || !contains(mouseX, mouseY, x, y, width, height)) {
			return false;
		}
		scrollOffset = Math.clamp(scrollOffset - (int) Math.round(scrollY * HudPopoverControl.DEFAULT_HEIGHT),
			0, maxScroll);
		return true;
	}

	public boolean contains(double mouseX, double mouseY) {
		return open && contains(mouseX, mouseY, x, y, width, height);
	}

	public void moveBy(int deltaX, int deltaY) {
		if (!open || !positioned) {
			return;
		}
		x = Math.clamp(x + deltaX, SCREEN_MARGIN,
			Math.max(SCREEN_MARGIN, screenWidth - SCREEN_MARGIN - width));
		y = Math.clamp(y + deltaY, editorTop, Math.max(editorTop, editorBottom - height));
	}

	private void layout(HudBounds moduleBounds, int screenWidth, int editorTop, int editorBottom) {
		this.screenWidth = screenWidth;
		this.editorTop = editorTop;
		this.editorBottom = editorBottom;
		int availableHeight = Math.max(HEADER_HEIGHT + PADDING * 2, editorBottom - editorTop);
		width = Math.min(WIDTH, Math.max(80, screenWidth - SCREEN_MARGIN * 2));
		height = Math.min(HEADER_HEIGHT + bodyHeight, availableHeight);
		maxScroll = Math.max(0, HEADER_HEIGHT + bodyHeight - height);
		scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);

		if (positioned) {
			x = Math.clamp(x, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenWidth - SCREEN_MARGIN - width));
			y = Math.clamp(y, editorTop, Math.max(editorTop, editorBottom - height));
			return;
		}
		positioned = true;

		int rightX = moduleBounds.right() + PLACEMENT_GAP;
		if (rightX + width <= screenWidth - SCREEN_MARGIN) {
			x = rightX;
			y = Math.clamp(moduleBounds.y(), editorTop, editorBottom - height);
			return;
		}

		int leftX = moduleBounds.x() - PLACEMENT_GAP - width;
		if (leftX >= SCREEN_MARGIN) {
			x = leftX;
			y = Math.clamp(moduleBounds.y(), editorTop, editorBottom - height);
			return;
		}

		int clampedX = Math.clamp(moduleBounds.x(), SCREEN_MARGIN, screenWidth - SCREEN_MARGIN - width);
		int belowY = moduleBounds.bottom() + PLACEMENT_GAP;
		if (belowY + height <= editorBottom) {
			x = clampedX;
			y = belowY;
			return;
		}

		int aboveY = moduleBounds.y() - PLACEMENT_GAP - height;
		if (aboveY >= editorTop) {
			x = clampedX;
			y = aboveY;
			return;
		}

		x = Math.clamp(rightX, SCREEN_MARGIN, screenWidth - SCREEN_MARGIN - width);
		y = Math.clamp(moduleBounds.y(), editorTop, editorBottom - height);
	}

	private void renderScrollbar(GuiGraphicsExtractor graphics, int contentTop, int contentBottom) {
		int trackHeight = contentBottom - contentTop - PADDING * 2;
		int contentHeight = trackHeight + maxScroll;
		int thumbHeight = Math.max(12, trackHeight * trackHeight / contentHeight);
		int thumbTravel = Math.max(0, trackHeight - thumbHeight);
		int thumbY = contentTop + PADDING + (maxScroll == 0 ? 0 : scrollOffset * thumbTravel / maxScroll);
		int scrollbarX = x + width - PADDING - SCROLLBAR_WIDTH;
		graphics.fill(scrollbarX, contentTop + PADDING, scrollbarX + SCROLLBAR_WIDTH,
			contentBottom - PADDING, 0xFF151515);
		graphics.fill(scrollbarX, thumbY, scrollbarX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFAAAAAA);
	}

	private void releaseActiveControl() {
		if (activeControl >= 0 && activeControl < controls.size()) {
			controls.get(activeControl).mouseReleased();
		}
		activeControl = -1;
	}

	private static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private static void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + 1, color);
		graphics.fill(x, y + height - 1, x + width, y + height, color);
		graphics.fill(x, y, x + 1, y + height, color);
		graphics.fill(x + width - 1, y, x + width, y + height, color);
	}
}
