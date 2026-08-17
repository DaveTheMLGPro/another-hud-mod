package net.davethemlgpro.client.module.containersearch;

import com.mojang.blaze3d.platform.InputConstants;
import net.davethemlgpro.client.AnotherHUDModClient;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.layout.HudLayoutEngine;
import net.davethemlgpro.client.mixin.AbstractContainerScreenAccessor;
import net.davethemlgpro.client.translation.TranslationKey;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class ContainerSearchOverlay {
	private static final Map<Screen, Controller> CONTROLLERS = new WeakHashMap<>();
	private static final ContainerSearchQueryState QUERY_STATE = new ContainerSearchQueryState();

	private ContainerSearchOverlay() {
	}

	public static void register() {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, minecraft) -> {
			QUERY_STATE.clear();
			CONTROLLERS.clear();
		});
		ScreenEvents.AFTER_INIT.register((minecraft, screen, scaledWidth, scaledHeight) -> {
			if (!supports(screen)) {
				return;
			}
			Controller controller = new Controller(minecraft, (AbstractContainerScreen<?>) screen);
			CONTROLLERS.put(screen, controller);
			ScreenEvents.remove(screen).register(removed -> {
				CONTROLLERS.remove(removed);
				controller.onRemoved();
			});
			ScreenMouseEvents.allowMouseClick(screen).register(controller::allowMouseClick);
			ScreenMouseEvents.allowMouseDrag(screen).register(controller::allowMouseDrag);
			ScreenMouseEvents.allowMouseRelease(screen).register(controller::allowMouseRelease);
			ScreenKeyboardEvents.allowKeyPress(screen).register(controller::allowKeyPress);
			ScreenKeyboardEvents.allowCharType(screen).register(controller::allowCharType);
		});
	}

	public static void renderBeforeTooltip(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY,
									   float tickProgress) {
		Controller controller = CONTROLLERS.get(screen);
		if (controller != null) {
			controller.render(screen, graphics, mouseX, mouseY, tickProgress);
		}
	}

	static boolean supports(Screen screen) {
		return screen instanceof AbstractContainerScreen<?>
			&& !(screen instanceof InventoryScreen)
			&& !(screen instanceof CreativeModeInventoryScreen);
	}

	private static final class Controller {
		private static final HudBounds LOCAL_PANEL = new HudBounds(0, 0,
			ContainerSearchHudModule.PANEL_WIDTH, ContainerSearchHudModule.PANEL_HEIGHT);

		private final Minecraft minecraft;
		private final AbstractContainerScreen<?> screen;
		private final HudLayoutEngine layoutEngine = new HudLayoutEngine();
		private final EditBox searchBox;
		private boolean mouseCaptured;
		private boolean dragging;
		private boolean clearPressed;
		private int dragOffsetX;
		private int dragOffsetY;

		private Controller(Minecraft minecraft, AbstractContainerScreen<?> screen) {
			this.minecraft = minecraft;
			this.screen = screen;
			this.searchBox = new EditBox(minecraft.font, 0, 0,
				ContainerSearchPanelRenderer.INPUT_WIDTH - ContainerSearchPanelRenderer.CLEAR_SIZE - 6,
				ContainerSearchPanelRenderer.INPUT_HEIGHT,
				TranslationKey.CONTAINER_SEARCH_INPUT.component());
			searchBox.setBordered(false);
			searchBox.setMaxLength(80);
			searchBox.setTextColor(ContainerSearchHudConfig.DEFAULT_TEXT_COLOR);
			searchBox.setHint(TranslationKey.CONTAINER_SEARCH_INPUT_HINT.component());
			searchBox.setCanLoseFocus(true);
			searchBox.setValue(QUERY_STATE.get());
			searchBox.setResponder(QUERY_STATE::set);
		}

		private void render(Screen ignored, GuiGraphicsExtractor graphics, int mouseX, int mouseY,
							float tickProgress) {
			if (!active()) {
				return;
			}
			ContainerSearchHudConfig config = config();
			HudBounds panel = panelBounds(config);
			positionSearchBox(config);
			List<Slot> searchableSlots = searchableSlots();
			String query = searchBox.getValue();
			List<Slot> matches = matchingSlots(searchableSlots, query, config.isExactMatch());

			if (!query.isBlank()) {
				renderSlotOverlays(graphics, searchableSlots, matches, config);
			}

			long itemCount = matches.stream().mapToLong(slot -> slot.getItem().getCount()).sum();
			Component status = query.isBlank() ? Component.empty()
				: (matches.size() == 1 && itemCount == 1
					? TranslationKey.CONTAINER_SEARCH_STATUS_SINGLE_ITEM.component()
					: matches.size() == 1
						? TranslationKey.CONTAINER_SEARCH_STATUS_ONE.component(itemCount)
						: TranslationKey.CONTAINER_SEARCH_STATUS_MANY.component(matches.size(), itemCount));
			double localMouseX = localX(panel, config, mouseX);
			double localMouseY = localY(panel, config, mouseY);
			boolean clearHovered = ContainerSearchPanelRenderer.clearButtonContains(
				LOCAL_PANEL, localMouseX, localMouseY);
			graphics.pose().pushMatrix();
			graphics.pose().translate(panel.x(), panel.y());
			float scale = (float) config.getUiScale();
			graphics.pose().scale(scale, scale);
			ContainerSearchPanelRenderer.renderPanel(graphics, minecraft, config, LOCAL_PANEL,
				Component.empty(), status, searchBox.isFocused(),
				ContainerSearchHudModule.sharedAccentColor(), clearHovered, clearPressed);
			graphics.pose().pushMatrix();
			graphics.pose().translate(0.0F, 6.0F);
			searchBox.extractRenderState(graphics,
				(int) localMouseX, (int) localMouseY, tickProgress);
			graphics.pose().popMatrix();
			graphics.pose().popMatrix();
		}

		private boolean allowMouseClick(Screen ignored, MouseButtonEvent event) {
			if (!active()) {
				return true;
			}
			HudBounds panel = panelBounds(config());
			mouseCaptured = panel.contains((int) event.x(), (int) event.y());
			if (!mouseCaptured) {
				clearPressed = false;
				searchBox.setFocused(false);
				return true;
			}

			ContainerSearchHudConfig config = config();
			double localX = localX(panel, config, event.x());
			double localY = localY(panel, config, event.y());
			if (event.button() == InputConstants.MOUSE_BUTTON_LEFT
				&& ContainerSearchPanelRenderer.clearButtonContains(LOCAL_PANEL, localX, localY)) {
				clearPressed = true;
				searchBox.setValue("");
				searchBox.setFocused(true);
				return false;
			}
			if (event.button() == InputConstants.MOUSE_BUTTON_LEFT
				&& ContainerSearchPanelRenderer.inputContains(LOCAL_PANEL, localX, localY)) {
				searchBox.setFocused(true);
				searchBox.onClick(new MouseButtonEvent(localX, localY, event.buttonInfo()), false);
				return false;
			}
			if (event.button() == InputConstants.MOUSE_BUTTON_LEFT
				&& ContainerSearchPanelRenderer.toggleContains(LOCAL_PANEL, localX, localY)) {
				config.setDimNonMatches(!config.isDimNonMatches());
				searchBox.setFocused(false);
				AnotherHUDModClient.getHudConfigManager().save();
				return false;
			}
			if (event.button() == InputConstants.MOUSE_BUTTON_LEFT
				&& ContainerSearchPanelRenderer.exactToggleContains(LOCAL_PANEL, localX, localY)) {
				config.setExactMatch(!config.isExactMatch());
				searchBox.setFocused(false);
				AnotherHUDModClient.getHudConfigManager().save();
				return false;
			}
			searchBox.setFocused(false);
			if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && event.hasAltDown()
				&& headerContains(LOCAL_PANEL, localX, localY)) {
				dragging = true;
				dragOffsetX = (int) event.x() - panel.x();
				dragOffsetY = (int) event.y() - panel.y();
			}
			return false;
		}

		private boolean allowMouseDrag(Screen ignored, MouseButtonEvent event,
								   double horizontalAmount, double verticalAmount) {
			if (!active() || !mouseCaptured) {
				return true;
			}
			if (dragging) {
				ContainerSearchHudConfig config = config();
				layoutEngine.applyDrag(config.getLayout(), ContainerSearchHudModule.panelSize(config),
					(int) event.x() - dragOffsetX, (int) event.y() - dragOffsetY,
					screen.width, screen.height);
			}
			return false;
		}

		private boolean allowMouseRelease(Screen ignored, MouseButtonEvent event) {
			if (!active() || !mouseCaptured) {
				return true;
			}
			boolean save = dragging;
			dragging = false;
			clearPressed = false;
			mouseCaptured = false;
			if (save) {
				AnotherHUDModClient.getHudConfigManager().save();
			}
			return false;
		}

		private boolean allowKeyPress(Screen ignored, KeyEvent event) {
			if (!active() || !searchBox.isFocused()) {
				return true;
			}
			searchBox.keyPressed(event);
			// Escape may close the container. Every other key stays local so hotbar shortcuts and
			// other handled-screen actions cannot run while the search field has focus.
			return event.isEscape();
		}

		private boolean allowCharType(Screen ignored, CharacterEvent event) {
			if (!active() || !searchBox.isFocused()) {
				return true;
			}
			return !searchBox.charTyped(event);
		}

		private List<Slot> searchableSlots() {
			if (minecraft.player == null) {
				return List.of();
			}
			List<Slot> result = new ArrayList<>();
			for (Slot slot : screen.getMenu().slots) {
				if (slot.isActive() && slot.container != minecraft.player.getInventory()) {
					result.add(slot);
				}
			}
			return result;
		}

		private static List<Slot> matchingSlots(List<Slot> slots, String query, boolean exactMatch) {
			if (query == null || query.isBlank()) {
				return List.of();
			}
			return slots.stream()
				.filter(slot -> ContainerSearchMatcher.matches(slot.getItem(), query, exactMatch))
				.toList();
		}

		private void renderSlotOverlays(GuiGraphicsExtractor graphics, List<Slot> slots, List<Slot> matches,
								ContainerSearchHudConfig config) {
			AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) screen;
			int left = accessor.anotherHudMod$getLeftPos();
			int top = accessor.anotherHudMod$getTopPos();
			for (Slot slot : slots) {
				int x = left + slot.x;
				int y = top + slot.y;
				if (matches.contains(slot)) {
					renderMatchHighlight(graphics, x, y, config);
				} else if (config.isDimNonMatches()) {
					graphics.fill(x, y, x + 16, y + 16, config.getDimColor());
				}
			}
		}

		private static void renderMatchHighlight(GuiGraphicsExtractor graphics, int x, int y,
										 ContainerSearchHudConfig config) {
			int rgb = config.getHighlightColor() & 0x00FFFFFF;
			switch (config.getHighlightStyle()) {
				case OUTLINE -> graphics.outline(x - 1, y - 1, 18, 18, 0xFF000000 | rgb);
				case FILLED -> {
					graphics.fill(x, y, x + 16, y + 16, 0x78000000 | rgb);
					graphics.outline(x - 1, y - 1, 18, 18, 0xFF000000 | rgb);
				}
				case PULSE -> {
					double wave = (Math.sin(System.nanoTime() / 250_000_000.0D) + 1.0D) * 0.5D;
					int alpha = 80 + (int) Math.round(wave * 175.0D);
					graphics.fill(x, y, x + 16, y + 16, ((alpha / 3) << 24) | rgb);
					graphics.outline(x - 1, y - 1, 18, 18, (alpha << 24) | rgb);
				}
				case CORNERS -> renderCorners(graphics, x - 1, y - 1, 18, 18, 0xFF000000 | rgb);
			}
		}

		private static void renderCorners(GuiGraphicsExtractor graphics, int x, int y,
									  int width, int height, int color) {
			int right = x + width;
			int bottom = y + height;
			graphics.fill(x, y, x + 5, y + 1, color);
			graphics.fill(x, y, x + 1, y + 5, color);
			graphics.fill(right - 5, y, right, y + 1, color);
			graphics.fill(right - 1, y, right, y + 5, color);
			graphics.fill(x, bottom - 1, x + 5, bottom, color);
			graphics.fill(x, bottom - 5, x + 1, bottom, color);
			graphics.fill(right - 5, bottom - 1, right, bottom, color);
			graphics.fill(right - 1, bottom - 5, right, bottom, color);
		}

		private void onRemoved() {
			if (config().isClearOnClose()) {
				QUERY_STATE.clear();
			}
		}

		private HudBounds panelBounds(ContainerSearchHudConfig config) {
			return HudLayoutEngine.resolve(config.getLayout(), ContainerSearchHudModule.panelSize(config),
				screen.width, screen.height);
		}

		private void positionSearchBox(ContainerSearchHudConfig config) {
			searchBox.setX(ContainerSearchPanelRenderer.INPUT_X + 4);
			searchBox.setY(ContainerSearchPanelRenderer.INPUT_Y);
			searchBox.setTextColor(config.getTextColor());
		}

		private static boolean headerContains(HudBounds panel, double mouseX, double mouseY) {
			return mouseX >= panel.x() && mouseX < panel.right()
				&& mouseY >= panel.y() && mouseY < panel.y() + 22;
		}

		private static double localX(HudBounds panel, ContainerSearchHudConfig config, double screenX) {
			return (screenX - panel.x()) / config.getUiScale();
		}

		private static double localY(HudBounds panel, ContainerSearchHudConfig config, double screenY) {
			return (screenY - panel.y()) / config.getUiScale();
		}

		private boolean active() {
			return AnotherHUDModClient.getHudConfigManager() != null
				&& AnotherHUDModClient.getHudModuleRegistry().isModuleEnabled(ContainerSearchHudModule.ID)
				&& config().visible();
		}

		private ContainerSearchHudConfig config() {
			return AnotherHUDModClient.getContainerSearchHudConfig();
		}
	}
}
