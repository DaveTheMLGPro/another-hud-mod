package net.davethemlgpro.client.screen.popover;

import net.davethemlgpro.client.config.EditorConfig;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class HudModulePopover implements HudPopoverContext {
	private static final int WIDTH = 196;
	private static final int HEADER_HEIGHT = 22;
	private static final int TAB_HEIGHT = 22;
	private static final int PADDING = 5;
	private static final int PLACEMENT_GAP = 16;
	private static final int CONTROL_PLACEMENT_GAP = 4;
	private static final int SCREEN_MARGIN = 4;
	private static final int EMPTY_BODY_HEIGHT = 28;
	private static final int SCROLLBAR_WIDTH = 3;
	private static final int CLOSE_BUTTON_WIDTH = 20;
	private static final Component CLOSE_LABEL = Component.literal("X");

	private final HudColorPicker colorPicker = new HudColorPicker();
	private Component title = Component.empty();
	private List<HudPopoverTab> tabs = List.of();
	private int selectedTab;
	private int x;
	private int y;
	private int width;
	private int height;
	private int scrollOffset;
	private int maxScroll;
	private int hoveredControl = -1;
	private int activeControl = -1;
	private int bodyHeight;
	private int visibleControlCount;
	private boolean open;
	private boolean positioned;
	private boolean placeBelowAnchor;
	private boolean dragging;
	private boolean draggingScrollbar;
	private int dragOffsetX;
	private int dragOffsetY;
	private int screenWidth;
	private int editorTop;
	private int editorBottom;

	public void open(Component title, List<HudPopoverTab> tabs) {
		open(title, tabs, false, 0);
	}

	public void openBelow(Component title, List<HudPopoverTab> tabs) {
		open(title, tabs, true, 0);
	}

	public void openBelow(Component title, List<HudPopoverTab> tabs, int selectedTab) {
		open(title, tabs, true, selectedTab);
	}

	private void open(Component title, List<HudPopoverTab> tabs, boolean placeBelowAnchor, int selectedTab) {
		releaseActiveControl();
		colorPicker.finish();
		this.title = title;
		this.tabs = List.copyOf(tabs);
		this.placeBelowAnchor = placeBelowAnchor;
		for (HudPopoverTab tab : this.tabs) {
			for (HudPopoverControl control : tab.controls()) {
				control.onAdded(this);
			}
		}
		this.selectedTab = Math.clamp(selectedTab, 0, Math.max(0, this.tabs.size() - 1));
		scrollOffset = 0;
		positioned = false;
		dragging = false;
		draggingScrollbar = false;
		open = true;
	}

	public void close() {
		releaseActiveControl();
		colorPicker.finish();
		tabs = List.of();
		selectedTab = 0;
		hoveredControl = -1;
		scrollOffset = 0;
		maxScroll = 0;
		positioned = false;
		placeBelowAnchor = false;
		dragging = false;
		draggingScrollbar = false;
		open = false;
	}

	public boolean isOpen() {
		return open;
	}

	public boolean isColorPickerOpen() {
		return colorPicker.isOpen();
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

		renderTabs(graphics, font, colors.getAccentColor(), mouseX, mouseY);
		List<HudPopoverControl> controls = controls();
		int contentTop = y + HEADER_HEIGHT + tabBarHeight();
		int contentBottom = y + height - PADDING;
		int controlWidth = width - PADDING * 2 - (maxScroll > 0 ? SCROLLBAR_WIDTH + 2 : 0);
		hoveredControl = -1;
		graphics.enableScissor(x + 1, contentTop, x + width - 1, contentBottom);
		if (visibleControlCount == 0) {
			graphics.centeredText(font, TranslationKey.POPOVER_NO_SETTINGS.component(),
				x + width / 2, contentTop + 9, 0xFFAAAAAA);
		} else {
			int controlY = contentTop + PADDING - scrollOffset;
			for (int i = 0; i < controls.size(); i++) {
				HudPopoverControl control = controls.get(i);
				if (!control.visible()) {
					continue;
				}
				boolean visible = controlY < contentBottom && controlY + control.height() > contentTop;
				boolean hovered = visible && contains(mouseX, mouseY, x + PADDING, controlY,
					controlWidth, control.height());
				if (hovered) {
					hoveredControl = i;
				}
				if (visible) {
					control.render(graphics, font, x + PADDING, controlY, controlWidth,
						hovered, colors.getAccentColor());
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
		colorPicker.render(graphics, font, mouseX, mouseY, x, y, width, screenWidth, editorBottom);
	}

	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		if (colorPicker.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (colorPicker.isOpen()) {
			colorPicker.finish();
		}
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

		int clickedTab = tabAt(mouseX, mouseY);
		if (button == 0 && clickedTab >= 0) {
			selectTab(clickedTab);
			return true;
		}

		List<HudPopoverControl> controls = controls();
		int contentTop = y + HEADER_HEIGHT + tabBarHeight();
		int contentBottom = y + height - PADDING;
		if (button == 0 && maxScroll > 0 && contains(mouseX, mouseY,
			x + width - PADDING - SCROLLBAR_WIDTH - 2, contentTop + PADDING,
			SCROLLBAR_WIDTH + 4, contentBottom - contentTop - PADDING * 2)) {
			releaseActiveControl();
			draggingScrollbar = true;
			setScrollFromMouse(mouseY, contentTop, contentBottom);
			return true;
		}
		int controlWidth = width - PADDING * 2 - (maxScroll > 0 ? SCROLLBAR_WIDTH + 2 : 0);
		int controlY = contentTop + PADDING - scrollOffset;
		releaseActiveControl();
		controls.forEach(HudPopoverControl::focusLost);
		for (int i = 0; i < controls.size(); i++) {
			HudPopoverControl control = controls.get(i);
			if (!control.visible()) {
				continue;
			}
			if (controlY < contentBottom && controlY + control.height() > contentTop
				&& control.mouseClicked(mouseX, mouseY, button, x + PADDING, controlY, controlWidth)) {
				List<HudPopoverControl> currentControls = controls();
				if (open && i < currentControls.size() && currentControls.get(i) == control) {
					activeControl = i;
				}
				return true;
			}
			controlY += control.height();
		}
		return true;
	}

	public boolean mouseDragged(MouseButtonEvent event) {
		double mouseX = event.x();
		double mouseY = event.y();
		if (colorPicker.mouseDragged(event)) {
			return true;
		}
		if (open && draggingScrollbar) {
			setScrollFromMouse(mouseY, y + HEADER_HEIGHT + tabBarHeight(), y + height - PADDING);
			return true;
		}
		if (open && dragging) {
			x = Math.clamp((int) mouseX - dragOffsetX, SCREEN_MARGIN,
				Math.max(SCREEN_MARGIN, screenWidth - SCREEN_MARGIN - width));
			y = Math.clamp((int) mouseY - dragOffsetY, editorTop,
				Math.max(editorTop, editorBottom - height));
			return true;
		}
		List<HudPopoverControl> controls = controls();
		if (!open || activeControl < 0 || activeControl >= controls.size()) {
			return false;
		}
		if (!controls.get(activeControl).visible()) {
			releaseActiveControl();
			return false;
		}

		int controlY = y + HEADER_HEIGHT + tabBarHeight() + PADDING - scrollOffset;
		for (int i = 0; i < activeControl; i++) {
			if (controls.get(i).visible()) {
				controlY += controls.get(i).height();
			}
		}
		int controlWidth = width - PADDING * 2 - (maxScroll > 0 ? SCROLLBAR_WIDTH + 2 : 0);
		return controls.get(activeControl).mouseDragged(mouseX, mouseY, x + PADDING, controlY, controlWidth);
	}

	public boolean mouseReleased() {
		if (colorPicker.mouseReleased()) {
			return true;
		}
		if (draggingScrollbar) {
			draggingScrollbar = false;
			return true;
		}
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
		return open && (contains(mouseX, mouseY, x, y, width, height)
			|| colorPicker.contains(mouseX, mouseY));
	}

	public boolean keyPressed(KeyEvent event) {
		if (colorPicker.keyPressed(event)) {
			return true;
		}
		for (HudPopoverControl control : controls()) {
			if (control.visible() && control.keyPressed(event)) {
				return true;
			}
		}
		return false;
	}

	public boolean charTyped(CharacterEvent event) {
		if (colorPicker.charTyped(event)) {
			return true;
		}
		for (HudPopoverControl control : controls()) {
			if (control.visible() && control.charTyped(event)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void openColorPicker(Component title, IntSupplier getter, IntConsumer setter) {
		colorPicker.open(title, getter, setter);
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
		List<HudPopoverControl> controls = controls();
		bodyHeight = PADDING * 2;
		visibleControlCount = 0;
		for (HudPopoverControl control : controls) {
			if (control.visible()) {
				bodyHeight += control.height();
				visibleControlCount++;
			}
		}
		if (visibleControlCount == 0) {
			bodyHeight = EMPTY_BODY_HEIGHT;
		}
		int fixedHeight = HEADER_HEIGHT + tabBarHeight();
		int availableHeight = Math.max(fixedHeight + PADDING * 2, editorBottom - editorTop);
		width = Math.clamp(screenWidth - SCREEN_MARGIN * 2, 80, WIDTH);
		height = Math.min(fixedHeight + bodyHeight, availableHeight);
		maxScroll = Math.max(0, fixedHeight + bodyHeight - height);
		scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);
		if (maxScroll == 0) {
			draggingScrollbar = false;
		}

		if (positioned) {
			x = Math.clamp(x, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenWidth - SCREEN_MARGIN - width));
			y = Math.clamp(y, editorTop, Math.max(editorTop, editorBottom - height));
			return;
		}
		positioned = true;

		if (placeBelowAnchor) {
			x = Math.clamp(moduleBounds.x(), SCREEN_MARGIN,
				Math.max(SCREEN_MARGIN, screenWidth - SCREEN_MARGIN - width));
			y = Math.clamp(moduleBounds.bottom() + CONTROL_PLACEMENT_GAP, editorTop,
				Math.max(editorTop, editorBottom - height));
			return;
		}

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

	private void renderTabs(GuiGraphicsExtractor graphics, Font font, int accentColor, int mouseX, int mouseY) {
		if (tabs.isEmpty()) {
			return;
		}
		int tabY = y + HEADER_HEIGHT;
		int innerX = x + 1;
		int innerWidth = width - 2;
		for (int i = 0; i < tabs.size(); i++) {
			int tabX = innerX + i * innerWidth / tabs.size();
			int tabRight = innerX + (i + 1) * innerWidth / tabs.size();
			boolean selected = i == selectedTab;
			boolean hovered = contains(mouseX, mouseY, tabX, tabY, tabRight - tabX, TAB_HEIGHT);
			int background = selected ? 0xFF333333 : hovered ? 0xFF303030 : 0xFF242424;
			graphics.fill(tabX, tabY, tabRight, tabY + TAB_HEIGHT, background);
			if (i > 0) {
				graphics.fill(tabX, tabY, tabX + 1, tabY + TAB_HEIGHT, 0xFF555555);
			}
			graphics.enableScissor(tabX + 2, tabY, tabRight - 2, tabY + TAB_HEIGHT);
			graphics.centeredText(font, tabs.get(i).title(), (tabX + tabRight) / 2, tabY + 7,
				selected ? accentColor : 0xFFCCCCCC);
			graphics.disableScissor();
			if (selected) {
				graphics.fill(tabX, tabY + TAB_HEIGHT - 2, tabRight, tabY + TAB_HEIGHT, accentColor);
			} else {
				graphics.fill(tabX, tabY + TAB_HEIGHT - 1, tabRight, tabY + TAB_HEIGHT, 0xFF555555);
			}
		}
	}

	private int tabAt(double mouseX, double mouseY) {
		if (tabs.isEmpty() || !contains(mouseX, mouseY, x + 1, y + HEADER_HEIGHT,
			width - 2, TAB_HEIGHT)) {
			return -1;
		}
		int relativeX = Math.clamp((int) mouseX - (x + 1), 0, width - 3);
		return Math.min(tabs.size() - 1, relativeX * tabs.size() / (width - 2));
	}

	private void selectTab(int index) {
		if (index == selectedTab || index < 0 || index >= tabs.size()) {
			return;
		}
		releaseActiveControl();
		colorPicker.finish();
		selectedTab = index;
		scrollOffset = 0;
		maxScroll = 0;
		hoveredControl = -1;
		draggingScrollbar = false;
	}

	private List<HudPopoverControl> controls() {
		if (tabs.isEmpty() || selectedTab < 0 || selectedTab >= tabs.size()) {
			return List.of();
		}
		return tabs.get(selectedTab).controls();
	}

	private int tabBarHeight() {
		return tabs.isEmpty() ? 0 : TAB_HEIGHT;
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

	private void setScrollFromMouse(double mouseY, int contentTop, int contentBottom) {
		int trackHeight = contentBottom - contentTop - PADDING * 2;
		int contentHeight = trackHeight + maxScroll;
		int thumbHeight = Math.max(12, trackHeight * trackHeight / contentHeight);
		int thumbTravel = Math.max(0, trackHeight - thumbHeight);
		if (thumbTravel == 0) {
			scrollOffset = 0;
			return;
		}
		int thumbTop = Math.clamp((int) Math.round(mouseY) - thumbHeight / 2,
			contentTop + PADDING, contentBottom - PADDING - thumbHeight);
		scrollOffset = (thumbTop - contentTop - PADDING) * maxScroll / thumbTravel;
	}

	private void releaseActiveControl() {
		List<HudPopoverControl> controls = controls();
		if (activeControl >= 0 && activeControl < controls.size()) {
			controls.get(activeControl).mouseReleased();
		}
		activeControl = -1;
	}

	private boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + 1, color);
		graphics.fill(x, y + height - 1, x + width, y + height, color);
		graphics.fill(x, y, x + 1, y + height, color);
		graphics.fill(x + width - 1, y, x + width, y + height, color);
	}
}
