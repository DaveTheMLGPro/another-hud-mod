package net.davethemlgpro.client.module.itempickup;

import com.google.gson.annotations.SerializedName;
import net.davethemlgpro.client.hud.layout.HudAnchor;
import net.davethemlgpro.client.hud.layout.ModuleLayout;
import net.davethemlgpro.client.module.HudModuleConfig;

public final class ItemPickupHudConfig implements HudModuleConfig<ItemPickupHudConfig> {
	public static final int MIN_VISIBLE_ITEMS = 1;
	public static final int MAX_VISIBLE_ITEMS = 10;
	public static final double MIN_DISPLAY_TIME_SECONDS = 0.5D;
	public static final double MAX_DISPLAY_TIME_SECONDS = 10.0D;
	public static final double MIN_REMOVE_DELAY_SECONDS = 0.1D;
	public static final double MAX_REMOVE_DELAY_SECONDS = 3.0D;
	public static final double MIN_FADE_DURATION_SECONDS = 0.1D;
	public static final double MAX_FADE_DURATION_SECONDS = 2.0D;
	public static final double MIN_MERGE_WINDOW_SECONDS = 0.0D;
	public static final double MAX_MERGE_WINDOW_SECONDS = 5.0D;
	public static final double MIN_ENTRY_ANIMATION_SECONDS = 0.1D;
	public static final double MAX_ENTRY_ANIMATION_SECONDS = 1.0D;
	public static final double MIN_UI_SCALE = 0.5D;
	public static final double MAX_UI_SCALE = 2.0D;
	public static final int MIN_ROW_SPACING = 0;
	public static final int MAX_ROW_SPACING = 10;
	public static final int DEFAULT_BACKGROUND_COLOR = 0x40000000;
	public static final int DEFAULT_TEXT_COLOR = 0xFF55FF55;

	@SerializedName("enabled")
	private boolean visible = true;
	private ModuleLayout layout = new ModuleLayout(HudAnchor.BOTTOM_RIGHT, -8, -8);
	private int maxVisibleItems = 3;
	private double displayTimeSeconds = 3.0D;
	private double removeDelaySeconds = 0.5D;
	private double fadeDurationSeconds = 0.5D;
	private double mergeWindowSeconds = 1.0D;
	private double entryAnimationSeconds = 0.25D;
	private ItemPickupPresentation presentation = ItemPickupPresentation.LIST;
	private ItemPickupGrowthDirection growthDirection = ItemPickupGrowthDirection.AUTO;
	private ItemPickupEntryAnimation entryAnimation = ItemPickupEntryAnimation.SLIDE;
	private ItemPickupMergeFeedback mergeFeedback = ItemPickupMergeFeedback.PULSE;
	private boolean stableWidth = true;
	private double uiScale = 1.0D;
	private int rowSpacing = 2;
	private boolean showItemIcon = true;
	private ItemPickupHudStyle style = ItemPickupHudStyle.NORMAL;
	private ItemPickupCountFormat countFormat = ItemPickupCountFormat.PLUS;
	private ItemPickupBackgroundStyle backgroundStyle = ItemPickupBackgroundStyle.TINTED_ROWS;
	private int backgroundColor = DEFAULT_BACKGROUND_COLOR;
	private int textColor = DEFAULT_TEXT_COLOR;
	private ItemPickupRemovalMode removalMode = ItemPickupRemovalMode.FADE_OUT;

	@Override
	public boolean visible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public ModuleLayout getLayout() {
		return layout;
	}

	@Override
	public ItemPickupHudConfig copy() {
		ItemPickupHudConfig copy = new ItemPickupHudConfig();
		copy.copyFrom(this);
		return copy;
	}

	@Override
	public void copyFrom(ItemPickupHudConfig source) {
		visible = source.visible;
		layout = new ModuleLayout();
		layout.copyFrom(source.layout);
		maxVisibleItems = source.maxVisibleItems;
		displayTimeSeconds = source.displayTimeSeconds;
		removeDelaySeconds = source.removeDelaySeconds;
		fadeDurationSeconds = source.fadeDurationSeconds;
		mergeWindowSeconds = source.mergeWindowSeconds;
		entryAnimationSeconds = source.entryAnimationSeconds;
		presentation = source.presentation;
		growthDirection = source.growthDirection;
		entryAnimation = source.entryAnimation;
		mergeFeedback = source.mergeFeedback;
		stableWidth = source.stableWidth;
		uiScale = source.uiScale;
		rowSpacing = source.rowSpacing;
		showItemIcon = source.showItemIcon;
		style = source.style;
		countFormat = source.countFormat;
		backgroundStyle = source.backgroundStyle;
		backgroundColor = source.backgroundColor;
		textColor = source.textColor;
		removalMode = source.removalMode;
		validate();
	}

	@Override
	public void validate() {
		if (layout == null) {
			layout = new ModuleLayout(HudAnchor.BOTTOM_RIGHT, -8, -8);
		}
		layout.validate();
		maxVisibleItems = Math.clamp(maxVisibleItems, MIN_VISIBLE_ITEMS, MAX_VISIBLE_ITEMS);
		displayTimeSeconds = finiteOrDefault(displayTimeSeconds, 3.0D);
		displayTimeSeconds = Math.clamp(displayTimeSeconds, MIN_DISPLAY_TIME_SECONDS, MAX_DISPLAY_TIME_SECONDS);
		removeDelaySeconds = finiteOrDefault(removeDelaySeconds, 0.5D);
		removeDelaySeconds = Math.clamp(removeDelaySeconds, MIN_REMOVE_DELAY_SECONDS, MAX_REMOVE_DELAY_SECONDS);
		fadeDurationSeconds = finiteOrDefault(fadeDurationSeconds, 0.3D);
		fadeDurationSeconds = Math.clamp(fadeDurationSeconds,
			MIN_FADE_DURATION_SECONDS, MAX_FADE_DURATION_SECONDS);
		mergeWindowSeconds = finiteOrDefault(mergeWindowSeconds, 1.0D);
		mergeWindowSeconds = Math.clamp(mergeWindowSeconds, MIN_MERGE_WINDOW_SECONDS, MAX_MERGE_WINDOW_SECONDS);
		entryAnimationSeconds = finiteOrDefault(entryAnimationSeconds, 0.25D);
		entryAnimationSeconds = Math.clamp(entryAnimationSeconds,
			MIN_ENTRY_ANIMATION_SECONDS, MAX_ENTRY_ANIMATION_SECONDS);
		if (presentation == null) {
			presentation = ItemPickupPresentation.LIST;
		}
		if (growthDirection == null) {
			growthDirection = ItemPickupGrowthDirection.AUTO;
		}
		if (!directionMatchesPresentation(growthDirection, presentation)) {
			growthDirection = ItemPickupGrowthDirection.AUTO;
		}
		if (entryAnimation == null) {
			entryAnimation = ItemPickupEntryAnimation.SLIDE;
		}
		if (mergeFeedback == null) {
			mergeFeedback = ItemPickupMergeFeedback.PULSE;
		}
		uiScale = Math.clamp(finiteOrDefault(uiScale, 1.0D), MIN_UI_SCALE, MAX_UI_SCALE);
		rowSpacing = Math.clamp(rowSpacing, MIN_ROW_SPACING, MAX_ROW_SPACING);
		if (style == null) {
			style = ItemPickupHudStyle.NORMAL;
		}
		if (countFormat == null) {
			countFormat = ItemPickupCountFormat.PLUS;
		}
		if (backgroundStyle == null) {
			backgroundStyle = ItemPickupBackgroundStyle.TINTED_ROWS;
		}
		if (removalMode == null) {
			removalMode = ItemPickupRemovalMode.INSTANT;
		}
	}

	public int getMaxVisibleItems() {
		return maxVisibleItems;
	}

	public void setMaxVisibleItems(int maxVisibleItems) {
		this.maxVisibleItems = Math.clamp(maxVisibleItems, MIN_VISIBLE_ITEMS, MAX_VISIBLE_ITEMS);
	}

	public double getDisplayTimeSeconds() {
		return displayTimeSeconds;
	}

	public void setDisplayTimeSeconds(double displayTimeSeconds) {
		this.displayTimeSeconds = Math.clamp(finiteOrDefault(displayTimeSeconds, 3.0D),
			MIN_DISPLAY_TIME_SECONDS, MAX_DISPLAY_TIME_SECONDS);
	}

	public double getRemoveDelaySeconds() {
		return removeDelaySeconds;
	}

	public void setRemoveDelaySeconds(double removeDelaySeconds) {
		this.removeDelaySeconds = Math.clamp(finiteOrDefault(removeDelaySeconds, 0.5D),
			MIN_REMOVE_DELAY_SECONDS, MAX_REMOVE_DELAY_SECONDS);
	}

	public double getFadeDurationSeconds() {
		return fadeDurationSeconds;
	}

	public void setFadeDurationSeconds(double fadeDurationSeconds) {
		this.fadeDurationSeconds = Math.clamp(finiteOrDefault(fadeDurationSeconds, 0.5D),
			MIN_FADE_DURATION_SECONDS, MAX_FADE_DURATION_SECONDS);
	}

	public double getMergeWindowSeconds() {
		return mergeWindowSeconds;
	}

	public void setMergeWindowSeconds(double mergeWindowSeconds) {
		this.mergeWindowSeconds = Math.clamp(finiteOrDefault(mergeWindowSeconds, 1.0D),
			MIN_MERGE_WINDOW_SECONDS, MAX_MERGE_WINDOW_SECONDS);
	}

	public double getEntryAnimationSeconds() {
		return entryAnimationSeconds;
	}

	public void setEntryAnimationSeconds(double entryAnimationSeconds) {
		this.entryAnimationSeconds = Math.clamp(finiteOrDefault(entryAnimationSeconds, 0.25D),
			MIN_ENTRY_ANIMATION_SECONDS, MAX_ENTRY_ANIMATION_SECONDS);
	}

	public ItemPickupPresentation getPresentation() {
		return presentation;
	}

	public void setPresentation(ItemPickupPresentation presentation) {
		this.presentation = presentation == null ? ItemPickupPresentation.LIST : presentation;
		if (!directionMatchesPresentation(growthDirection, this.presentation)) {
			growthDirection = ItemPickupGrowthDirection.AUTO;
		}
	}

	public ItemPickupGrowthDirection getGrowthDirection() {
		return growthDirection;
	}

	public void setGrowthDirection(ItemPickupGrowthDirection growthDirection) {
		this.growthDirection = growthDirection == null ? ItemPickupGrowthDirection.AUTO : growthDirection;
	}

	public ItemPickupEntryAnimation getEntryAnimation() {
		return entryAnimation;
	}

	public void setEntryAnimation(ItemPickupEntryAnimation entryAnimation) {
		this.entryAnimation = entryAnimation == null ? ItemPickupEntryAnimation.SLIDE : entryAnimation;
	}

	public ItemPickupMergeFeedback getMergeFeedback() {
		return mergeFeedback;
	}

	public void setMergeFeedback(ItemPickupMergeFeedback mergeFeedback) {
		this.mergeFeedback = mergeFeedback == null ? ItemPickupMergeFeedback.PULSE : mergeFeedback;
	}

	public boolean isStableWidth() {
		return stableWidth;
	}

	public void setStableWidth(boolean stableWidth) {
		this.stableWidth = stableWidth;
	}

	public double getUiScale() {
		return uiScale;
	}

	public void setUiScale(double uiScale) {
		this.uiScale = Math.clamp(finiteOrDefault(uiScale, 1.0D), MIN_UI_SCALE, MAX_UI_SCALE);
	}

	public int getRowSpacing() {
		return rowSpacing;
	}

	public void setRowSpacing(int rowSpacing) {
		this.rowSpacing = Math.clamp(rowSpacing, MIN_ROW_SPACING, MAX_ROW_SPACING);
	}

	public boolean isShowItemIcon() {
		return showItemIcon;
	}

	public void setShowItemIcon(boolean showItemIcon) {
		this.showItemIcon = showItemIcon;
	}

	public ItemPickupHudStyle getStyle() {
		return style;
	}

	public void setStyle(ItemPickupHudStyle style) {
		this.style = style == null ? ItemPickupHudStyle.NORMAL : style;
	}

	public ItemPickupCountFormat getCountFormat() {
		return countFormat;
	}

	public void setCountFormat(ItemPickupCountFormat countFormat) {
		this.countFormat = countFormat == null ? ItemPickupCountFormat.PLUS : countFormat;
	}

	public ItemPickupBackgroundStyle getBackgroundStyle() {
		return backgroundStyle;
	}

	public void setBackgroundStyle(ItemPickupBackgroundStyle backgroundStyle) {
		this.backgroundStyle = backgroundStyle == null
			? ItemPickupBackgroundStyle.TINTED_ROWS : backgroundStyle;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public ItemPickupRemovalMode getRemovalMode() {
		return removalMode;
	}

	public void setRemovalMode(ItemPickupRemovalMode removalMode) {
		this.removalMode = removalMode == null ? ItemPickupRemovalMode.INSTANT : removalMode;
	}

	private double finiteOrDefault(double value, double fallback) {
		return Double.isFinite(value) ? value : fallback;
	}

	private boolean directionMatchesPresentation(ItemPickupGrowthDirection direction,
											  ItemPickupPresentation presentation) {
		return direction == ItemPickupGrowthDirection.AUTO
			|| presentation == ItemPickupPresentation.LIST
				&& (direction == ItemPickupGrowthDirection.UP || direction == ItemPickupGrowthDirection.DOWN)
			|| presentation == ItemPickupPresentation.CARDS
				&& (direction == ItemPickupGrowthDirection.LEFT || direction == ItemPickupGrowthDirection.RIGHT);
	}
}
