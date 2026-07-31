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
	private static final int PREVIEW_ITEMS = 3;
	private static final int MERGE_PULSE_MILLIS = 350;
	private final ItemPickupToastQueue<ItemStack> toasts =
		new ItemPickupToastQueue<>(ItemPickupHudConfig.MAX_VISIBLE_ITEMS);
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
		List<ItemPickupToastQueue.Entry<ItemStack>> active =
			activeToasts(minecraft, config, System.nanoTime());
		return active.isEmpty() ? new HudSize(0, 0) : liveSize(minecraft, config, active);
	}

	@Override
	public HudSize measureEditorPreview(Minecraft minecraft, ItemPickupHudConfig config) {
		return previewSize(minecraft, config);
	}

	@Override
	public synchronized void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft,
									ItemPickupHudConfig config, HudBounds bounds) {
		long nowNanos = System.nanoTime();
		renderPickups(graphics, minecraft, config, bounds,
			activeToasts(minecraft, config, nowNanos), nowNanos, false, config.getMaxVisibleItems());
	}

	@Override
	public void renderEditorPreview(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Minecraft minecraft,
									ItemPickupHudConfig config, HudBounds bounds) {
		renderPickups(graphics, minecraft, config, bounds, editorToasts(), 0L, true, PREVIEW_ITEMS);
	}

	public synchronized void recordPickup(ItemStack source, int amount, ClientLevel level) {
		if (source.isEmpty() || amount <= 0) {
			return;
		}
		resetForLevel(level);
		toasts.record(source.copyWithCount(1), amount, System.nanoTime(),
			secondsToNanos(AnotherHUDModClient.getItemPickupHudConfig().getMergeWindowSeconds()),
			ItemStack::isSameItemSameComponents);
	}

	private List<ItemPickupToastQueue.Entry<ItemStack>> activeToasts(Minecraft minecraft,
														 ItemPickupHudConfig config, long nowNanos) {
		resetForLevel(minecraft.level);
		return toasts.snapshot(config.getMaxVisibleItems(), nowNanos,
			secondsToNanos(config.getDisplayTimeSeconds()),
			secondsToNanos(config.getRemoveDelaySeconds()),
			secondsToNanos(config.getFadeDurationSeconds()),
			config.getRemovalMode() == ItemPickupRemovalMode.FADE_OUT);
	}

	private void resetForLevel(ClientLevel level) {
		if (currentLevel != level) {
			currentLevel = level;
			toasts.clear();
		}
	}

	private HudSize previewSize(Minecraft minecraft, ItemPickupHudConfig config) {
		return scaledSize(unscaledPreviewSize(minecraft, config), config.getUiScale());
	}

	private HudSize unscaledPreviewSize(Minecraft minecraft, ItemPickupHudConfig config) {
		if (config.getPresentation() == ItemPickupPresentation.CARDS) {
			return ItemPickupCardLayout.measure(PREVIEW_ITEMS, config.getRowSpacing());
		}
		return measureList(minecraft, editorToasts(), config, PREVIEW_ITEMS);
	}

	private HudSize liveSize(Minecraft minecraft, ItemPickupHudConfig config,
							 List<ItemPickupToastQueue.Entry<ItemStack>> entries) {
		return scaledSize(unscaledLiveSize(minecraft, config, entries), config.getUiScale());
	}

	private HudSize unscaledLiveSize(Minecraft minecraft, ItemPickupHudConfig config,
								 List<ItemPickupToastQueue.Entry<ItemStack>> entries) {
		if (config.getPresentation() == ItemPickupPresentation.CARDS) {
			return ItemPickupCardLayout.measure(config.getMaxVisibleItems(), config.getRowSpacing());
		}
		List<ItemPickupToastQueue.Entry<ItemStack>> measuredEntries = config.isStableWidth()
			? editorToasts() : entries;
		return measureList(minecraft, measuredEntries, config, config.getMaxVisibleItems());
	}

	private HudSize measureList(Minecraft minecraft, List<ItemPickupToastQueue.Entry<ItemStack>> entries,
								ItemPickupHudConfig config, int capacity) {
		int widestText = entries.stream().mapToInt(entry -> minecraft.font.width(label(entry, config))).max().orElse(0);
		int minimumWidth = config.getStyle() == ItemPickupHudStyle.COMPACT
			? ItemPickupToastLayout.COMPACT_MIN_WIDTH : ItemPickupToastLayout.NORMAL_MIN_WIDTH;
		return ItemPickupToastLayout.measure(capacity, widestText, minimumWidth,
			listTextLeft(config), config.getRowSpacing());
	}

	private void renderPickups(GuiGraphicsExtractor graphics, Minecraft minecraft, ItemPickupHudConfig config,
							   HudBounds bounds, List<ItemPickupToastQueue.Entry<ItemStack>> entries,
							   long nowNanos, boolean editorPreview, int capacity) {
		if (entries.isEmpty()) {
			return;
		}
		HudSize unscaledSize = editorPreview ? unscaledPreviewSize(minecraft, config)
			: unscaledLiveSize(minecraft, config, entries);
		var matrices = graphics.pose();
		matrices.pushMatrix();
		try {
			float scale = (float) config.getUiScale();
			matrices.translate(bounds.x(), bounds.y());
			matrices.scale(scale, scale);
			HudBounds localBounds = new HudBounds(0, 0, unscaledSize.width(), unscaledSize.height());
			if (config.getPresentation() == ItemPickupPresentation.CARDS) {
				renderCards(graphics, minecraft, config, localBounds, bounds, entries,
					nowNanos, editorPreview, capacity);
			} else {
				renderList(graphics, minecraft, config, localBounds, bounds, entries,
					nowNanos, editorPreview, capacity);
			}
		} finally {
			matrices.popMatrix();
		}
	}

	private void renderList(GuiGraphicsExtractor graphics, Minecraft minecraft, ItemPickupHudConfig config,
							HudBounds bounds, HudBounds autoBounds,
							List<ItemPickupToastQueue.Entry<ItemStack>> entries,
							long nowNanos, boolean editorPreview, int capacity) {
		boolean upward = resolveVerticalDirection(minecraft, config, autoBounds) == ItemPickupGrowthDirection.UP;
		int step = ItemPickupToastLayout.ROW_HEIGHT + config.getRowSpacing();
		int animationOffset = entryOffset(entries, config, nowNanos, editorPreview, step, upward);
		int width = bounds.width();
		if (config.getBackgroundStyle() == ItemPickupBackgroundStyle.UNIFIED_PANEL) {
			int visibleCount = Math.min(entries.size(), capacity);
			int firstSlot = upward ? capacity - visibleCount : 0;
			int panelHeight = visibleCount * ItemPickupToastLayout.ROW_HEIGHT
				+ (visibleCount - 1) * config.getRowSpacing();
			drawRoundedBackground(graphics, bounds.x(), bounds.y() + firstSlot * step,
				width, panelHeight, config.getBackgroundColor());
		}
		for (int index = 0; index < entries.size(); index++) {
			var entry = entries.get(index);
			int slot = ItemPickupFlowLayout.slot(index, entries.size(), capacity, upward);
			int y = bounds.y() + slot * step + animationOffset;
			float opacity = toastOpacity(entry, config, nowNanos, editorPreview);
			if (config.getBackgroundStyle() == ItemPickupBackgroundStyle.TINTED_ROWS) {
				drawRoundedBackground(graphics, bounds.x(), y, width, ItemPickupToastLayout.ROW_HEIGHT,
					withOpacity(config.getBackgroundColor(), opacity));
			}
			if (showsListIcon(config)) {
				drawItem(graphics, entry.value(), bounds.x() + ItemPickupToastLayout.ICON_LEFT,
					y + ItemPickupToastLayout.ICON_TOP, opacity);
			}
			int textLeft = listTextLeft(config);
			Component fittedLabel = fitText(minecraft, label(entry, config),
				width - textLeft - ItemPickupToastLayout.RIGHT_PADDING);
			drawText(graphics, minecraft, fittedLabel,
				bounds.x() + textLeft, y + ItemPickupToastLayout.TEXT_TOP,
				config.getTextColor(), opacity, mergeScale(entry, config, nowNanos, editorPreview), false);
		}
	}

	private void renderCards(GuiGraphicsExtractor graphics, Minecraft minecraft, ItemPickupHudConfig config,
							 HudBounds bounds, HudBounds autoBounds,
							 List<ItemPickupToastQueue.Entry<ItemStack>> entries,
							 long nowNanos, boolean editorPreview, int capacity) {
		boolean leftward = resolveHorizontalDirection(minecraft, config, autoBounds) == ItemPickupGrowthDirection.LEFT;
		int step = ItemPickupCardLayout.CARD_WIDTH + config.getRowSpacing();
		int animationOffset = entryOffset(entries, config, nowNanos, editorPreview, step, leftward);
		if (config.getBackgroundStyle() == ItemPickupBackgroundStyle.UNIFIED_PANEL) {
			int visibleCount = Math.min(entries.size(), capacity);
			int firstSlot = leftward ? capacity - visibleCount : 0;
			int panelX = bounds.x() + ItemPickupCardLayout.cardX(firstSlot, config.getRowSpacing());
			int panelWidth = ItemPickupCardLayout.measure(visibleCount, config.getRowSpacing()).width();
			drawRoundedBackground(graphics, panelX, bounds.y(), panelWidth,
				ItemPickupCardLayout.CARD_HEIGHT, config.getBackgroundColor());
			for (int separator = 1; separator < visibleCount; separator++) {
				int separatorX = panelX + separator * step - (config.getRowSpacing() + 1) / 2;
				graphics.fill(separatorX, bounds.y() + 5, separatorX + 1,
					bounds.y() + ItemPickupCardLayout.CARD_HEIGHT - 5, 0x30FFFFFF);
			}
		}
		for (int index = 0; index < entries.size(); index++) {
			var entry = entries.get(index);
			int slot = ItemPickupFlowLayout.slot(index, entries.size(), capacity, leftward);
			int x = bounds.x() + ItemPickupCardLayout.cardX(slot, config.getRowSpacing()) + animationOffset;
			float opacity = toastOpacity(entry, config, nowNanos, editorPreview);
			if (config.getBackgroundStyle() == ItemPickupBackgroundStyle.TINTED_ROWS) {
				drawRoundedBackground(graphics, x, bounds.y(), ItemPickupCardLayout.CARD_WIDTH,
					ItemPickupCardLayout.CARD_HEIGHT, withOpacity(config.getBackgroundColor(), opacity));
			}
			int iconX = x + (ItemPickupCardLayout.CARD_WIDTH - ItemPickupToastLayout.ITEM_SIZE) / 2;
			drawItem(graphics, entry.value(), iconX, bounds.y() + ItemPickupCardLayout.ICON_TOP, opacity);
			drawText(graphics, minecraft, countLabel(entry, config),
				x + ItemPickupCardLayout.CARD_WIDTH / 2, bounds.y() + ItemPickupCardLayout.TEXT_TOP,
				config.getTextColor(), opacity, mergeScale(entry, config, nowNanos, editorPreview), true);
		}
	}

	private int entryOffset(List<ItemPickupToastQueue.Entry<ItemStack>> entries, ItemPickupHudConfig config,
							long nowNanos, boolean editorPreview, int step, boolean towardNegative) {
		if (editorPreview || config.getEntryAnimation() == ItemPickupEntryAnimation.INSTANT) {
			return 0;
		}
		var newest = entries.getLast();
		if (newest.merged()) {
			return 0;
		}
		long duration = secondsToNanos(config.getEntryAnimationSeconds());
		long age = Math.max(0L, nowNanos - newest.updatedAtNanos());
		float linear = duration == 0L ? 1.0F : Math.clamp((float) age / duration, 0.0F, 1.0F);
		float eased = 1.0F - (float) Math.pow(1.0F - linear, 3.0D);
		return ItemPickupFlowLayout.entryOffset(step, eased, towardNegative);
	}

	private static ItemPickupGrowthDirection resolveVerticalDirection(Minecraft minecraft,
														ItemPickupHudConfig config, HudBounds bounds) {
		if (config.getGrowthDirection() == ItemPickupGrowthDirection.UP
			|| config.getGrowthDirection() == ItemPickupGrowthDirection.DOWN) {
			return config.getGrowthDirection();
		}
		return ItemPickupFlowLayout.growsTowardNegative(bounds.y(), bounds.height(),
			minecraft.getWindow().getGuiScaledHeight())
			? ItemPickupGrowthDirection.UP : ItemPickupGrowthDirection.DOWN;
	}

	private static ItemPickupGrowthDirection resolveHorizontalDirection(Minecraft minecraft,
														  ItemPickupHudConfig config, HudBounds bounds) {
		if (config.getGrowthDirection() == ItemPickupGrowthDirection.LEFT
			|| config.getGrowthDirection() == ItemPickupGrowthDirection.RIGHT) {
			return config.getGrowthDirection();
		}
		return ItemPickupFlowLayout.growsTowardNegative(bounds.x(), bounds.width(),
			minecraft.getWindow().getGuiScaledWidth())
			? ItemPickupGrowthDirection.LEFT : ItemPickupGrowthDirection.RIGHT;
	}

	private float toastOpacity(ItemPickupToastQueue.Entry<ItemStack> entry, ItemPickupHudConfig config,
							   long nowNanos, boolean editorPreview) {
		return editorPreview ? 1.0F : toasts.opacity(entry, nowNanos,
			secondsToNanos(config.getDisplayTimeSeconds()), secondsToNanos(config.getFadeDurationSeconds()),
			config.getRemovalMode() == ItemPickupRemovalMode.FADE_OUT);
	}

	private static float mergeScale(ItemPickupToastQueue.Entry<ItemStack> entry, ItemPickupHudConfig config,
								long nowNanos, boolean editorPreview) {
		if (editorPreview || !entry.merged() || config.getMergeFeedback() == ItemPickupMergeFeedback.NONE) {
			return 1.0F;
		}
		float progress = Math.clamp((float) (nowNanos - entry.updatedAtNanos())
			/ (MERGE_PULSE_MILLIS * 1_000_000L), 0.0F, 1.0F);
		return 1.0F + 0.25F * (float) Math.sin(Math.PI * progress);
	}

	private static void drawText(GuiGraphicsExtractor graphics, Minecraft minecraft, Component text,
							 int anchorX, int y, int color, float opacity, float scale, boolean centered) {
		int textWidth = minecraft.font.width(text);
		int x = centered ? anchorX - textWidth / 2 : anchorX;
		if (Math.abs(scale - 1.0F) < 0.001F) {
			graphics.text(minecraft.font, text, x, y, withOpacity(color, opacity));
			return;
		}
		var matrices = graphics.pose();
		matrices.pushMatrix();
		try {
			float centerX = x + textWidth / 2.0F;
			float centerY = y + minecraft.font.lineHeight / 2.0F;
			matrices.translate(centerX, centerY);
			matrices.scale(scale, scale);
			graphics.text(minecraft.font, text, -textWidth / 2, -minecraft.font.lineHeight / 2,
				withOpacity(color, opacity));
		} finally {
			matrices.popMatrix();
		}
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

	private static HudSize scaledSize(HudSize size, double scale) {
		return new HudSize((int) Math.ceil(size.width() * scale),
			(int) Math.ceil(size.height() * scale));
	}

	private static boolean showsListIcon(ItemPickupHudConfig config) {
		return config.getStyle() == ItemPickupHudStyle.COMPACT || config.isShowItemIcon();
	}

	private static int listTextLeft(ItemPickupHudConfig config) {
		return showsListIcon(config) ? ItemPickupToastLayout.TEXT_LEFT : ItemPickupToastLayout.ICON_LEFT;
	}

	private static Component fitText(Minecraft minecraft, Component text, int maximumWidth) {
		if (maximumWidth <= 0 || minecraft.font.width(text) <= maximumWidth) {
			return text;
		}
		String ellipsis = "…";
		int available = Math.max(0, maximumWidth - minecraft.font.width(ellipsis));
		return Component.literal(minecraft.font.plainSubstrByWidth(text.getString(), available) + ellipsis);
	}

	private static List<ItemPickupToastQueue.Entry<ItemStack>> editorToasts() {
		return List.of(
			new ItemPickupToastQueue.Entry<>(new ItemStack(Items.COBBLESTONE), 12, Long.MAX_VALUE, false),
			new ItemPickupToastQueue.Entry<>(new ItemStack(Items.IRON_ORE), 3, Long.MAX_VALUE, false),
			new ItemPickupToastQueue.Entry<>(new ItemStack(Items.DIAMOND), 1, Long.MAX_VALUE, false)
		);
	}

	private static long secondsToNanos(double seconds) {
		return (long) (seconds * 1_000_000_000.0D);
	}

	private static Component label(ItemPickupToastQueue.Entry<ItemStack> entry, ItemPickupHudConfig config) {
		Component count = countLabel(entry, config);
		return config.getStyle() == ItemPickupHudStyle.COMPACT
			? count : count.copy().append(" ").append(entry.value().getHoverName());
	}

	private static Component countLabel(ItemPickupToastQueue.Entry<ItemStack> entry, ItemPickupHudConfig config) {
		return Component.literal(config.getCountFormat().format(entry.amount()));
	}
}
