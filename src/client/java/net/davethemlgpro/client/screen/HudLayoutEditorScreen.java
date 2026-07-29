package net.davethemlgpro.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.config.EditorConfig;
import net.davethemlgpro.client.config.HudEditSession;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudRenderDispatcher;
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

public final class HudLayoutEditorScreen extends Screen {
	private static final int FOOTER_HEIGHT = 36;
	private static final int BUTTON_HEIGHT = 20;
	private static final int SETTINGS_BUTTON_X = 6;
	private static final int SETTINGS_BUTTON_Y = 6;
	private static final int SETTINGS_BUTTON_SIZE = 20;
	private static final Component SETTINGS_BUTTON_LABEL = Component.literal("\u2699");
	private static final Component INSTRUCTIONS = TranslationKey.EDITOR_INSTRUCTIONS.component();
	private static final Component SAVE_FAILED = TranslationKey.EDITOR_SAVE_FAILED.component();

	private final Screen parent;
	private final HudEditSession session;
	private final HudModuleRegistry registry;
	private final HudRenderDispatcher renderDispatcher;
	private final HudLayoutEngine layoutEngine = new HudLayoutEngine();
	private final HudModulePopover popover = new HudModulePopover();

	private int selectedModule = -1;
	private int hoveredModule = -1;
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
		addRenderableWidget(Button.builder(SETTINGS_BUTTON_LABEL, button -> openGlobalSettings())
			.bounds(SETTINGS_BUTTON_X, SETTINGS_BUTTON_Y, SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE)
			.tooltip(Tooltip.create(TranslationKey.EDITOR_SETTINGS_OPEN.component()))
			.build());
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
		hoveredModule = findModuleAt(mouseX, mouseY);
		EditorConfig colors = session.getDraft().getRawEditor();
		if (isGridActive()) {
			HudGridRenderer.render(graphics, width, height, height / 2, colors);
		}
		if (dragging) {
			HudProtectedRegionRenderer.render(graphics, font, protectedRegion());
		}
		int count = Math.min(registry.getEntries().size(), renderDispatcher.getTrackedModuleCount());
		for (int i = 0; i < count; i++) {
			HudBounds bounds = renderDispatcher.getLastBounds(i);
			if (bounds != null) {
				HudModuleConfig<?> config = configAt(i);
				HudSelectionRenderer.render(graphics, bounds, config.enabled(), i == selectedModule,
					i == hoveredModule, colors, mouseX, mouseY, width, height);
			}
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
		int clickedModule = event.button() == 0
			? findModuleAt((int) event.x(), (int) event.y()) : -1;
		int visibilityModule = event.button() == 0
			? findVisibilityButtonAt((int) event.x(), (int) event.y()) : -1;
		boolean clickedSelectedHud = clickedModule >= 0 && clickedModule == selectedModule
			&& visibilityModule < 0 && !globalSettingsOpen;
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

		if (visibilityModule >= 0) {
			HudModuleConfig<?> config = configAt(visibilityModule);
			config.setEnabled(!config.enabled());
			selectedModule = visibilityModule;
			dragging = false;
			saveFailed = false;
			return true;
		}

		boolean selectionChanged = clickedModule != selectedModule;
		selectedModule = clickedModule;
		if (selectedModule >= 0 && event.hasShiftDown() && (selectionChanged || !popover.isOpen())) {
			openSelectedPopover();
		}
		saveFailed = false;
		if (selectedModule < 0) {
			dragging = false;
			return false;
		}

		HudBounds bounds = renderDispatcher.getLastBounds(selectedModule);
		if (bounds == null) {
			return false;
		}
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

		HudBounds bounds = renderDispatcher.getLastBounds(selectedModule);
		HudModuleConfig<?> config = selectedConfig();
		if (bounds == null || config == null) {
			return true;
		}

		int requestedX = (int) event.x() - dragOffsetX;
		int requestedY = (int) event.y() - dragOffsetY;
		int gridSpacing = isGridActive() ? HudGridRenderer.MINOR_SPACING : 1;
		HudBounds resolved = layoutEngine.applyConstrainedDragOffset(
			config.getLayout(), bounds.width(), bounds.height(), requestedX, requestedY,
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

	private int findModuleAt(int x, int y) {
		int count = Math.min(registry.getEntries().size(), renderDispatcher.getTrackedModuleCount());
		for (int i = count - 1; i >= 0; i--) {
			HudBounds bounds = renderDispatcher.getLastBounds(i);
			if (bounds != null && HudSelectionRenderer.containsSelection(bounds, x, y)) {
				return i;
			}
		}
		return -1;
	}

	private int findVisibilityButtonAt(int x, int y) {
		int count = Math.min(registry.getEntries().size(), renderDispatcher.getTrackedModuleCount());
		for (int i = count - 1; i >= 0; i--) {
			HudBounds bounds = renderDispatcher.getLastBounds(i);
			if (bounds != null && HudSelectionRenderer.containsVisibilityButton(bounds, x, y, width, height)) {
				return i;
			}
		}
		return -1;
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
		dragging = false;
		globalSettingsOpen = true;
		EditorConfig config = session.getDraft().getRawEditor();
		popover.open(TranslationKey.EDITOR_SETTINGS_TITLE.component(), EditorGridSettingsPopover.create(config));
		saveFailed = false;
	}

	private HudBounds popoverAnchor() {
		if (globalSettingsOpen) {
			return new HudBounds(SETTINGS_BUTTON_X, SETTINGS_BUTTON_Y,
				SETTINGS_BUTTON_SIZE, SETTINGS_BUTTON_SIZE);
		}
		return selectedModule >= 0 ? renderDispatcher.getLastBounds(selectedModule) : null;
	}

	private boolean isGridActive() {
		return InputConstants.isKeyDown(minecraft.getWindow(), InputConstants.KEY_LALT);
	}

	private HudBounds protectedRegion() {
		return HudPlacementConstraints.vanillaHudRegion(width, height);
	}
}
