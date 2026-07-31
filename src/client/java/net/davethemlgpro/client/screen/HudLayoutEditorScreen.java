package net.davethemlgpro.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.config.EditorConfig;
import net.davethemlgpro.client.config.HudEditSession;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudRenderDispatcher;
import net.davethemlgpro.client.hud.HudRenderedElement;
import net.davethemlgpro.client.hud.layout.HudLayoutEngine;
import net.davethemlgpro.client.hud.layout.HudPlacementConstraints;
import net.davethemlgpro.client.module.HudModuleConfig;
import net.davethemlgpro.client.module.HudModuleEntry;
import net.davethemlgpro.client.module.HudModuleRegistry;
import net.davethemlgpro.client.screen.popover.HudModulePopover;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class HudLayoutEditorScreen extends Screen {
	private static final int FOOTER_HEIGHT = 36;
	private static final int BUTTON_HEIGHT = 20;
	private static final int SETTINGS_BUTTON_X = 6;
	private static final int SETTINGS_BUTTON_Y = 6;
	private static final int SETTINGS_BUTTON_SIZE = 20;
	private static final int SETTINGS_ICON_SIZE = 16;
	private static final int GLOBAL_MODULES_TAB = 2;
	private static final Component INSTRUCTIONS = TranslationKey.EDITOR_INSTRUCTIONS.component();
	private static final Component SAVE_FAILED = TranslationKey.EDITOR_SAVE_FAILED.component();

	private final Screen parent;
	private final HudEditSession session;
	private final HudModuleRegistry registry;
	private final HudRenderDispatcher renderDispatcher;
	private final HudLayoutEngine layoutEngine = new HudLayoutEngine();
	private final HudModulePopover popover = new HudModulePopover();

	private int selectedModule = -1;
	private int selectedElement = -1;
	private int hoveredModule = -1;
	private int hoveredElement = -1;
	private boolean dragging;
	private int dragOffsetX;
	private int dragOffsetY;
	private int lastDragX;
	private int lastDragY;
	private boolean saveFailed;
	private boolean globalSettingsOpen;

	public HudLayoutEditorScreen(Screen parent, HudEditSession session, HudModuleRegistry registry,
								 HudRenderDispatcher renderDispatcher) {
		super(TranslationKey.EDITOR_TITLE.component());
		this.parent = parent;
		this.session = session;
		this.registry = registry;
		this.renderDispatcher = renderDispatcher;
	}

	@Override
	protected void init() {
		HudIconButton settingsButton = new HudIconButton(
			SETTINGS_BUTTON_X, SETTINGS_BUTTON_Y, SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE,
			TranslationKey.EDITOR_SETTINGS_OPEN.component(), button -> openGlobalSettings(),
			AnotherHUDMod.id("textures/settings_icon.png"), SETTINGS_ICON_SIZE);
		settingsButton.setTooltip(Tooltip.create(TranslationKey.EDITOR_SETTINGS_OPEN.component()));
		addRenderableWidget(settingsButton);
		int footerY = height - 28;
		addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> saveAndClose())
			.bounds(width / 2 - 154, footerY, 100, BUTTON_HEIGHT).build());
		addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
			.bounds(width / 2 - 50, footerY, 100, BUTTON_HEIGHT).build());
		addRenderableWidget(Button.builder(TranslationKey.EDITOR_RESET.component(),
			button -> resetDraft()).bounds(width / 2 + 54, footerY, 100, BUTTON_HEIGHT).build());
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, width, height, 0x4D000000);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		normalizeSelection();
		HudRenderedElement hovered = findElementAt(mouseX, mouseY);
		hoveredModule = hovered == null ? -1 : hovered.moduleIndex();
		hoveredElement = hovered == null ? -1 : hovered.elementIndex();
		EditorConfig colors = session.getDraft().getRawEditor();
		if (isGridActive()) {
			HudGridRenderer.render(graphics, width, height, height / 2, colors);
		}
		if (dragging) {
			HudProtectedRegionRenderer.render(graphics, font, protectedRegion());
		}
		for (HudRenderedElement element : renderDispatcher.getLastElements()) {
			HudModuleConfig<?> config = configAt(element.moduleIndex());
			HudModuleEntry<?> entry = registry.getEntries().get(element.moduleIndex());
			boolean selected = element.moduleIndex() == selectedModule
				&& element.elementIndex() == selectedElement;
			boolean isHovered = element.moduleIndex() == hoveredModule
				&& element.elementIndex() == hoveredElement;
			boolean visible = session.getDraft().isModuleEnabled(entry.getModule().id())
				&& entry.elementVisibleUntyped(config, element.elementIndex());
			HudSelectionRenderer.render(graphics, element.bounds(), visible, selected,
				isHovered, colors, mouseX, mouseY, width, height);
		}

		HudBounds popoverAnchor = popoverAnchor();
		if (popoverAnchor != null && popover.isOpen()) {
			popover.render(graphics, font, popoverAnchor, colors, mouseX, mouseY,
				width, 32, height - FOOTER_HEIGHT);
		}

		graphics.centeredText(font, title, width / 2, 8, 0xFFFFFFFF);
		graphics.centeredText(font, INSTRUCTIONS, width / 2, 19, 0xFFCCCCCC);
		if (saveFailed) {
			graphics.centeredText(font, SAVE_FAILED, width / 2, height - FOOTER_HEIGHT - 12, 0xFFFF5555);
		}
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		HudRenderedElement clickedElement = event.button() == 0
			? findElementAt((int) event.x(), (int) event.y()) : null;
		HudRenderedElement visibilityElement = event.button() == 0
			? findVisibilityButtonAt((int) event.x(), (int) event.y()) : null;
		boolean clickedSelectedHud = clickedElement != null
			&& clickedElement.moduleIndex() == selectedModule
			&& clickedElement.elementIndex() == selectedElement
			&& visibilityElement == null && !globalSettingsOpen;
		if (popover.isOpen() && !popover.contains(event.x(), event.y()) && !clickedSelectedHud) {
			popover.close();
			globalSettingsOpen = false;
		}
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (popover.mouseClicked(event, doubleClick)) {
			dragging = false;
			saveFailed = false;
			return true;
		}
		if (event.button() != 0) {
			return false;
		}

		if (visibilityElement != null) {
			HudModuleConfig<?> config = configAt(visibilityElement.moduleIndex());
			HudModuleEntry<?> entry = registry.getEntries().get(visibilityElement.moduleIndex());
			boolean visible = entry.elementVisibleUntyped(config, visibilityElement.elementIndex());
			entry.setElementVisibleUntyped(config, visibilityElement.elementIndex(), !visible);
			selectedModule = visibilityElement.moduleIndex();
			selectedElement = visibilityElement.elementIndex();
			dragging = false;
			saveFailed = false;
			return true;
		}

		boolean selectionChanged = clickedElement == null
			? selectedModule >= 0
			: clickedElement.moduleIndex() != selectedModule || clickedElement.elementIndex() != selectedElement;
		selectedModule = clickedElement == null ? -1 : clickedElement.moduleIndex();
		selectedElement = clickedElement == null ? -1 : clickedElement.elementIndex();
		if (selectedModule >= 0 && event.hasShiftDown() && (selectionChanged || !popover.isOpen())) {
			openSelectedPopover();
		}
		saveFailed = false;
		if (selectedModule < 0) {
			dragging = false;
			return false;
		}

		if (clickedElement == null) {
			return false;
		}
		HudBounds bounds = clickedElement.bounds();
		dragging = true;
		dragOffsetX = (int) event.x() - bounds.x();
		dragOffsetY = (int) event.y() - bounds.y();
		lastDragX = bounds.x();
		lastDragY = bounds.y();
		return true;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (popover.mouseDragged(event)) {
			return true;
		}
		if (!dragging || selectedModule < 0) {
			return super.mouseDragged(event, dragX, dragY);
		}

		HudRenderedElement selected = selectedRenderedElement();
		HudModuleConfig<?> config = selectedConfig();
		HudModuleEntry<?> entry = selectedEntry();
		if (selected == null || config == null || entry == null) {
			return true;
		}
		HudBounds bounds = selected.bounds();

		int requestedX = (int) event.x() - dragOffsetX;
		int requestedY = (int) event.y() - dragOffsetY;
		int gridSpacing = isGridActive() ? HudGridRenderer.MINOR_SPACING : 1;
		HudBounds resolved = layoutEngine.applyConstrainedDragOffset(
			entry.elementLayoutUntyped(config, selectedElement),
			bounds.width(), bounds.height(), requestedX, requestedY,
			width, height, protectedRegion(), gridSpacing);
		popover.moveBy(resolved.x() - lastDragX, resolved.y() - lastDragY);
		lastDragX = resolved.x();
		lastDragY = resolved.y();
		return true;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (popover.mouseReleased()) {
			return true;
		}
		if (event.button() == 0 && dragging) {
			dragging = false;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (popover.mouseScrolled(mouseX, mouseY, scrollY)) {
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (popover.keyPressed(event)) {
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (popover.charTyped(event)) {
			return true;
		}
		return super.charTyped(event);
	}

	@Override
	public void onClose() {
		session.cancelEdit();
		minecraft.gui.setScreen(parent);
	}

	@Override
	public void removed() {
		if (HudEditSession.getActive() == session) {
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

	private HudRenderedElement findElementAt(int x, int y) {
		var elements = renderDispatcher.getLastElements();
		for (int i = elements.size() - 1; i >= 0; i--) {
			HudRenderedElement element = elements.get(i);
			if (HudSelectionRenderer.containsSelection(element.bounds(), x, y)) {
				return element;
			}
		}
		return null;
	}

	private HudRenderedElement findVisibilityButtonAt(int x, int y) {
		var elements = renderDispatcher.getLastElements();
		for (int i = elements.size() - 1; i >= 0; i--) {
			HudRenderedElement element = elements.get(i);
			if (HudSelectionRenderer.containsVisibilityButton(element.bounds(), x, y, width, height)) {
				return element;
			}
		}
		return null;
	}

	private HudModuleConfig<?> selectedConfig() {
		if (selectedModule < 0 || selectedModule >= registry.getEntries().size()) {
			return null;
		}
		return configAt(selectedModule);
	}

	private HudModuleEntry<?> selectedEntry() {
		if (selectedModule < 0 || selectedModule >= registry.getEntries().size()) {
			return null;
		}
		return registry.getEntries().get(selectedModule);
	}

	private HudModuleConfig<?> configAt(int index) {
		HudModuleEntry<?> entry = registry.getEntries().get(index);
		return session.getDraft().getRawConfig(entry.getModule().id());
	}

	private void saveAndClose() {
		if (session.applyAndSave()) {
			minecraft.gui.setScreen(parent);
		} else {
			saveFailed = true;
			AnotherHUDMod.LOGGER.warn("HUD editor changes could not be saved.");
		}
	}

	private void resetDraft() {
		session.resetToOpeningState();
		selectedElement = selectedModule >= 0 ? 0 : -1;
		popover.close();
		globalSettingsOpen = false;
		saveFailed = false;
	}

	private void openSelectedPopover() {
		HudModuleEntry<?> entry = selectedEntry();
		HudModuleConfig<?> config = selectedConfig();
		if (entry == null || config == null) {
			popover.close();
			globalSettingsOpen = false;
			return;
		}
		globalSettingsOpen = false;
		popover.open(entry.getModule().displayName(), entry.createPopoverTabsUntyped(config));
	}

	private void openGlobalSettings() {
		openGlobalSettings(0);
	}

	private void openGlobalSettings(int selectedTab) {
		dragging = false;
		globalSettingsOpen = true;
		EditorConfig config = session.getDraft().getRawEditor();
		popover.openBelow(TranslationKey.EDITOR_SETTINGS_TITLE.component(),
			EditorGlobalSettingsPopover.create(config, session.getDraft(), registry,
				this::resetModuleToDefaults, this::resetAllModulesToDefaults), selectedTab);
		saveFailed = false;
	}

	private void resetModuleToDefaults(Identifier moduleId) {
		session.resetModuleToDefaults(moduleId);
		normalizeSelectionAfterModuleReset(moduleId);
		openGlobalSettings(GLOBAL_MODULES_TAB);
	}

	private void resetAllModulesToDefaults() {
		session.resetModulesToDefaults();
		selectedElement = selectedModule >= 0 ? 0 : -1;
		openGlobalSettings(GLOBAL_MODULES_TAB);
	}

	private void normalizeSelectionAfterModuleReset(Identifier moduleId) {
		if (selectedModule >= 0
			&& registry.getEntries().get(selectedModule).getModule().id().equals(moduleId)) {
			selectedElement = 0;
		}
	}

	private HudBounds popoverAnchor() {
		if (globalSettingsOpen) {
			return new HudBounds(SETTINGS_BUTTON_X, SETTINGS_BUTTON_Y,
				SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE);
		}
		HudRenderedElement selected = selectedRenderedElement();
		return selected == null ? null : selected.bounds();
	}

	private HudRenderedElement selectedRenderedElement() {
		return selectedModule < 0 ? null
			: renderDispatcher.getLastElement(selectedModule, selectedElement);
	}

	private void normalizeSelection() {
		if (selectedModule < 0 || selectedRenderedElement() != null) {
			return;
		}
		for (HudRenderedElement element : renderDispatcher.getLastElements()) {
			if (element.moduleIndex() == selectedModule) {
				selectedElement = element.elementIndex();
				return;
			}
		}
		selectedModule = -1;
		selectedElement = -1;
		if (!globalSettingsOpen) {
			popover.close();
		}
	}

	private boolean isGridActive() {
		return InputConstants.isKeyDown(minecraft.getWindow(), InputConstants.KEY_LALT);
	}

	private HudBounds protectedRegion() {
		return HudPlacementConstraints.vanillaHudRegion(width, height);
	}
}
