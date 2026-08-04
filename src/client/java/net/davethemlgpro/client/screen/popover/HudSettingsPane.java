package net.davethemlgpro.client.screen.popover;

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

/** Renders module-owned popover controls inside a fixed screen pane. */
public final class HudSettingsPane implements HudPopoverContext {
	private static final int TAB_HEIGHT = 22;
	private static final int PADDING = 5;
	private static final int SCROLLBAR_WIDTH = 3;

	private final HudColorPicker colorPicker = new HudColorPicker();
	private List<HudPopoverTab> tabs = List.of();
	private int selectedTab;
	private int scrollOffset;
	private int maxScroll;
	private int hoveredControl = -1;
	private int activeControl = -1;
	private boolean draggingScrollbar;
	private int x;
	private int y;
	private int width;
	private int height;

	public void setTabs(List<HudPopoverTab> tabs, int selectedTab) {
		releaseActiveControl();
		colorPicker.finish();
		this.tabs = List.copyOf(tabs);
		for (HudPopoverTab tab : this.tabs) {
			for (HudPopoverControl control : tab.controls()) {
				control.onAdded(this);
			}
		}
		this.selectedTab = Math.clamp(selectedTab, 0, Math.max(0, this.tabs.size() - 1));
		scrollOffset = 0;
		maxScroll = 0;
		hoveredControl = -1;
		draggingScrollbar = false;
	}

	public int selectedTab() {
		return selectedTab;
	}

	public boolean isColorPickerOpen() {
		return colorPicker.isOpen();
	}

	public void closeColorPicker() {
		colorPicker.finish();
	}

	public int colorPickerWidth() {
		return HudColorPicker.width();
	}

	public int colorPickerHeight() {
		return HudColorPicker.height();
	}

	public void render(GuiGraphicsExtractor graphics, Font font, int x, int y, int width, int height,
					   int accentColor, int mouseX, int mouseY) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		layout();
		renderTabs(graphics, font, accentColor, mouseX, mouseY);

		int contentTop = y + tabBarHeight();
		int contentBottom = y + height;
		int controlWidth = width - PADDING * 2 - (maxScroll > 0 ? SCROLLBAR_WIDTH + 2 : 0);
		List<HudPopoverControl> controls = controls();
		hoveredControl = -1;
		graphics.enableScissor(x, contentTop, x + width, contentBottom);
		if (visibleControlCount() == 0) {
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
					control.render(graphics, font, x + PADDING, controlY, controlWidth, hovered, accentColor);
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

	public void renderDockedColorPicker(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY,
									int x, int y) {
		colorPicker.renderDocked(graphics, font, mouseX, mouseY, x, y);
	}

	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (colorPicker.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (colorPicker.isOpen()) {
			colorPicker.finish();
		}
		if (!contains(event.x(), event.y(), x, y, width, height)) {
			return false;
		}
		int clickedTab = tabAt(event.x(), event.y());
		if (event.button() == 0 && clickedTab >= 0) {
			selectTab(clickedTab);
			return true;
		}

		int contentTop = y + tabBarHeight();
		int contentBottom = y + height;
		if (event.button() == 0 && maxScroll > 0 && contains(event.x(), event.y(),
			x + width - PADDING - SCROLLBAR_WIDTH - 2, contentTop + PADDING,
			SCROLLBAR_WIDTH + 4, contentBottom - contentTop - PADDING * 2)) {
			releaseActiveControl();
			draggingScrollbar = true;
			setScrollFromMouse(event.y(), contentTop, contentBottom);
			return true;
		}

		List<HudPopoverControl> controls = controls();
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
				&& control.mouseClicked(event.x(), event.y(), event.button(), x + PADDING, controlY, controlWidth)) {
				if (i < controls().size() && controls().get(i) == control) {
					activeControl = i;
				}
				return true;
			}
			controlY += control.height();
		}
		return true;
	}

	public boolean mouseDragged(MouseButtonEvent event) {
		if (colorPicker.mouseDragged(event)) {
			return true;
		}
		if (draggingScrollbar) {
			setScrollFromMouse(event.y(), y + tabBarHeight(), y + height);
			return true;
		}
		List<HudPopoverControl> controls = controls();
		if (activeControl < 0 || activeControl >= controls.size() || !controls.get(activeControl).visible()) {
			return false;
		}
		int controlY = y + tabBarHeight() + PADDING - scrollOffset;
		for (int i = 0; i < activeControl; i++) {
			if (controls.get(i).visible()) {
				controlY += controls.get(i).height();
			}
		}
		int controlWidth = width - PADDING * 2 - (maxScroll > 0 ? SCROLLBAR_WIDTH + 2 : 0);
		return controls.get(activeControl).mouseDragged(event.x(), event.y(), x + PADDING, controlY, controlWidth);
	}

	public boolean mouseReleased() {
		if (colorPicker.mouseReleased()) {
			return true;
		}
		if (draggingScrollbar) {
			draggingScrollbar = false;
			return true;
		}
		if (activeControl < 0) {
			return false;
		}
		releaseActiveControl();
		return true;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
		if (maxScroll <= 0 || !contains(mouseX, mouseY, x, y, width, height)) {
			return false;
		}
		scrollOffset = Math.clamp(scrollOffset - (int) Math.round(scrollY * HudPopoverControl.DEFAULT_HEIGHT),
			0, maxScroll);
		return true;
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

	private void layout() {
		int contentHeight = PADDING * 2;
		for (HudPopoverControl control : controls()) {
			if (control.visible()) {
				contentHeight += control.height();
			}
		}
		maxScroll = Math.max(0, tabBarHeight() + contentHeight - height);
		scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);
		if (maxScroll == 0) {
			draggingScrollbar = false;
		}
	}

	private int visibleControlCount() {
		int count = 0;
		for (HudPopoverControl control : controls()) {
			if (control.visible()) {
				count++;
			}
		}
		return count;
	}

	private void renderTabs(GuiGraphicsExtractor graphics, Font font, int accentColor, int mouseX, int mouseY) {
		if (tabs.isEmpty()) {
			return;
		}
		for (int i = 0; i < tabs.size(); i++) {
			int tabX = x + i * width / tabs.size();
			int tabRight = x + (i + 1) * width / tabs.size();
			boolean selected = i == selectedTab;
			boolean hovered = contains(mouseX, mouseY, tabX, y, tabRight - tabX, TAB_HEIGHT);
			graphics.fill(tabX, y, tabRight, y + TAB_HEIGHT,
				selected ? 0xFF333333 : hovered ? 0xFF303030 : 0xFF242424);
			if (i > 0) {
				graphics.fill(tabX, y, tabX + 1, y + TAB_HEIGHT, 0xFF555555);
			}
			graphics.enableScissor(tabX + 2, y, tabRight - 2, y + TAB_HEIGHT);
			graphics.centeredText(font, tabs.get(i).title(), (tabX + tabRight) / 2, y + 7,
				selected ? accentColor : 0xFFCCCCCC);
			graphics.disableScissor();
			graphics.fill(tabX, y + TAB_HEIGHT - (selected ? 2 : 1), tabRight, y + TAB_HEIGHT,
				selected ? accentColor : 0xFF555555);
		}
	}

	private int tabAt(double mouseX, double mouseY) {
		if (tabs.isEmpty() || !contains(mouseX, mouseY, x, y, width, TAB_HEIGHT)) {
			return -1;
		}
		int relativeX = Math.clamp((int) mouseX - x, 0, width - 1);
		return Math.min(tabs.size() - 1, relativeX * tabs.size() / width);
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
		return tabs.isEmpty() ? List.of() : tabs.get(selectedTab).controls();
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

	private static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}
}
