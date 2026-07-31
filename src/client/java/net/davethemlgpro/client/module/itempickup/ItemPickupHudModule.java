package net.davethemlgpro.client.module.itempickup;

import net.davethemlgpro.AnotherHUDMod;
import net.davethemlgpro.client.AnotherHUDModClient;
import net.davethemlgpro.client.hud.HudBounds;
import net.davethemlgpro.client.hud.HudSize;
import net.davethemlgpro.client.module.HudModule;
import net.davethemlgpro.client.translation.TranslationKey;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public final class ItemPickupHudModule implements HudModule<ItemPickupHudConfig> {
	public static final Identifier ID = AnotherHUDMod.id("item_pickup");
	private static final Component DISPLAY_NAME = TranslationKey.MODULE_ITEM_PICKUP.component();
	private static final int MAX_TRACKED_TOASTS = ItemPickupHudConfig.MAX_VISIBLE_ITEMS;
	private final ItemPickupToastQueue<ItemStack> toasts = new ItemPickupToastQueue<>(MAX_TRACKED_TOASTS);
	private ClientLevel currentLevel;

	@Override
	public Identifier id() {
		return ID;
	}

	@Override
	public Component displayName() {
		return DISPLAY_NAME;
	}

	@Override
	public synchronized HudSize measure(Minecraft minecraft, ItemPickupHudConfig config) {
		List<ItemPickupToastQueue.Entry<ItemStack>> active = activeToasts(minecraft, config, System.nanoTime());
		return active.isEmpty() ? new HudSize(0, 0) : previewSize(minecraft, config);
	}

	@Override
	public HudSize measureEditorPreview(Minecraft minecraft, ItemPickupHudConfig config) {
		return previewSize(minecraft, config);
	}

	@Override
	public synchronized void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft,
									ItemPickupHudConfig config, HudBounds bounds) {
		long nowNanos = System.nanoTime();
		renderToasts(graphics, minecraft, config, bounds,
			activeToasts(minecraft, config, nowNanos), nowNanos, false);
	}

	@Override
	public void renderEditorPreview(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft,
									ItemPickupHudConfig config, HudBounds bounds) {
		renderToasts(graphics, minecraft, config, bounds, editorToasts(), 0L, true);
	}

	public synchronized void recordPickup(ItemStack source, int amount, ClientLevel level) {
		if (source.isEmpty() || amount <= 0) {
			return;
		}
		resetForLevel(level);
		ItemStack displayStack = source.copyWithCount(1);
		toasts.record(displayStack, amount, System.nanoTime(),
			secondsToNanos(AnotherHUDModClient.getItemPickupHudConfig().getMergeWindowSeconds()),
			ItemStack::isSameItemSameComponents);
	}

	private List<ItemPickupToastQueue.Entry<ItemStack>> activeToasts(Minecraft minecraft,
																	 ItemPickupHudConfig config,
																	 long nowNanos) {
		resetForLevel(minecraft.level);
		return toasts.snapshot(config.getMaxVisibleItems(), nowNanos,
			secondsToNanos(config.getDisplayTimeSeconds()),
			secondsToNanos(config.getRemoveDelaySeconds()),
			secondsToNanos(config.getFadeDurationSeconds()),
			config.getRemovalMode() == ItemPickupRemovalMode.FADE_OUT);
	}

	private void resetForLevel(ClientLevel level) {
		if (currentLevel == level) {
			return;
		}
		currentLevel = level;
		toasts.clear();
	}

	private HudSize measureToasts(Minecraft minecraft,
								  List<ItemPickupToastQueue.Entry<ItemStack>> visibleToasts,
								  ItemPickupHudConfig config, int rowCapacity) {
		int widestText = 0;
		for (ItemPickupToastQueue.Entry<ItemStack> toast : visibleToasts) {
			widestText = Math.max(widestText, minecraft.font.width(label(toast, config)));
		}
		int minimumWidth = config.getStyle() == ItemPickupHudStyle.COMPACT
			? ItemPickupToastLayout.COMPACT_MIN_WIDTH : ItemPickupToastLayout.NORMAL_MIN_WIDTH;
		return ItemPickupToastLayout.measure(rowCapacity, widestText, minimumWidth);
	}

	private HudSize previewSize(Minecraft minecraft, ItemPickupHudConfig config) {
		List<ItemPickupToastQueue.Entry<ItemStack>> preview = editorToasts();
		return measureToasts(minecraft, preview, config, preview.size());
	}

	private void renderToasts(GuiGraphicsExtractor graphics, Minecraft minecraft, ItemPickupHudConfig config,
							  HudBounds bounds,
							  List<ItemPickupToastQueue.Entry<ItemStack>> visibleToasts,
							  long nowNanos, boolean editorPreview) {
		int firstRowY = ItemPickupToastLayout.bottomAlignedFirstRowY(bounds.height(), visibleToasts.size());
		int renderWidth = Math.max(bounds.width(),
			measureToasts(minecraft, visibleToasts, config, visibleToasts.size()).width());
		if (config.getBackgroundStyle() == ItemPickupBackgroundStyle.UNIFIED_PANEL && !visibleToasts.isEmpty()) {
			int stackHeight = ItemPickupToastLayout.measure(visibleToasts.size(), 0).height();
			float panelOpacity = visibleToasts.size() == 1
				? toastOpacity(visibleToasts.getFirst(), config, nowNanos, editorPreview) : 1.0F;
			drawRoundedBackground(graphics, bounds.x(), bounds.y() + firstRowY, renderWidth, stackHeight,
				withOpacity(config.getBackgroundColor(), panelOpacity));
		}
		for (int index = 0; index < visibleToasts.size(); index++) {
			ItemPickupToastQueue.Entry<ItemStack> toast = visibleToasts.get(index);
			float opacity = toastOpacity(toast, config, nowNanos, editorPreview);
			int rowY = bounds.y() + firstRowY + ItemPickupToastLayout.rowY(index);
			if (config.getBackgroundStyle() == ItemPickupBackgroundStyle.TINTED_ROWS) {
				drawRoundedBackground(graphics, bounds.x(), rowY, renderWidth, ItemPickupToastLayout.ROW_HEIGHT,
					withOpacity(config.getBackgroundColor(), opacity));
			}
			drawItem(graphics, toast.value(), bounds.x() + ItemPickupToastLayout.ICON_LEFT,
				rowY + ItemPickupToastLayout.ICON_TOP, opacity);
			graphics.text(minecraft.font, label(toast, config), bounds.x() + ItemPickupToastLayout.TEXT_LEFT,
				rowY + ItemPickupToastLayout.TEXT_TOP, withOpacity(config.getTextColor(), opacity));
		}
	}

	private float toastOpacity(ItemPickupToastQueue.Entry<ItemStack> toast, ItemPickupHudConfig config,
							   long nowNanos, boolean editorPreview) {
		return editorPreview ? 1.0F : toasts.opacity(toast, nowNanos,
			secondsToNanos(config.getDisplayTimeSeconds()),
			secondsToNanos(config.getFadeDurationSeconds()),
			config.getRemovalMode() == ItemPickupRemovalMode.FADE_OUT);
	}

	private static void drawRoundedBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
											  int color) {
		graphics.fill(x + 2, y, x + width - 2, y + height, color);
		graphics.fill(x, y + 2, x + width, y + height - 2, color);
		graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, color);
	}

	private static void drawItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, float opacity) {
		if (opacity >= 0.999F) {
			graphics.item(stack, x, y);
			return;
		}
		if (opacity <= 0.001F) {
			return;
		}
		var matrices = graphics.pose();
		matrices.pushMatrix();
		try {
			float center = ItemPickupToastLayout.ITEM_SIZE / 2.0F;
			matrices.translate(x + center, y + center);
			matrices.scale(opacity, opacity);
			graphics.item(stack, -ItemPickupToastLayout.ITEM_SIZE / 2,
				-ItemPickupToastLayout.ITEM_SIZE / 2);
		} finally {
			matrices.popMatrix();
		}
	}

	private static int withOpacity(int color, float opacity) {
		int alpha = Math.round((color >>> 24) * Math.clamp(opacity, 0.0F, 1.0F));
		return color & 0x00FFFFFF | alpha << 24;
	}

	private static List<ItemPickupToastQueue.Entry<ItemStack>> editorToasts() {
		return List.of(
			new ItemPickupToastQueue.Entry<>(new ItemStack(Items.COBBLESTONE), 12, Long.MAX_VALUE),
			new ItemPickupToastQueue.Entry<>(new ItemStack(Items.IRON_ORE), 3, Long.MAX_VALUE),
			new ItemPickupToastQueue.Entry<>(new ItemStack(Items.DIAMOND), 1, Long.MAX_VALUE)
		);
	}

	private static long secondsToNanos(double seconds) {
		return (long) (seconds * 1_000_000_000.0D);
	}

	private static Component label(ItemPickupToastQueue.Entry<ItemStack> toast, ItemPickupHudConfig config) {
		var count = Component.literal(config.getCountFormat().format(toast.amount()));
		return config.getStyle() == ItemPickupHudStyle.COMPACT
			? count : count.append(" ").append(toast.value().getHoverName());
	}
}
