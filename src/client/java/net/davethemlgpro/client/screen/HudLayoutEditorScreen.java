package net.davethemlgpro.client.screen;

import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.config.EditorConfig;
import net.davethemlgpro.client.config.HudEditSession;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudRenderDispatcher;
import net.davethemlgpro.client.hud.layout.HudLayoutEngine;
import net.davethemlgpro.client.module.HudModuleConfig;
import net.davethemlgpro.client.module.HudModuleEntry;
import net.davethemlgpro.client.module.HudModuleRegistry;
import net.davethemlgpro.client.screen.popover.HudModulePopover;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class HudLayoutEditorScreen extends Screen {
	private static final int FOOTER_HEIGHT = 36;
	private static final int BUTTON_HEIGHT = 20;
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
		int count = Math.min(registry.getEntries().size(), renderDispatcher.getTrackedModuleCount());
		for (int i = 0; i < count; i++) {
			HudBounds bounds = renderDispatcher.getLastBounds(i);
			if (bounds != null) {
				HudModuleConfig<?> config = configAt(i);
				HudSelectionRenderer.render(graphics, bounds, config.enabled(), i == selectedModule,
					i == hoveredModule, colors, mouseX, mouseY, width, height);
			}
		}

		HudBounds selectedBounds = selectedModule >= 0 ? renderDispatcher.getLastBounds(selectedModule) : null;
		if (selectedBounds != null && popover.isOpen()) {
			popover.render(graphics, font, selectedBounds, colors, mouseX, mouseY,
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
		int clickedModule = event.button() == 0 && event.y() < height - FOOTER_HEIGHT
			? findModuleAt((int) event.x(), (int) event.y()) : -1;
		int visibilityModule = event.button() == 0 && event.y() < height - FOOTER_HEIGHT
			? findVisibilityButtonAt((int) event.x(), (int) event.y()) : -1;
		boolean clickedSelectedHud = clickedModule >= 0 && clickedModule == selectedModule
			&& visibilityModule < 0;
		if (popover.isOpen() && !popover.contains(event.x(), event.y()) && !clickedSelectedHud) {
			popover.close();
		}
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (popover.mouseClicked(event.x(), event.y(), event.button())) {
			dragging = false;
			saveFailed = false;
			return true;
		}
		if (event.button() != 0 || event.y() >= height - FOOTER_HEIGHT) {
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
		if (popover.mouseDragged(event.x(), event.y())) {
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
		int requestedY = Math.clamp((int) event.y() - dragOffsetY, 0,
			Math.max(0, height - FOOTER_HEIGHT - bounds.height()));
		int resolvedX = Math.clamp(requestedX, 0, Math.max(0, width - bounds.width()));
		int resolvedY = Math.clamp(requestedY, 0, Math.max(0, height - bounds.height()));
		popover.moveBy(resolvedX - lastDragX, resolvedY - lastDragY);
		lastDragX = resolvedX;
		lastDragY = resolvedY;
		layoutEngine.applyDragOffset(config.getLayout(), bounds.width(), bounds.height(),
			requestedX, requestedY, width, height);
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
		saveFailed = false;
	}

	private void openSelectedPopover() {
		HudModuleEntry<?> entry = selectedEntry();
		HudModuleConfig<?> config = selectedConfig();
		if (entry == null || config == null) {
			popover.close();
			return;
		}
		popover.open(entry.getModule().displayName(), entry.createPopoverTabsUntyped(config));
	}
}
