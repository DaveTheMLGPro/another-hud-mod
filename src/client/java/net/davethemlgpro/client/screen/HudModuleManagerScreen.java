package net.davethemlgpro.client.screen;

import net.davethemlgpro.client.config.EditorConfig;
import net.davethemlgpro.client.config.HudConfigSnapshot;
import net.davethemlgpro.client.config.HudEditSession;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudSize;
import net.davethemlgpro.client.module.HudModuleConfig;
import net.davethemlgpro.client.module.HudModuleEntry;
import net.davethemlgpro.client.module.HudModuleRegistry;
import net.davethemlgpro.client.screen.popover.HudSettingsPane;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HudModuleManagerScreen extends Screen {
	private static final int SCREEN_MARGIN = 6;
	private static final int MAX_PANEL_WIDTH = 540;
	private static final int MAX_PANEL_HEIGHT = 380;
	private static final int HEADER_HEIGHT = 28;
	private static final int FOOTER_HEIGHT = 36;
	private static final int LEFT_WIDTH = 210;
	private static final int PADDING = 6;
	private static final int MODULE_HEADER_HEIGHT = 22;
	private static final int MODULE_ROW_HEIGHT = 30;
	private static final int TOGGLE_WIDTH = 40;
	private static final int BUTTON_HEIGHT = 20;
	private static final int DETAIL_HEADER_HEIGHT = 28;
	private static final int SETTINGS_PREVIEW_HEIGHT = 70;
	private static final int COMPACT_SETTINGS_PREVIEW_HEIGHT = 36;
	private static final int MIN_SETTINGS_PANE_HEIGHT = 80;
	private static final int OVERVIEW_PREVIEW_HEIGHT = 112;
	private static final int SCROLLBAR_WIDTH = 3;
	private static final int DISABLED_OVERLAY = 0x88000000;
	private static final Component BACK_LABEL = Component.literal("<");

	private final Screen parent;
	private final HudEditSession session;
	private final HudModuleRegistry registry;
	private final HudConfigSnapshot openingState;
	private final HudSettingsPane settingsPane = new HudSettingsPane();
	private final Map<Identifier, Integer> selectedTabs = new HashMap<>();

	private DetailMode detailMode = DetailMode.OVERVIEW;
	private int selectedModule;
	private int moduleScroll;
	private int maxModuleScroll;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;
	private int leftWidth;
	private int contentTop;
	private int contentBottom;
	private int moduleListTop;
	private int moduleListBottom;
	private int rightX;
	private int rightWidth;
	private int overviewButtonY;
	private boolean draggingModuleScrollbar;
	private boolean returningToParent;

	public HudModuleManagerScreen(Screen parent, HudEditSession session, HudModuleRegistry registry) {
		super(TranslationKey.MODULE_MANAGER_TITLE.component());
		this.parent = parent;
		this.session = session;
		this.registry = registry;
		openingState = session.getDraft().copy();
	}

	@Override
	protected void init() {
		layoutScreen();
		int footerY = panelY + panelHeight - FOOTER_HEIGHT + 8;
		int buttonWidth = Math.min(200, (panelWidth - PADDING * 3) / 2);
		int center = panelX + panelWidth / 2;
		addRenderableWidget(new HudPanelButton(center - buttonWidth - 2, footerY, buttonWidth, BUTTON_HEIGHT,
			CommonComponents.GUI_CANCEL, button -> cancelAndReturn(),
			() -> session.getDraft().getRawEditor().getAccentColor(), false));
		addRenderableWidget(new HudPanelButton(center + 2, footerY, buttonWidth, BUTTON_HEIGHT,
			CommonComponents.GUI_DONE, button -> returnToParent(),
			() -> session.getDraft().getRawEditor().getAccentColor(), true));
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, width, height, 0x66000000);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		layoutScreen();
		EditorConfig colors = session.getDraft().getRawEditor();
		graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF0202020);
		drawBorder(graphics, panelX, panelY, panelWidth, panelHeight, 0xFF707070);
		graphics.fill(panelX + 1, panelY + HEADER_HEIGHT - 1, panelX + panelWidth - 1,
			panelY + HEADER_HEIGHT, 0xFF555555);
		graphics.centeredText(font, title, panelX + panelWidth / 2, panelY + 9, 0xFFFFFFFF);
		graphics.fill(panelX + leftWidth, contentTop, panelX + leftWidth + 1, contentBottom, 0xFF555555);
		graphics.fill(panelX + 1, contentBottom, panelX + panelWidth - 1, contentBottom + 1, 0xFF555555);

		renderModuleList(graphics, colors.getAccentColor(), mouseX, mouseY);
		if (detailMode == DetailMode.OVERVIEW) {
			renderOverview(graphics, colors.getAccentColor(), mouseX, mouseY);
		} else {
			renderSettings(graphics, colors.getAccentColor(), mouseX, mouseY);
		}
		if (settingsPane.isColorPickerOpen()) {
			int pickerX = panelX + (leftWidth - settingsPane.colorPickerWidth()) / 2;
			int pickerY = contentBottom - PADDING - settingsPane.colorPickerHeight();
			settingsPane.renderDockedColorPicker(graphics, font, mouseX, mouseY, pickerX, pickerY);
		}
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (detailMode == DetailMode.SETTINGS && settingsPane.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (event.button() != 0) {
			return false;
		}
		if (maxModuleScroll > 0 && contains(event.x(), event.y(),
			panelX + leftWidth - PADDING - SCROLLBAR_WIDTH - 2, moduleListTop,
			SCROLLBAR_WIDTH + 4, moduleListBottom - moduleListTop)) {
			settingsPane.closeColorPicker();
			draggingModuleScrollbar = true;
			setModuleScrollFromMouse(event.y());
			return true;
		}

		int moduleIndex = moduleAt(event.x(), event.y());
		if (moduleIndex >= 0) {
			HudModuleEntry<?> entry = registry.getEntries().get(moduleIndex);
			int toggleX = panelX + leftWidth - PADDING - 4 - TOGGLE_WIDTH;
			int rowY = moduleListTop + moduleIndex * MODULE_ROW_HEIGHT - moduleScroll;
			settingsPane.closeColorPicker();
			if (contains(event.x(), event.y(), toggleX, rowY + 6, TOGGLE_WIDTH, 18)) {
				Identifier id = entry.getModule().id();
				session.getDraft().setModuleEnabled(id, !session.getDraft().isModuleEnabled(id));
				return true;
			}
			if (moduleIndex != selectedModule) {
				rememberSelectedTab();
				selectedModule = moduleIndex;
				detailMode = DetailMode.OVERVIEW;
			}
			return true;
		}
		if (contains(event.x(), event.y(), statusButtonX(), contentTop + 5,
			statusButtonWidth(), 18)) {
			settingsPane.closeColorPicker();
			toggleSelectedModule();
			return true;
		}

		if (detailMode == DetailMode.OVERVIEW) {
			if (contains(event.x(), event.y(), rightX + PADDING, overviewButtonY,
				(rightWidth - PADDING * 3) / 2, BUTTON_HEIGHT)) {
				openSettings();
				return true;
			}
			if (contains(event.x(), event.y(), rightX + PADDING * 2 + (rightWidth - PADDING * 3) / 2,
				overviewButtonY, (rightWidth - PADDING * 3) / 2, BUTTON_HEIGHT)) {
				resetSelectedModule();
				return true;
			}
		} else {
			if (contains(event.x(), event.y(), rightX + PADDING, contentTop + 4, 20, 20)) {
				rememberSelectedTab();
				settingsPane.closeColorPicker();
				detailMode = DetailMode.OVERVIEW;
				return true;
			}
			if (contains(event.x(), event.y(), rightX + PADDING, contentBottom - 26,
				rightWidth - PADDING * 2, BUTTON_HEIGHT)) {
				resetSelectedModule();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (draggingModuleScrollbar) {
			setModuleScrollFromMouse(event.y());
			return true;
		}
		return detailMode == DetailMode.SETTINGS && settingsPane.mouseDragged(event)
			|| super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (draggingModuleScrollbar) {
			draggingModuleScrollbar = false;
			return true;
		}
		return detailMode == DetailMode.SETTINGS && settingsPane.mouseReleased()
			|| super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (detailMode == DetailMode.SETTINGS && settingsPane.mouseScrolled(mouseX, mouseY, scrollY)) {
			return true;
		}
		if (maxModuleScroll > 0 && contains(mouseX, mouseY, panelX + PADDING, moduleListTop,
			leftWidth - PADDING * 2, moduleListBottom - moduleListTop)) {
			moduleScroll = Math.clamp(moduleScroll - (int) Math.round(scrollY * MODULE_ROW_HEIGHT),
				0, maxModuleScroll);
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (detailMode == DetailMode.SETTINGS && settingsPane.keyPressed(event)) {
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (detailMode == DetailMode.SETTINGS && settingsPane.charTyped(event)) {
			return true;
		}
		return super.charTyped(event);
	}

	@Override
	public void onClose() {
		cancelAndReturn();
	}

	@Override
	public void removed() {
		if (!returningToParent && HudEditSession.getActive() == session) {
			session.cancelEdit();
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean isInGameUi() {
		return true;
	}

	private void layoutScreen() {
		panelWidth = Math.min(MAX_PANEL_WIDTH, width - SCREEN_MARGIN * 2);
		panelHeight = Math.min(MAX_PANEL_HEIGHT, height - SCREEN_MARGIN * 2);
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;
		leftWidth = Math.min(LEFT_WIDTH, Math.max(150, panelWidth - 280));
		contentTop = panelY + HEADER_HEIGHT;
		contentBottom = panelY + panelHeight - FOOTER_HEIGHT;
		rightX = panelX + leftWidth + 1;
		rightWidth = panelWidth - leftWidth - 1;
		moduleListTop = contentTop + MODULE_HEADER_HEIGHT;
		moduleListBottom = contentBottom - PADDING;
		if (settingsPane.isColorPickerOpen()) {
			moduleListBottom -= settingsPane.colorPickerHeight() + PADDING;
		}
		int moduleContentHeight = registry.getEntries().size() * MODULE_ROW_HEIGHT;
		maxModuleScroll = Math.max(0, moduleContentHeight - Math.max(0, moduleListBottom - moduleListTop));
		moduleScroll = Math.clamp(moduleScroll, 0, maxModuleScroll);
	}

	private void renderModuleList(GuiGraphicsExtractor graphics, int accentColor, int mouseX, int mouseY) {
		int enabled = 0;
		for (HudModuleEntry<?> entry : registry.getEntries()) {
			if (session.getDraft().isModuleEnabled(entry.getModule().id())) {
				enabled++;
			}
		}
		Component count = TranslationKey.MODULE_MANAGER_COUNT.component(enabled, registry.getEntries().size());
		graphics.text(font, count, panelX + PADDING, contentTop + 7, 0xFFCCCCCC);
		graphics.enableScissor(panelX + 1, moduleListTop, panelX + leftWidth, moduleListBottom);
		List<HudModuleEntry<?>> entries = registry.getEntries();
		for (int index = 0; index < entries.size(); index++) {
			HudModuleEntry<?> entry = entries.get(index);
			int rowY = moduleListTop + index * MODULE_ROW_HEIGHT - moduleScroll;
			if (rowY >= moduleListBottom || rowY + MODULE_ROW_HEIGHT <= moduleListTop) {
				continue;
			}
			boolean selected = index == selectedModule;
			boolean hovered = contains(mouseX, mouseY, panelX + PADDING, rowY,
				leftWidth - PADDING * 2, MODULE_ROW_HEIGHT - 2);
			int rowX = panelX + PADDING;
			int rowWidth = leftWidth - PADDING * 2;
			graphics.fill(rowX, rowY, rowX + rowWidth, rowY + MODULE_ROW_HEIGHT - 2,
				selected ? 0xFF283238 : hovered ? 0xFF303030 : 0xFF252525);
			drawBorder(graphics, rowX, rowY, rowWidth, MODULE_ROW_HEIGHT - 2,
				selected ? accentColor : 0xFF444444);
			graphics.enableScissor(rowX + 6, rowY, rowX + rowWidth - TOGGLE_WIDTH - 10,
				rowY + MODULE_ROW_HEIGHT - 2);
			graphics.text(font, entry.getModule().displayName(), rowX + 6, rowY + 10, 0xFFFFFFFF);
			graphics.disableScissor();
			boolean moduleEnabled = session.getDraft().isModuleEnabled(entry.getModule().id());
			int toggleX = panelX + leftWidth - PADDING - 4 - TOGGLE_WIDTH;
			renderButton(graphics, moduleEnabled ? Component.literal("ON") : Component.literal("OFF"),
				toggleX, rowY + 6, TOGGLE_WIDTH, 18,
				contains(mouseX, mouseY, toggleX, rowY + 6, TOGGLE_WIDTH, 18),
				moduleEnabled ? accentColor : 0xFF777777, false);
		}
		graphics.disableScissor();
		if (maxModuleScroll > 0) {
			renderModuleScrollbar(graphics);
		}
	}

	private void renderOverview(GuiGraphicsExtractor graphics, int accentColor, int mouseX, int mouseY) {
		HudModuleEntry<?> entry = selectedEntry();
		if (entry == null) {
			return;
		}
		Identifier id = entry.getModule().id();
		boolean enabled = session.getDraft().isModuleEnabled(id);
		graphics.enableScissor(rightX + PADDING, contentTop,
			statusButtonX() - 4, contentTop + DETAIL_HEADER_HEIGHT);
		graphics.text(font, entry.getModule().displayName(), rightX + PADDING, contentTop + 9, 0xFFFFFFFF);
		graphics.disableScissor();
		renderEnabledButton(graphics, enabled, accentColor, mouseX, mouseY);

		int descriptionY = contentTop + DETAIL_HEADER_HEIGHT + 3;
		int lineY = descriptionY;
		for (var line : font.split(entry.getModule().description(), rightWidth - PADDING * 2)) {
			graphics.text(font, line, rightX + PADDING, lineY, 0xFFCCCCCC);
			lineY += font.lineHeight;
		}
		int previewY = Math.max(contentTop + 58, lineY + 7);
		int availablePreviewHeight = contentBottom - PADDING - BUTTON_HEIGHT - 8 - previewY;
		int previewHeight = Math.max(0, Math.min(OVERVIEW_PREVIEW_HEIGHT, availablePreviewHeight));
		if (previewHeight > 0) {
			renderPreview(graphics, entry, rightX + PADDING, previewY,
				rightWidth - PADDING * 2, previewHeight, enabled);
		}
		int buttonWidth = (rightWidth - PADDING * 3) / 2;
		overviewButtonY = previewY + previewHeight + 8;
		renderButton(graphics, TranslationKey.MODULE_MANAGER_EDIT_SETTINGS.component(),
			rightX + PADDING, overviewButtonY, buttonWidth, BUTTON_HEIGHT,
			contains(mouseX, mouseY, rightX + PADDING, overviewButtonY, buttonWidth, BUTTON_HEIGHT),
			accentColor, false);
		renderButton(graphics, TranslationKey.MODULE_MANAGER_RESET.component(),
			rightX + PADDING * 2 + buttonWidth, overviewButtonY, buttonWidth, BUTTON_HEIGHT,
			contains(mouseX, mouseY, rightX + PADDING * 2 + buttonWidth, overviewButtonY,
				buttonWidth, BUTTON_HEIGHT), 0xFFFF5555, true);
	}

	private void renderSettings(GuiGraphicsExtractor graphics, int accentColor, int mouseX, int mouseY) {
		HudModuleEntry<?> entry = selectedEntry();
		if (entry == null) {
			return;
		}
		renderButton(graphics, BACK_LABEL, rightX + PADDING, contentTop + 4, 20, 20,
			contains(mouseX, mouseY, rightX + PADDING, contentTop + 4, 20, 20), accentColor, false);
		Component breadcrumb = entry.getModule().displayName().copy().append("  >  ")
			.append(TranslationKey.MODULE_MANAGER_SETTINGS.component());
		graphics.enableScissor(rightX + PADDING + 28, contentTop,
			statusButtonX() - 4, contentTop + DETAIL_HEADER_HEIGHT);
		graphics.text(font, breadcrumb, rightX + PADDING + 28, contentTop + 10, 0xFFFFFFFF);
		graphics.disableScissor();
		boolean enabled = session.getDraft().isModuleEnabled(entry.getModule().id());
		renderEnabledButton(graphics, enabled, accentColor, mouseX, mouseY);
		int resetY = contentBottom - 26;
		int previewY = contentTop + DETAIL_HEADER_HEIGHT;
		int availableHeight = resetY - PADDING - previewY;
		int previewHeight = settingsPreviewHeight(availableHeight);
		if (previewHeight > 0) {
			renderPreview(graphics, entry, rightX + PADDING, previewY,
				rightWidth - PADDING * 2, previewHeight, enabled);
		}

		int paneY = previewY + (previewHeight > 0 ? previewHeight + PADDING : 0);
		int paneHeight = resetY - PADDING - paneY;
		settingsPane.render(graphics, font, rightX + PADDING, paneY,
			rightWidth - PADDING * 2, paneHeight, accentColor, mouseX, mouseY);
		renderButton(graphics, TranslationKey.MODULE_MANAGER_RESET.component(),
			rightX + PADDING, resetY, rightWidth - PADDING * 2, BUTTON_HEIGHT,
			contains(mouseX, mouseY, rightX + PADDING, resetY,
				rightWidth - PADDING * 2, BUTTON_HEIGHT), 0xFFFF5555, true);
	}

	private void renderPreview(GuiGraphicsExtractor graphics, HudModuleEntry<?> entry,
						   int x, int y, int width, int height, boolean enabled) {
		graphics.fill(x, y, x + width, y + height, 0xFF303030);
		drawBorder(graphics, x, y, width, height, 0xFF777777);
		HudModuleConfig<?> config = session.getDraft().getRawConfig(entry.getModule().id());
		HudSize size = entry.measureEditorPreviewUntyped(minecraft, config);
		if (size.width() > 0 && size.height() > 0) {
			float scale = Math.min((width - 16.0F) / size.width(), (height - 12.0F) / size.height());
			scale = Math.min(scale, 2.0F);
			float previewX = x + (width - size.width() * scale) / 2.0F;
			float previewY = y + (height - size.height() * scale) / 2.0F;
			var matrices = graphics.pose();
			matrices.pushMatrix();
			try {
				matrices.translate(previewX, previewY);
				matrices.scale(scale, scale);
				entry.renderEditorPreviewUntyped(graphics, minecraft.getDeltaTracker(), minecraft, config,
					new HudBounds(0, 0, size.width(), size.height()));
			} finally {
				matrices.popMatrix();
			}
		}
		if (!enabled) {
			graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, DISABLED_OVERLAY);
			graphics.centeredText(font, TranslationKey.MODULE_MANAGER_DISABLED.component(),
				x + width / 2, y + height / 2 - 4, 0xFFCCCCCC);
		}
	}

	private int settingsPreviewHeight(int availableHeight) {
		if (availableHeight >= SETTINGS_PREVIEW_HEIGHT + PADDING + MIN_SETTINGS_PANE_HEIGHT) {
			return SETTINGS_PREVIEW_HEIGHT;
		}
		if (availableHeight >= COMPACT_SETTINGS_PREVIEW_HEIGHT + PADDING + MIN_SETTINGS_PANE_HEIGHT) {
			return COMPACT_SETTINGS_PREVIEW_HEIGHT;
		}
		return 0;
	}

	private void openSettings() {
		HudModuleEntry<?> entry = selectedEntry();
		if (entry == null || !entry.hasPopoverControls()) {
			return;
		}
		detailMode = DetailMode.SETTINGS;
		Identifier id = entry.getModule().id();
		settingsPane.setTabs(entry.createPopoverTabsUntyped(session.getDraft().getRawConfig(id)),
			selectedTabs.getOrDefault(id, 0));
	}

	private void rememberSelectedTab() {
		HudModuleEntry<?> entry = selectedEntry();
		if (detailMode == DetailMode.SETTINGS && entry != null) {
			selectedTabs.put(entry.getModule().id(), settingsPane.selectedTab());
		}
	}

	private void resetSelectedModule() {
		HudModuleEntry<?> entry = selectedEntry();
		if (entry == null) {
			return;
		}
		int tab = settingsPane.selectedTab();
		settingsPane.closeColorPicker();
		session.resetModuleToDefaults(entry.getModule().id());
		if (detailMode == DetailMode.SETTINGS) {
			settingsPane.setTabs(entry.createPopoverTabsUntyped(
				session.getDraft().getRawConfig(entry.getModule().id())), tab);
		}
	}

	private HudModuleEntry<?> selectedEntry() {
		List<HudModuleEntry<?>> entries = registry.getEntries();
		return selectedModule >= 0 && selectedModule < entries.size() ? entries.get(selectedModule) : null;
	}

	private int moduleAt(double mouseX, double mouseY) {
		if (!contains(mouseX, mouseY, panelX + PADDING, moduleListTop,
			leftWidth - PADDING * 2, moduleListBottom - moduleListTop)) {
			return -1;
		}
		int index = ((int) mouseY - moduleListTop + moduleScroll) / MODULE_ROW_HEIGHT;
		return index >= 0 && index < registry.getEntries().size() ? index : -1;
	}

	private void renderModuleScrollbar(GuiGraphicsExtractor graphics) {
		int trackHeight = moduleListBottom - moduleListTop;
		int contentHeight = trackHeight + maxModuleScroll;
		int thumbHeight = Math.max(12, trackHeight * trackHeight / contentHeight);
		int thumbTravel = trackHeight - thumbHeight;
		int thumbY = moduleListTop + moduleScroll * thumbTravel / maxModuleScroll;
		int scrollbarX = panelX + leftWidth - PADDING - SCROLLBAR_WIDTH;
		graphics.fill(scrollbarX, moduleListTop, scrollbarX + SCROLLBAR_WIDTH, moduleListBottom, 0xFF151515);
		graphics.fill(scrollbarX, thumbY, scrollbarX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xFFAAAAAA);
	}

	private void setModuleScrollFromMouse(double mouseY) {
		int trackHeight = moduleListBottom - moduleListTop;
		int contentHeight = trackHeight + maxModuleScroll;
		int thumbHeight = Math.max(12, trackHeight * trackHeight / contentHeight);
		int thumbTravel = trackHeight - thumbHeight;
		if (thumbTravel <= 0) {
			moduleScroll = 0;
			return;
		}
		int thumbTop = Math.clamp((int) Math.round(mouseY) - thumbHeight / 2,
			moduleListTop, moduleListBottom - thumbHeight);
		moduleScroll = (thumbTop - moduleListTop) * maxModuleScroll / thumbTravel;
	}

	private void renderEnabledButton(GuiGraphicsExtractor graphics, boolean enabled, int accentColor,
									 int mouseX, int mouseY) {
		Component status = enabled ? TranslationKey.MODULE_MANAGER_ENABLED.component()
			: TranslationKey.MODULE_MANAGER_DISABLED.component();
		int buttonX = statusButtonX();
		renderButton(graphics, status, buttonX, contentTop + 5, statusButtonWidth(), 18,
			contains(mouseX, mouseY, buttonX, contentTop + 5, statusButtonWidth(), 18),
			enabled ? accentColor : 0xFF777777, false);
	}

	private int statusButtonWidth() {
		return Math.max(font.width(TranslationKey.MODULE_MANAGER_ENABLED.component()),
			font.width(TranslationKey.MODULE_MANAGER_DISABLED.component())) + 12;
	}

	private int statusButtonX() {
		return rightX + rightWidth - PADDING - statusButtonWidth();
	}

	private void toggleSelectedModule() {
		HudModuleEntry<?> entry = selectedEntry();
		if (entry == null) {
			return;
		}
		Identifier id = entry.getModule().id();
		session.getDraft().setModuleEnabled(id, !session.getDraft().isModuleEnabled(id));
	}

	private void renderButton(GuiGraphicsExtractor graphics, Component label, int x, int y,
						  int width, int height, boolean hovered, int accentColor, boolean destructive) {
		int border = destructive ? 0xFF883333 : accentColor;
		int background = hovered ? destructive ? 0x55331111 : withAlpha(accentColor, 0x33) : 0xFF292929;
		graphics.fill(x, y, x + width, y + height, background);
		drawBorder(graphics, x, y, width, height, border);
		graphics.centeredText(font, label, x + width / 2, y + (height - 8) / 2,
			destructive ? 0xFFFF5555 : 0xFFFFFFFF);
	}

	private void cancelAndReturn() {
		session.getDraft().copyFrom(openingState);
		returnToParent();
	}

	private void returnToParent() {
		returningToParent = true;
		minecraft.gui.setScreen(parent);
	}

	private static int withAlpha(int color, int alpha) {
		return alpha << 24 | color & 0x00FFFFFF;
	}

	private static boolean contains(double mouseX, double mouseY, int x, int y, int width, int height) {
		return width > 0 && height > 0 && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private static void drawBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x, y, x + width, y + 1, color);
		graphics.fill(x, y + height - 1, x + width, y + height, color);
		graphics.fill(x, y, x + 1, y + height, color);
		graphics.fill(x + width - 1, y, x + width, y + height, color);
	}

	private enum DetailMode {
		OVERVIEW,
		SETTINGS
	}
}
